package health.ere.ps.websocket;

import java.io.IOException;
import java.net.URI;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.TimeUnit;

import javax.websocket.ClientEndpoint;
import javax.websocket.ContainerProvider;
import javax.websocket.OnMessage;
import javax.websocket.OnOpen;
import javax.websocket.Session;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import io.quarkus.test.common.http.TestHTTPResource;
import io.quarkus.test.junit.QuarkusTest;

@Disabled
@QuarkusTest
public class WebsocketIntegrationTest {

    private static final LinkedBlockingDeque<String> MESSAGES = new LinkedBlockingDeque<>();

    @TestHTTPResource("/websocket")
    URI uri;

    @Test
    public void testWebsocket_FHIR_Bundle() throws Exception {
        try (Session session = ContainerProvider.getWebSocketContainer().connectToServer(Client.class, uri)) {
            Assertions.assertEquals("CONNECT", MESSAGES.poll(10, TimeUnit.SECONDS));
            // Send a message to indicate that we are ready,
            // as the message handler may not be registered immediately after this callback.
            String signAndUploadBundles;
            try {
                signAndUploadBundles = new String(
                        getClass().getResourceAsStream("/websocket-messages/SignAndUploadBundles.json").readAllBytes());
                session.getAsyncRemote().sendText(signAndUploadBundles);
            } catch (IOException e) {
                e.printStackTrace();
            }
            // Assertions.assertEquals(">> stu: hello world", MESSAGES.poll(10, TimeUnit.SECONDS));
        }
    }

    @Test
    public void testWebsocket_VerifyPin() throws Exception {
        try (Session session = ContainerProvider.getWebSocketContainer().connectToServer(Client.class, uri)) {
            Assertions.assertEquals("CONNECT", MESSAGES.poll(10, TimeUnit.SECONDS));
            // Send a message to indicate that we are ready,
            // as the message handler may not be registered immediately after this callback.
            String signAndUploadBundles;
            try {
                signAndUploadBundles = new String(
                        getClass().getResourceAsStream("/websocket-messages/SignAndUploadBundles.json").readAllBytes());
                session.getAsyncRemote().sendText(signAndUploadBundles);
            } catch (IOException e) {
                e.printStackTrace();
            }
            // Assertions.assertEquals(">> stu: hello world", MESSAGES.poll(10, TimeUnit.SECONDS));
        }
    }

    @ClientEndpoint
    public static class Client {

        @OnOpen
        public void open(Session session) {
            MESSAGES.add("CONNECT");

        }

        @OnMessage
        void message(String msg) {
            MESSAGES.add(msg);
        }

    }
}