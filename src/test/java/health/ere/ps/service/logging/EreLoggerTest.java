package health.ere.ps.service.logging;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Date;
import java.util.List;

import io.quarkus.test.junit.QuarkusTest;

@QuarkusTest
class EreLoggerTest {

    private static final EreLogger logger = EreLogger.getLogger(EreLoggerTest.class);

    @Test
    void test_Successful_Message_Logging() {
        logger.info("Simple logging test Ok.");
    }

    @Test
    void test_Successful_Formatted_Message_Logging() {
        logger.infof("Simple logging test %d Ok.", 1);
    }

    @Test
    void test_Successful_Indexed_Formatted_Message_Logging() {
        logger.infov("Simple {1} logger test on {0} Ok.", new Date(), "Ere");
    }

    @Test
    void test_Successful_Message_Logging_With_Context() {
        logger.setLoggingContext(List.of(EreLogger.SystemContext.Connector,
                EreLogger.SystemContext.CardTerminal)).info("Simple " +
                "logging test with context Ok.");
    }

    @Test
    void test_Successful_Message_Logging_With_Context_And_Simple_Log_Message() {
        logger.setLoggingContext(List.of(EreLogger.SystemContext.Connector,
                EreLogger.SystemContext.CardTerminal),
                "SMC-B Card Not Present")
                .info("Simple logging test with context and simple message Ok.");
    }

    @Test
    void test_Successful_Message_Logging_With_Context_And_Simple_Log_Message_And_Log_Event_Publishing() {
        logger.setLoggingContext(List.of(EreLogger.SystemContext.Connector,
                EreLogger.SystemContext.CardTerminal),
                "SMC-B Card Not Present",
                true)
                .warn("Warning! SMC-B Card Not Detected.");
    }

    @Test
    void test_Successful_Formatted_Message_Logging_With_Context_And_Simple_Log_Message_And_Log_Event_Publishing() {
        logger.setLoggingContext(List.of(EreLogger.SystemContext.Connector,
                EreLogger.SystemContext.CardTerminal),
                "SMC-B Card Not Present",
                true)
                .errorf("Simple logging test with context, simple message and event publishing " +
                        "for end user %s with patient %s",
                        "Dr. Ray Larsen", "Marion Gruber.");
    }

    @Test
    void test_Successful_Indexed_Formatted_Message_Logging_With_Context_and_Simple_Log_Message_And_Log_Event_Publishing() {
        logger.setLoggingContext(List.of(EreLogger.SystemContext.Connector,
                EreLogger.SystemContext.CardTerminal),
                "SMC-B Card NotPresent",
                true)
                .errorv("Simple logging test with context, simple message and event publishing " +
                                "for end user {0} with patient {1}",
                        "Dr. Ray Larsen", "Marion Gruber.");
    }

    @Test
    void test_Successful_Formatted_Message_Logging_With_Context_And_Simple_Log_Message_And_Log_Details_List_And_Log_Event_Publishing() {
        logger.setLoggingContext(List.of(EreLogger.SystemContext.KbvBundlesProcessing,
                EreLogger.SystemContext.KbvBundleValidation),
                "Bundle validation failed!",
                true)
                .setLogDetails(List.of("Validation error 1", "Validation error 2"))
                .errorf("Error trying to validate prescription sign request " +
                                "for end user %s with patient %s",
                        "Dr. Ray Larsen", "Marion Gruber.");
    }

    /**
     * This and similar tests shows that if the log details is manually set via the
     * {@link EreLogger#setLogDetails(List)} method call while an exception object is logged, the
     * manually set log details will override the logged exception object and its contents
     * will appear in the {@link EreLogNotificationEventHandler} object's logMessageDetails[]
     * list instead of the exception object's message.
     *
     * This functionality is used to substitute end user friendly notification message details
     * in the front-end UI for cryptic exception stack traces.
     *
     * The exception stack trace details will still appear in the log file instead of the
     * manually set log details if an exception object is being logged.
     */
    @Test
    void test_Successful_Formatted_Message_Logging_With_Exception_Object_And_Context_And_Simple_Log_Message_And_Log_Details_List_And_Log_Event_Publishing() {
        logger.setLoggingContext(List.of(EreLogger.SystemContext.GematikEprescriptionService,
                EreLogger.SystemContext.GematikIdp),
                "Cannot connect to Gematik Idp and ePrescription Service!",
                true)
                .setLogDetails(List.of("Cannot connect to Gematik Idp", "Cannot connect to " +
                        "Gematik ePrescription Specialist Service."))
                .fatalf(new Throwable("Cannot connect to Gematik TI!"), "Unable to process " +
                                "e-prescription request for end user %s with patient %s",
                        "Dr. Ray Larsen", "Marion Gruber.");
    }

    @Test
    void test_Successful_Formatted_Message_Logging_With_Exception_And_Context_And_Simple_Log_Message_And_Exception_Object_And_Log_Event_Publishing() {
        try {
           throw new Throwable("Test exception thrown.");
        } catch(Throwable e) {
            logger.setLoggingContext(List.of(EreLogger.SystemContext.GematikEprescriptionService,
                    EreLogger.SystemContext.GematikIdp),
                    "Cannot connect to Gematik Idp and ePrescription Service!",
                    true)
                    .fatalf(e, "Unable to process " +
                                    "e-prescription request for end user %s with patient %s",
                            "Dr. Ray Larsen", "Marion Gruber.");
        }
    }
}