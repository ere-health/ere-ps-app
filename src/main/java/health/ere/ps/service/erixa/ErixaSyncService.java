package health.ere.ps.service.erixa;

import health.ere.ps.config.UserConfig;
import health.ere.ps.event.erixa.ErixaSyncEvent;
import health.ere.ps.model.erixa.ErixaSyncLoad;
import org.apache.pdfbox.pdmodel.PDDocument;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.ObservesAsync;
import javax.inject.Inject;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Base64;
import java.util.logging.Logger;

@ApplicationScoped
public class ErixaSyncService {

    private final Logger log = Logger.getLogger(ErixaSyncService.class.getName());

    private final DateTimeFormatter dateFormat = DateTimeFormatter.ofPattern("yyyyMMdd");

    @Inject
    UserConfig userConfig;

    public void syncToHotfolder(@ObservesAsync ErixaSyncEvent event) {
        log.info("ErixaSyncEvent");
        try {
            final ErixaSyncLoad load = event.load;
            PDDocument document = parseDocument(load.getDocument());
            Path path = buildFilePath(load.getPatient());
            saveDocument(document, path);
        } catch (IOException e) {
            log.severe("Unable to parse document");
        }
    }

    private PDDocument parseDocument(String document) throws IOException {
        byte[] decodedString = Base64.getDecoder().decode(document.getBytes(StandardCharsets.UTF_8));
        return PDDocument.load(decodedString);
    }

    private Path buildFilePath(String patientName) {
        return getHotfolderPath().resolve(buildFileName(patientName));
    }

    private Path getHotfolderPath() {
        return Path.of(userConfig.getErixaHotfolder());
    }

    private String buildFileName(String patientName) {
        return String.format("[%s] [%s] [%s].pdf", getDateString(), patientName, getReceiverEmail());
    }

    private String getDateString() {
        return LocalDate.now().format(dateFormat);
    }

    private String getReceiverEmail() {
        return userConfig.getErixaReceiverEmail();
    }

    private void saveDocument(PDDocument document, Path path) throws IOException {
        document.save(path.toFile());
    }
}
