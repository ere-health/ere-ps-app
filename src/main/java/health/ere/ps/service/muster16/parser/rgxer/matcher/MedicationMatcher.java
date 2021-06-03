package health.ere.ps.service.muster16.parser.rgxer.matcher;

import health.ere.ps.service.muster16.parser.rgxer.model.MedicationRecord;

public class MedicationMatcher {

    private final DataProvider provider;

    public MedicationMatcher() {
        this.provider = new DataProvider();
    }

    int calculateSimilarity(String entry, MedicationRecord record) {
        // TODO: Implement this method
        throw new UnsupportedOperationException();
    }

    public boolean matchName(String token) {
        return provider.getRecords().stream().anyMatch(record -> matchName(token, record));
    }

    private boolean matchName(String token, MedicationRecord record) {
        return record.getName().split(" ")[0].equalsIgnoreCase(token);
    }
}
