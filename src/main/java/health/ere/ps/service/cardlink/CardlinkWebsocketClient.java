package health.ere.ps.service.cardlink;

import java.net.URI;
import java.util.logging.Logger;

import jakarta.websocket.ClientEndpoint;
import jakarta.websocket.CloseReason;
import jakarta.websocket.ContainerProvider;
import jakarta.websocket.OnClose;
import jakarta.websocket.OnMessage;
import jakarta.websocket.OnOpen;
import jakarta.websocket.Session;
import jakarta.websocket.WebSocketContainer;

@ClientEndpoint(configurator = AddJWTConfigurator.class)
public class CardlinkWebsocketClient {

    private static final Logger log = Logger.getLogger(CardlinkWebsocketClient.class.getName());
    
    URI endpointURI;
    Session userSession;

    public CardlinkWebsocketClient() {
    }

    public CardlinkWebsocketClient(URI endpointURI) {
        try {
            this.endpointURI = endpointURI;
            WebSocketContainer container = ContainerProvider.getWebSocketContainer();
            container.connectToServer(this, endpointURI);
        } catch (Exception e) {
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

    /**
     * Send a message.
     *
     * @param message String
     */
    public void sendMessage(String message) {
        log.info(message);
        this.userSession.getAsyncRemote().sendText(message);
    }
}
