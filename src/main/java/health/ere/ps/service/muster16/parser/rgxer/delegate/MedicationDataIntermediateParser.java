package health.ere.ps.service.muster16.parser.rgxer.delegate;

import health.ere.ps.service.muster16.parser.rgxer.matcher.MedicationMatcher;
import health.ere.ps.service.muster16.parser.rgxer.model.MedicationLine;

import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class MedicationDataIntermediateParser {

    private final Pattern DOSAGE_PAT = Pattern.compile("[01]\\s*-\\s*[01]\\s*-\\s*[01]");
    private final Pattern STRENGTH_PAT = Pattern.compile("(?<value>\\d+([.,]\\d+)?)\\s*(?<unit>(μg|mg|g|ml|%))");
    private final Pattern SEPARATOR_PAT = Pattern.compile("[^\\w\\d]+");
    private final MedicationMatcher matcher;


    public MedicationDataIntermediateParser() {
        matcher = new MedicationMatcher();
    }

    public List<String> parse(String entry) {

        List<MedicationLine> lines = initializeLines(entry);
        List<MedicationLine> merged = tryMerge(lines);
        return merged.stream().map(MedicationLine::getValue).collect(Collectors.toList());
    }

    private List<MedicationLine> tryMerge(List<MedicationLine> lines) {
        for (int i = 1; i < lines.size(); )
            if (canMerge(lines.get(i - 1), lines.get(i))) {
                MedicationLine first = lines.get(i - 1), second = lines.remove(i);
                first.merge(second);
            } else
                i++;
        return lines;
    }

    private boolean canMerge(MedicationLine first, MedicationLine second) {
        float score = 0;
        score += nameExists(second) ? 4 : 0;
        score += hasDosage(second) ? 2 : 0;
        score += hasStrength(second) ? 2 : 0;
        return score < 4;
    }

    private List<MedicationLine> initializeLines(String entry) {
        return Arrays.stream(entry.split("\\n"))
                .filter(this::validLine)
                .map(MedicationLine::new)
                .collect(Collectors.toList());
    }

    private boolean nameExists(MedicationLine line) {
        return matcher.matchName(line.getSequence().get(0));
    }

    private boolean validLine(String line) {
        return !SEPARATOR_PAT.matcher(line).matches();
    }

    private boolean hasDosage(MedicationLine line) {
        return DOSAGE_PAT.matcher(line.getValue()).find();
    }

    private boolean hasStrength(MedicationLine line) {
        return STRENGTH_PAT.matcher(line.getValue()).find();
    }
}
