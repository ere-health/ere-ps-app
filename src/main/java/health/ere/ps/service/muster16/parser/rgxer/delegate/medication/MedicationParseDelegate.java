package health.ere.ps.service.muster16.parser.rgxer.delegate.medication;

import health.ere.ps.model.muster16.MedicationString;
import health.ere.ps.service.muster16.parser.rgxer.matcher.MedicationMatcher;
import health.ere.ps.service.muster16.parser.rgxer.model.MedicationRecord;
import health.ere.ps.service.muster16.parser.rgxer.delegate.pattern.MedicationPatterns;

import java.util.List;
import java.util.regex.Matcher;
import java.util.stream.Collectors;

public class MedicationParseDelegate {

    private final MedicationEntryParseDelegate intermediateParser;
    private final MedicationMatcher matcher;
    private final MedicationEntrySplitDelegate nameResolver;
    private final MedicationFormatDelegate formatter;
    private final MedicationPatterns patterns;

    private final int PZN_LENGTH = 8;


    public MedicationParseDelegate() {
        this.intermediateParser = new MedicationEntryParseDelegate();
        this.matcher = new MedicationMatcher();
        this.nameResolver = new MedicationEntrySplitDelegate();
        this.formatter = new MedicationFormatDelegate();
        this.patterns = new MedicationPatterns();
    }

    public List<MedicationString> parse(String entry) {
        List<String> lines = intermediateParser.parse(entry);
        return lines.stream().map(this::parseLine).collect(Collectors.toList());
    }

    private MedicationString parseLine(String line) {
        String pzn = getPZN(line);
        String form = pzn != null ? getForm(pzn) : null;
        String size = getSize(line, pzn);

        int index = nameResolver.getSplitIndex(line);
        String name = line.substring(0, index), dosage = line.substring(index);

        name = formatter.formatName(name);
        dosage = formatter.formatDosage(dosage);

        return new MedicationString(name, size, form, dosage, null, pzn);
    }

    private String getForm(String pzn) {
        MedicationRecord record = findRecord(pzn);
        return record != null ? record.getForm() : null;
    }

    private MedicationRecord findRecord(String pzn) {
        return matcher.findByPZN(pzn);
    }

    private String getSize(String entry, String pzn) {
        if (pzn != null) {
            MedicationRecord record = matcher.findByPZN(pzn);
            if (record != null)
                return record.getNorm();
        }
        return parseSize(entry);
    }

    private String parseSize(String entry) {
        Matcher matcher = patterns.SIZE_PAT.matcher(entry);
        return matcher.find() ? matcher.group() : null;
    }

    private String getPZN(String entry) {
        String pzn;
        if ((pzn = extractPZN(entry)) != null) {
            return normalizePZN(pzn);
        } else {
            MedicationRecord record = matcher.bestMatch(entry);
            return record != null ? record.getPZN() : null;
        }
    }

    private String normalizePZN(String pzn) {
        final int paddingSize = PZN_LENGTH - pzn.length();
        final String padding = new String(new char[paddingSize]).replace("\0", "0");
        return padding + pzn;
    }

    private String extractPZN(String entry) {
        Matcher matcher = patterns.PZN_PAT.matcher(entry);
        return matcher.find() ? matcher.group("value") : null;
    }
}
