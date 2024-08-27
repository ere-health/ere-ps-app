package health.ere.ps.service.cetp.tracker;

import com.google.common.io.Files;
import health.ere.ps.config.AppConfig;
import health.ere.ps.profile.RUDevTestProfile;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.junit.TestProfile;
import jakarta.inject.Inject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

import static health.ere.ps.service.cetp.tracker.TrackerService.REQUESTS_CSV;
import static health.ere.ps.utils.Utils.deleteFiles;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@QuarkusTest
@TestProfile(RUDevTestProfile.class)
public class TrackerTest {

    private static final Logger log = Logger.getLogger(TrackerTest.class.getName());

    private final ScheduledExecutorService scheduledThreadPool = Executors.newScheduledThreadPool(10);

    private final Random random = new Random();

    @Inject
    AppConfig appConfig;

    @Inject
    TrackerService trackerService;

    @BeforeEach
    void before() {
        String billingCsvFolder = appConfig.getBillingCsvFolder();
        File csvFolder = new File(billingCsvFolder);
        if (csvFolder.exists()) {
            boolean deleted = deleteFiles(csvFolder, file -> file.getName().endsWith(".csv"));
            if (!deleted) {
                log.warning("Can't delete test CSV files");
            }
        }
    }

    @Test
    public void billingRecordsFlushedToFile() throws Exception {
        int cnt = 1 + random.nextInt(100);

        List<Future<Boolean>> futures = new ArrayList<>();
        for (int i = 0; i < cnt; i++) {
            futures.add(scheduledThreadPool.submit(() ->
                trackerService.submit(
                    String.valueOf(random.nextInt(100)),
                    String.valueOf(random.nextInt(100)),
                    String.valueOf(random.nextInt(100)),
                    String.valueOf(random.nextInt(100))
                )
            ));
        }
        List<Boolean> statuses = new ArrayList<>();
        for (int i = 0; i < cnt; i++) {
            statuses.add(futures.get(i).get());
        }
        
        assertTrue(statuses.stream().allMatch(bool -> true));

        // emulating closing of the application context
        trackerService.close();
        TimeUnit.SECONDS.sleep(5);
        
        File path = new File(appConfig.getBillingCsvFolder() + "/" + REQUESTS_CSV);
        List<String> lines = Files.readLines(path, StandardCharsets.UTF_8);
        assertEquals(cnt, lines.stream().filter(l -> !l.isEmpty()).toList().size());
    }
}
