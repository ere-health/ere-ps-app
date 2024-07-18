package health.ere.ps.service.cardlink;

import health.ere.ps.service.health.check.CardlinkWebsocketCheck;
import jakarta.json.Json;
import jakarta.json.JsonArray;
import jakarta.json.JsonArrayBuilder;
import jakarta.json.JsonObject;
import jakarta.json.JsonObjectBuilder;
import jakarta.json.JsonValue;
import jakarta.websocket.ClientEndpoint;
import jakarta.websocket.CloseReason;
import jakarta.websocket.ContainerProvider;
import jakarta.websocket.DeploymentException;
import jakarta.websocket.OnClose;
import jakarta.websocket.OnMessage;
import jakarta.websocket.OnOpen;
import jakarta.websocket.Session;
import jakarta.websocket.WebSocketContainer;
import jakarta.xml.bind.DatatypeConverter;

import java.io.IOException;
import java.net.URI;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

@SuppressWarnings("unused")
@ClientEndpoint(configurator = AddJWTConfigurator.class)
public class CardlinkWebsocketClient {

    private static final Logger log = Logger.getLogger(CardlinkWebsocketClient.class.getName());
    
    URI endpointURI;
    Session userSession;
    WebSocketContainer container;

    private CardlinkWebsocketCheck cardlinkWebsocketCheck;

    public CardlinkWebsocketClient() {
    }

    public CardlinkWebsocketClient(URI endpointURI, CardlinkWebsocketCheck cardlinkWebsocketCheck) {
        try {
            this.cardlinkWebsocketCheck = cardlinkWebsocketCheck;
            this.cardlinkWebsocketCheck.setConnected(false);
            this.endpointURI = endpointURI;
            container = ContainerProvider.getWebSocketContainer();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public void connect() {
        try {
            container.connectToServer(this, endpointURI);
        } catch (DeploymentException | IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Callback hook for Connection open events.
     *
     * @param userSession the userSession which is opened.
     */
    @OnOpen
    public void onOpen(Session userSession) {
        log.info("opening websocket to "+endpointURI);
        this.userSession = userSession;
        cardlinkWebsocketCheck.setConnected(true);
    }

    /**
     * Callback hook for Connection close events.
     *
     * @param userSession the userSession which is getting closed.
     * @param reason the reason for connection close
     */
    @OnClose
    public void onClose(Session userSession, CloseReason reason) {
        log.info("closing websocket "+endpointURI);
        cardlinkWebsocketCheck.setConnected(false);
    }

    /**
     * Callback hook for Message Events. This method will be invoked when a client send a message.
     *
     * @param message The text message
     */
    @OnMessage
    public void onMessage(String message) {
        log.info(message);
    }

    public void sendJson(String correlationId, String type, Map<String, Object> payloadMap) {
        JsonObjectBuilder objectBuilder = Json.createObjectBuilder().add("type", type);
        if (!payloadMap.isEmpty()) {
            JsonObjectBuilder builder = Json.createObjectBuilder();
            for (Map.Entry<String, ?> entry : payloadMap.entrySet()) {
                if (entry.getValue() instanceof Integer) {
                    builder.add(entry.getKey(), (Integer) entry.getValue());
                } else if (entry.getValue() instanceof Long) {
                    builder.add(entry.getKey(), (Long) entry.getValue());
                } else if (entry.getValue() instanceof String) {
                    builder.add(entry.getKey(), (String) entry.getValue());
                } else if (entry.getValue() instanceof JsonArrayBuilder) {
                    builder.add(entry.getKey(), (JsonArrayBuilder) entry.getValue());
                }
            }
            String payload = builder.build().toString();
            objectBuilder.add("payload", DatatypeConverter.printBase64Binary(payload.getBytes()));
        }
        JsonObject jsonObject = objectBuilder.build();
        JsonArray jsonArray = Json.createArrayBuilder()
            .add(jsonObject)
            .add(JsonValue.NULL)
            .add(correlationId)
            .build();
        sendMessage(jsonArray.toString(), correlationId);
    }

    /**
     * Send a message.
     *
     * @param message String
     */
    public void sendMessage(String message, String correlationId) {
        try {
            this.userSession.getBasicRemote().sendText(message);
            log.info(String.format("[%s] WS message is sent to cardlink: %s", correlationId, message));
        } catch (Throwable e) {
            log.log(Level.WARNING, String.format("[%s] Could not send WS message to cardlink: %s", correlationId, message), e);
        }

        // TODO

        /*
        Jun 15 21:39:16 manuel-System-Product-Name promtail[275518]: level=error ts=2024-06-15T19:39:16.292577252Z
        caller=client.go:430 component=client host=localhost:3100 msg="final error sending batch" status=400
        tenant= error="server returned HTTP status 400 Bad Request (400): stream '
        {err=\"[uuid] Could not send WS message to cardlink: [{\\\"type\\\":\\\"eRezeptTokensFromAVS\\\",\\\"payload\\\":\\\"eyJjdElkIjoiZTdhOGEwOTUtMTYxMy00YmZmLWE3MTQtMjEyYjcyYWRmZGFkIiwidG9rZW5zIjoiRVJST1I6IEZlaGxlciBiZWkgZGVyIEMyQy1BdXRoZW50aXNpZXJ1bmcgLT4gZGUuZ2VtYXRpay53cy5jb25uLnZzZHMudnNkc2VydmljZS52NS5GYXVsdE1lc3NhZ2U6IEZlaGxlciBiZWkgZGVyIEMyQy1BdXRoZW50aXNpZXJ1bmdcblx0YXQgamF2YS5iYXNlL2pkay5pbnRlcm5hbC5yZWZsZWN0LkRpcmVjdENvbnN0cnVjdG9ySGFuZGxlQWNjZXNzb3IubmV3SW5zdGFuY2UoRGlyZWN0Q29uc3RydWN0b3JIYW5kbGVBY2Nlc3Nvci5qYXZhOjYyKVxuXHRhdCBqYXZhLmJhc2UvamF2YS5sYW5nLnJlZmxlY3QuQ29uc3RydWN0b3IubmV3SW5zdGFuY2VXaXRoQ2FsbGVyKENvbnN0cnVjdG9yLmphdmE6NTAyKVxuXHRhdCBqYXZhLmJhc2UvamF2YS5sYW5nLnJlZmxlY3QuQ29uc3RydWN0b3IubmV3SW5zdGFuY2UoQ29uc3RydWN0b3IuamF2YTo0ODYpXG5cdGF0IGNvbS5zdW4ueG1sLndzLmZhdWx0LlNPQVBGYXVsdEJ1aWxkZXIuY3JlYXRlRXhjZXB0aW9uKFNPQVBGYXVsdEJ1aWxkZXIuamF2YToxMjMpXG5cdGF0IGNvbS5zdW4ueG1sLndzLmNsaWVudC5zZWkuU3R1YkhhbmRsZXIucmVhZFJlc3BvbnNlKFN0dWJIYW5kbGVyLmphdmE6MjI1KVxuXHRhdCBjb20uc3VuLnhtbC53cy5kYi5EYXRhYmluZGluZ0ltcG"
         */
    }

    public void close() {
        try {
            userSession.close();
        } catch (IOException e) {
            log.log(Level.WARNING, "Could not close websocket session", e);
        }
    }
}
