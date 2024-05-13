package health.ere.ps.websocket;

import java.io.IOException;
import java.io.StringReader;
import java.net.URI;
import java.util.List;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.TimeUnit;

import jakarta.json.Json;
import jakarta.json.JsonArray;
import jakarta.json.JsonObject;
import jakarta.json.JsonReader;
import jakarta.websocket.ClientEndpoint;
import jakarta.websocket.ContainerProvider;
import jakarta.websocket.DeploymentException;
import jakarta.websocket.OnMessage;
import jakarta.websocket.OnOpen;
import jakarta.websocket.Session;

import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import health.ere.ps.service.logging.EreLogger;
import io.quarkus.test.common.http.TestHTTPResource;
import io.quarkus.test.junit.QuarkusTest;

@Disabled("This test works consistently when tested on its own. However, there seems to be some " +
        "port conflicts affecting its consistent positive execution when run with other tests. " +
        "Some other tests including this test may be using the same port which is probably not " +
        "being closed properly or in time for this test to run successfully on a consistent basis" +
        " when run with other similar port accessing tests. This needs to be investigated.")
@QuarkusTest
public class WebsocketClientNotificationTest {

    @TestHTTPResource("/websocket")
    URI uri;

    private static final LinkedBlockingDeque<String> MESSAGES = new LinkedBlockingDeque<>();
    private static final String OK_TEST_MESSAGE = "Notification received OK";
    private static final String SIMPLE_LOG_MSG = "Test simple log message";
    private static final String LOG_MSG = "This is a log notification test.";
    private static final List<EreLogger.SystemContext> CTX_LIST = List.of(
            EreLogger.SystemContext.KbvBundlesProcessing,
            EreLogger.SystemContext.KbvBundleValidation);
    private static final EreLogger ereLogger =
            EreLogger.getLogger(WebsocketClientNotificationTest.class);
    private static final int MAX_RESPONSE_WAIT_TIME_IN_SECONDS = 60;

    @Test
    public void test_Successful_Sending_Of_Backend_Notification() {
        try (Session session = ContainerProvider.getWebSocketContainer().connectToServer(
                Client.class, uri)) {
            Assertions.assertEquals("CONNECT",
                    MESSAGES.poll(MAX_RESPONSE_WAIT_TIME_IN_SECONDS, TimeUnit.SECONDS));

            // Trigger log notification event submission to FE Client simulator.
            ereLogger.setLoggingContext(CTX_LIST, SIMPLE_LOG_MSG, true)
                    .info(LOG_MSG);

            ereLogger.infov("Log notification event was published for simple log message " +
                    "[{0}] and context list [{1}]", SIMPLE_LOG_MSG, CTX_LIST);

            // Await expected positive receipt confirmation of notification event payload from FE
            // client simulator.
            Assertions.assertEquals(OK_TEST_MESSAGE,
                    StringUtils.defaultString(MESSAGES.poll(MAX_RESPONSE_WAIT_TIME_IN_SECONDS,
                    TimeUnit.SECONDS)));

            ereLogger.info("Test run completed!");
        } catch (InterruptedException | DeploymentException | IOException e) {
            ereLogger.error("Exception occurred during test!", e);
        }
    }

    @ClientEndpoint
    public static class Client {

        @OnOpen
        public void open(Session session) {
            MESSAGES.add("CONNECT");
        }

        @OnMessage
        void message(String jsonString) {
            ereLogger.infof("Payload received is:\n %s", jsonString);

            if (StringUtils.isNotBlank(jsonString)) {
                try (JsonReader jsonReader = Json.createReader(new StringReader(jsonString))) {
                    JsonObject jsonObject = jsonReader.readObject();

                    String payloadType = StringUtils.defaultString(
                            jsonObject.getString("type"));
                    JsonObject payloadJson = jsonObject.getJsonObject("payload");

                    JsonArray jsonArray = payloadJson.getJsonArray("systemContextList");

                    String ctx1 = jsonArray != null && jsonArray.size() == 2 ?
                            jsonArray.getString(0) : "";
                    String ctx2 = jsonArray != null && jsonArray.size() == 2 ?
                            jsonArray.getString(1) : "";
                    String simpleLogMessage = StringUtils.defaultString(payloadJson.getString(
                            "simpleLogMessage"));
                    String status = StringUtils.defaultString(
                            payloadJson.getString("status"));
                    String logMessage = StringUtils.defaultString(payloadJson.getString(
                            "logMessage"));

                    List<String> ctxList = List.of(
                            EreLogger.SystemContext.KbvBundlesProcessing.getSysContext(),
                            EreLogger.SystemContext.KbvBundleValidation.getSysContext());

                    if (payloadType.equals("Notification") &&
                            ctxList.contains(StringUtils.defaultString(ctx1)) &&
                            ctxList.contains(StringUtils.defaultString(ctx2)) &&
                            simpleLogMessage.equals(SIMPLE_LOG_MSG) && status.equals("INFO") &&
                            logMessage.equals(LOG_MSG)) {
                        MESSAGES.add(OK_TEST_MESSAGE);
                    } else {
                        MESSAGES.add("Did not receive expected notification");
                    }
                }
            }
        }
    }
}
