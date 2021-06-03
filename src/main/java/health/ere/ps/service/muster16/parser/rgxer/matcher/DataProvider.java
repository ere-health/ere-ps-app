package health.ere.ps.service.muster16.parser.rgxer.matcher;

import ca.uhn.fhir.rest.server.exceptions.ResourceNotFoundException;
import health.ere.ps.service.muster16.parser.rgxer.model.MedicationRecord;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;

import java.io.*;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;
import java.util.stream.Collectors;

public class DataProvider {

    private final List<MedicationRecord> records;

    private static final Logger log = Logger.getLogger(DataProvider.class.getName());

    public DataProvider() {
        List<MedicationRecord> _records;
        try {
            _records = loadFromFile();
        } catch (URISyntaxException | IOException e) {
            _records = new ArrayList<>();
            log.severe("Failed to load data file");
        }
        records = _records;
    }

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

    private File getDataFile() throws URISyntaxException {
        String resourcePath = "data/medication-data.csv"; // FIXME replace hardcoded file path
        URL resource = getClass().getClassLoader().getResource(resourcePath);
        if (resource == null)
            throw new ResourceNotFoundException(resourcePath);
        else
            return new File(resource.toURI());

    }
}
