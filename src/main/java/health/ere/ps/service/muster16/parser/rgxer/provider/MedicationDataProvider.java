package health.ere.ps.service.muster16.parser.rgxer.provider;

import health.ere.ps.service.muster16.parser.rgxer.model.MedicationRecord;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;

import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;
import java.util.stream.Collectors;

public class MedicationDataProvider implements DataProvider<MedicationRecord> {

    private final List<MedicationRecord> records;

    private static final Logger log = Logger.getLogger(MedicationDataProvider.class.getName());

    public MedicationDataProvider() {
        List<MedicationRecord> _records;
        try {
            _records = loadFromFile();
        } catch (URISyntaxException | IOException e) {
            _records = new ArrayList<>();
            log.severe("Failed to load data file");
        }
        records = _records;
    }

    @Override
    public List<MedicationRecord> getRecords() {
        return records;
    }

    private List<MedicationRecord> loadFromFile() throws URISyntaxException, IOException {
        return loadCSVRecords().stream().map(this::parseRecord).collect(Collectors.toList());
    }

    private MedicationRecord parseRecord(CSVRecord csvRecord) {
        return new MedicationRecord(
                csvRecord.get("PZN"),
                csvRecord.get("Name"),
                csvRecord.get("Norm"),
                csvRecord.get("Amount"),
                csvRecord.get("Darreichung")
        );
    }

    private List<CSVRecord> loadCSVRecords() throws URISyntaxException, IOException {
        Reader csvFile = new FileReader(getDataFile());
        CSVParser csvParser = CSVFormat.DEFAULT.withFirstRecordAsHeader().parse(csvFile);
        return csvParser.getRecords();
    }

    @Override
    public String getFilePath() {
        return "data/medication-data.csv";
    }
}
