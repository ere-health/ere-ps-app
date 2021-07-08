package health.ere.ps.service.muster16.parser.rgxer.delegate.medication;

import health.ere.ps.service.muster16.parser.rgxer.delegate.pattern.MedicationPatterns;
import health.ere.ps.service.muster16.parser.rgxer.matcher.MedicationMatcher;
import health.ere.ps.service.muster16.parser.rgxer.model.MedicationLine;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class MedicationEntryParseDelegate {


    private final MedicationMatcher matcher;
    private final MedicationPatterns patterns;


    public MedicationEntryParseDelegate() {
        matcher = new MedicationMatcher();
        patterns = new MedicationPatterns();
    }

    public List<String> parse(String entry) {

        List<MedicationLine> lines = initializeLines(entry);
        List<MedicationLine> merged = tryMerge(lines);
        return merged.stream().map(MedicationLine::getValue).collect(Collectors.toList());
    }

    private List<MedicationLine> tryMerge(List<MedicationLine> lines) {
        for (int i = 1; i < lines.size(); )
            if (canMerge(lines.get(i))) {
                MedicationLine first = lines.get(i - 1), second = lines.remove(i);
                first.merge(second);
            } else
                i++;
        return lines;
    }

    private boolean canMerge(MedicationLine second) {
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
        return !patterns.SEPARATOR_PAT.matcher(line).matches();
    }

    private boolean hasDosage(MedicationLine line) {
        return patterns.DOSAGE_PAT.matcher(line.getValue()).find();
    }

    private boolean hasStrength(MedicationLine line) {
        return patterns.STRENGTH_PAT.matcher(line.getValue()).find();
    }
}
