package health.ere.ps.service.cetp.tracker;

import health.ere.ps.config.AppConfig;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.jboss.logmanager.Level;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import static de.health.service.cetp.utils.Utils.terminateExecutor;

@ApplicationScoped
public class TrackerService {

    private static final Logger log = Logger.getLogger(TrackerService.class.getName());

    public static final String REQUESTS_CSV = "requests.csv";

    private final ExecutorService executor = Executors.newSingleThreadExecutor();

    private final LinkedBlockingQueue<BillingInfo> queue = new LinkedBlockingQueue<>();

    @Inject
    AppConfig appConfig;

    @PostConstruct
    void onStart() {
        log.info("TrackerService is started");

        String billingCsvFolder = appConfig.getBillingCsvFolder();
        File csvFolder = new File(billingCsvFolder);
        if (!csvFolder.exists()) {
            boolean created = csvFolder.mkdir();
            if (!created) {
                log.warning(String.format("Can't create Billing CSV folder: '%s'", billingCsvFolder));
            }
        }

        List<BillingInfo> infos = new ArrayList<>();
        int trackBatch = appConfig.getTrackBatch();
        executor.submit(() -> {
            while (!Thread.currentThread().isInterrupted()) {
                try {
                    for (int i = 0; i < trackBatch; i++) {
                        infos.add(queue.take());
                    }
                    flush(infos);
                } catch (InterruptedException e) {
                    flush(infos);
                    break;
                }
            }
            flush(queue.stream().toList());
        });
    }

    public boolean submit(String ctId, String mandantId, String workplaceId, String clientSystemId) {
        return queue.offer(new BillingInfo(Instant.now(), ctId, mandantId, workplaceId, clientSystemId));
    }

    private void flush(List<BillingInfo> infos) {
        if (infos.isEmpty()) {
            return;
        }
        String data = infos.stream().map(BillingInfo::toString).collect(Collectors.joining("\n"));
        String path = appConfig.getBillingCsvFolder() + "/" + REQUESTS_CSV;
        try (OutputStream os = new FileOutputStream(path, true)) {
            os.write(String.format("%s\n", data).getBytes());
        } catch (IOException e) {
            String msg = String.format("Unable to write tracked requests, %d records are lost", infos.size());
            log.log(Level.SEVERE, msg, e);
            for (BillingInfo info : infos) {
                log.warning(String.format("Lost billing record: %s", info));
            }
        } finally {
            infos.clear();
        }
    }

    @PreDestroy
    public void close() throws Exception {
        terminateExecutor(executor, "TrackerService", 5000);
    }
}
