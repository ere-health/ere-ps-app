package health.ere.ps.service.cetp.tracker;

import jakarta.enterprise.context.ApplicationScoped;

import java.io.File;
import java.io.FileOutputStream;
import java.nio.channels.FileChannel;
import java.nio.channels.FileLock;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.logging.Level;
import java.util.logging.Logger;

@ApplicationScoped
public class PrescriptionTracker {

    private static final Logger log = Logger.getLogger(PrescriptionTracker.class.getName());

    public static final String TELEMATIK_FILE = "telematik-accepted.csv";

    private static final DateTimeFormatter formatter = DateTimeFormatter
        .ofPattern("yyyy-MM-dd'T'HH:mm:ss")
        .withZone(ZoneId.systemDefault());
    
    private final File telematikAcceptedFile = new File(TELEMATIK_FILE);

    public void countSuccessfulPrescription(String telematikId) {
        try {
            try (FileOutputStream os = new FileOutputStream(telematikAcceptedFile, true)) {
                FileChannel channel = os.getChannel();
                FileLock lock = channel.lock();
                try (channel) {
                    String timestamp = formatter.format(Instant.now());
                    TelematikIdInfo telematikIdInfo = new TelematikIdInfo(timestamp, telematikId);
                    os.write(telematikIdInfo.toString().getBytes());
                } finally {
                    lock.release();
                }
            }
        } catch (Exception e) {
            log.log(Level.SEVERE, "Error while appending " + TELEMATIK_FILE, e);
        }
    }
}
