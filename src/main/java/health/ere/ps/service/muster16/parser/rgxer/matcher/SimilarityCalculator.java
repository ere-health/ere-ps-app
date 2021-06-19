package health.ere.ps.service.muster16.parser.rgxer.matcher;

import health.ere.ps.service.muster16.parser.rgxer.model.MedicationRecord;
import me.xdrop.fuzzywuzzy.FuzzySearch;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SimilarityCalculator {

    private final double NAME_MATCH_FACTOR = 0.51;
    private final double SIZE_MATCH_FACTOR = 0.1225;
    private final double STRENGTH_MATCH_FACTOR = 0.1225;
    private final double FUZZY_MATCH_FACTOR = 0.254;

    final Pattern SIZE_PAT = Pattern.compile("\\b(N[1-3]|KP)\\b");
    private final Pattern STRENGTH_PAT = Pattern.compile("(?<value>\\d+([.,]\\d+)?)\\s*(?<unit>(μg|mg|g|ml|%))");

    public SimilarityCalculator() {
    }

    public double calculate(String entry, MedicationRecord record) {
        return calculateNameScore(entry, record) * NAME_MATCH_FACTOR +
                calculateStrengthScore(entry, record) * STRENGTH_MATCH_FACTOR +
                calculateSizeScore(entry, record) * SIZE_MATCH_FACTOR +
                calculateFuzzyScore(entry, record) * FUZZY_MATCH_FACTOR;
    }

    private int calculateNameScore(String entry, MedicationRecord record) {
        String token1 = getFirstToken(entry), token2 = getFirstToken(record.getName());
        if (token1 == null || token2 == null)
            return 0;
        else if (token1.equals(token2))
            return 100;
        else if (token1.contains(token2) || token2.contains(token1))
            return 40;
        return 0;
    }

    private String getFirstToken(String string) {
        String[] tokens = string.split(" ");
        return tokens.length > 0 ? tokens[0] : null;
    }

    private int calculateStrengthScore(String entry, MedicationRecord record) {
        final String s1 = getStrength(entry), s2 = getStrength(record.getName());
        if (s1 == null && s2 == null)
            return 50;
        else if (s1 == null || s2 == null)
            return 25;
        else
            return 100 * (s1.equals(s2) ? 1 : 0);
    }

    private int calculateSizeScore(String entry, MedicationRecord record) {
        final String size1 = getSize(entry), size2 = record.getNorm();
        if (size1 == null && size2 == null)
            return 75;
        else if (size1 == null || size2 == null)
            return 50;
        else
            return 100 * (size1.equals(size2) ? 1 : 0);
    }

    private int calculateFuzzyScore(String entry, MedicationRecord record) {
        return FuzzySearch.tokenSetRatio(entry, record.getName());
    }

    private String getStrength(String entry) {
        final Matcher matcher = STRENGTH_PAT.matcher(entry);
        return matcher.find() ? matcher.group("value") + matcher.group("unit") : null;
    }

    private String getSize(String entry) {
        final Matcher matcher = SIZE_PAT.matcher(entry);
        return matcher.find() ? matcher.group() : null;
    }
}
