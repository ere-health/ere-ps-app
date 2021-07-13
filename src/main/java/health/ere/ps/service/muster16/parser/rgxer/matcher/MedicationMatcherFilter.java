package health.ere.ps.service.muster16.parser.rgxer.matcher;

import health.ere.ps.service.muster16.parser.rgxer.model.MedicationRecord;

public class MedicationMatcherFilter {

    private String getFirstToken(String string) {
        String[] tokens = string.split(" ");
        return tokens.length > 0 ? tokens[0] : null;
    }

    boolean containsFirstToken(String entry, MedicationRecord record) {
        String token1 = getFirstToken(entry), token2 = getFirstToken(record.getName());

        return (token1 != null && token2 != null) &&
                (token1.equals(token2) || token1.contains(token2) || token2.contains(token1));
    }
}
