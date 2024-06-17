package health.ere.ps.jmx;

import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import javax.management.openmbean.CompositeData;
import javax.management.openmbean.OpenDataException;

@QuarkusTest
class StatusMXBeanImplTest {
    @Inject
    StatusMXBeanImpl mxBean;

    @Test
    void test() throws OpenDataException {
        CompositeData status = mxBean.getStatus();
        Assertions.assertNotNull(status);
    }
}