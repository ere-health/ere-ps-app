package health.ere.ps.websocket;

import java.io.IOException;
import java.io.StringReader;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.UUID;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.TimeUnit;

import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonObject;
import javax.json.JsonString;
import javax.json.JsonValue;
import javax.websocket.ClientEndpoint;
import javax.websocket.ContainerProvider;
import javax.websocket.OnMessage;
import javax.websocket.OnOpen;
import javax.websocket.Session;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import health.ere.ps.profile.RUTestProfile;
import io.quarkus.test.common.http.TestHTTPResource;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.junit.TestProfile;

@Disabled
@QuarkusTest
@TestProfile(RUTestProfile.class)
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
    public void testWebsocket_GetCards() throws Exception {
        try (Session session = ContainerProvider.getWebSocketContainer().connectToServer(Client.class, uri)) {
            Assertions.assertEquals("CONNECT", MESSAGES.poll(10, TimeUnit.SECONDS));
            session.getAsyncRemote().sendText("{\r\n    \"type\": \"GetCards\",\r\n    \"id\": \"7716aa4a-00e0-47d3-a600-c3b8fd61071b\"\r\n}");
            Thread.sleep(3000);
        }
    }

    @Test
    public void testWebsocket_VerifyPin() throws Exception {
        try (Session session = ContainerProvider.getWebSocketContainer().connectToServer(Client.class, uri)) {
            Assertions.assertEquals("CONNECT", MESSAGES.poll(10, TimeUnit.SECONDS));
            // Send a message to indicate that we are ready,
            // as the message handler may not be registered immediately after this callback.
            String cardHandle = "SMC-B-303";
            session.getAsyncRemote().sendText("{\r\n    \"type\": \"VerifyPin\",\r\n    \"payload\": {\r\n        \"cardHandle\": \""+cardHandle+"\"\r\n    },\r\n    \"id\": \"7716aa4a-00e0-47d3-a600-c3b8fd61071b\"\r\n}");
            Thread.sleep(3000);
        }
    }

    @Test
    public void testWebsocket_UnblockPin() throws Exception {
        try (Session session = ContainerProvider.getWebSocketContainer().connectToServer(Client.class, uri)) {
            Assertions.assertEquals("CONNECT", MESSAGES.poll(10, TimeUnit.SECONDS));
            // Send a message to indicate that we are ready,
            // as the message handler may not be registered immediately after this callback.
            String cardHandle = "SMC-B-303";
            session.getAsyncRemote().sendText("{\r\n    \"type\": \"UnblockPin\",\r\n    \"payload\": {\r\n        \"cardHandle\": \""+cardHandle+"\"\r\n        \"type\": \"SMCB\"\r\n    },\r\n    \"id\": \"7716aa4a-00e0-47d3-a600-c3b8fd61071b\"\r\n}");
            Thread.sleep(3000);
        }
    }

    @Test
    public void testWebsocket_GetPinStatus() throws Exception {
        try (Session session = ContainerProvider.getWebSocketContainer().connectToServer(Client.class, uri)) {
            Assertions.assertEquals("CONNECT", MESSAGES.poll(10, TimeUnit.SECONDS));
            // Send a message to indicate that we are ready,
            // as the message handler may not be registered immediately after this callback.
            String cardHandle = "SMC-B-303";
            session.getAsyncRemote().sendText("{\r\n    \"type\": \"GetPinStatus\",\r\n    \"payload\": {\r\n        \"cardHandle\": \""+cardHandle+"\"\r\n        \"type\": \"SMCB\"\r\n    },\r\n    \"id\": \"7716aa4a-00e0-47d3-a600-c3b8fd61071b\"\r\n}");
            Thread.sleep(3000);
        }
    }

    @ClientEndpoint
    public static class Client {

        @OnOpen
        public void open(Session session) {
            MESSAGES.add("CONNECT");

        }

        @OnMessage
        void message(String message) {
            MESSAGES.add(message);

            System.out.println(message);
            JsonObject jsonObject = Json.createReader(new StringReader(message)).readObject();
            JsonArray ja = jsonObject.getJsonArray("payload");
            for(JsonValue jv : ja) {
                JsonString js = (JsonString) jv;
                try {
                    Files.write(Paths.get("target/"+jsonObject.getString("type")+"-"+UUID.randomUUID().toString()+".html"), js.getString().getBytes());
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

    }
}