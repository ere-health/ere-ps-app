package health.ere.ps.jmx;

import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import javax.management.ObjectName;
import java.lang.management.ManagementFactory;
import java.util.ArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

@QuarkusTest
class ReadEPrescriptionsMXBeanImplTest {
    @Inject
    ReadEPrescriptionsMXBeanImpl bean;

    @Test
    void testCIDandParallel() throws Exception {
        Assertions.assertNotNull(bean);
        var nrTasks = 10_000;
        var tasks = new ArrayList<Future<?>>(nrTasks);
        int numOfCores = Runtime.getRuntime().availableProcessors() / 2;
        ExecutorService pool = Executors.newFixedThreadPool(numOfCores);
        for (int i = 0; i < nrTasks; i++) {
            var future = pool.submit(bean::increaseNumberEPrescriptionRead);
            tasks.add(future);
        }
        for (var t : tasks) {
            t.get();
        }
        pool.shutdownNow(); //autocloseable with java 21+

        Assertions.assertTrue(bean.getNumberEPrescriptionRead() >= nrTasks);
        var platformMBeanServer = ManagementFactory.getPlatformMBeanServer();
        Assertions.assertNotNull(platformMBeanServer.getObjectInstance(new ObjectName("health.ere.ps:type=ReadEPrescriptionsMXBean")));
    }
}