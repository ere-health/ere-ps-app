package health.ere.ps.service.muster16.parser.rgxer.matcher;

import health.ere.ps.service.muster16.parser.rgxer.model.MedicationRecord;
import health.ere.ps.service.muster16.parser.rgxer.provider.MedicationDataProvider;

import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;

public class MedicationMatcher {

    private final MedicationDataProvider provider;
    private final SimilarityCalculator similarityCalculator;
    private final MedicationMatcherFilter matcherFilter;

    private Map<String, MedicationRecord> pznMedicationMap;

    public MedicationMatcher() {
        this.provider = new MedicationDataProvider();
        this.similarityCalculator = new SimilarityCalculator();
        this.matcherFilter = new MedicationMatcherFilter();
        initializePZNMap();
    }

    private void initializePZNMap() {
        pznMedicationMap = new HashMap<>();
        provider.getRecords().forEach(r -> pznMedicationMap.put(r.getPZN(), r));
    }

    public boolean matchName(String token) {
        return provider.getRecords().stream().anyMatch(record -> matchName(token, record));
    }

    private boolean matchName(String token, MedicationRecord record) {
        return record.getName().split(" ")[0].equalsIgnoreCase(token);
    }

    public MedicationRecord findByPZN(String pzn) {
        return pznMedicationMap.getOrDefault(pzn, null);
    }

    public MedicationRecord bestMatch(String entry) {
        return provider.getRecords().stream()
                .filter(record -> matcherFilter.containsFirstToken(entry, record))
                .max(Comparator.comparing((MedicationRecord r) -> similarityCalculator.calculate(entry, r)))
                .orElse(null);
    }
}
