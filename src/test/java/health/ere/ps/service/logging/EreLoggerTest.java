package health.ere.ps.service.logging;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import javax.enterprise.event.ObservesAsync;

import health.ere.ps.event.EreLogNotificationEvent;
import io.quarkus.test.junit.QuarkusTest;

@QuarkusTest
class EreLoggerTest {

    private EreLogger logger;

    @BeforeEach
    void init() {
        logger = EreLogger.getLogger(getClass());
    }

    @Test
    void test_Successful_Simple_Log_Message_Write() {
        logger.info("Simple logging test Ok.");
    }

    @Test
    void test_Successful_Simple_Log_Message_Write_With_Context() {
        logger.setLoggingContext(List.of(EreLogger.SystemContext.Connector,
                EreLogger.SystemContext.CardTerminal)).info("Simple " +
                "logging test with context Ok.");
    }

    @Test
    void test_Successful_Simple_Log_Message_Write_With_Context_And_Simple_Log_Message() {
        logger.setLoggingContext(List.of(EreLogger.SystemContext.Connector,
                EreLogger.SystemContext.CardTerminal),
                "SMC-B Card NotPresent")
                .info("Simple logging test with context and simple message Ok.");
    }

    @Test
    void test_Successful_Simple_Log_Message_Write_With_Context_Simple_Log_And_Log_Event_Publishing() {
        logger.setLoggingContext(List.of(EreLogger.SystemContext.Connector,
                EreLogger.SystemContext.CardTerminal),
                "SMC-B Card NotPresent",
                true)
                .info("Simple logging test with context and simple message Ok.");
    }

    void logEventHandler(@ObservesAsync EreLogNotificationEvent ereLogNotificationEvent) {
        System.out.println(ereLogNotificationEvent.toString());
    }
}