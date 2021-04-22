package health.ere.ps.websocket;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Logger;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Observes;
import javax.websocket.OnClose;
import javax.websocket.OnError;
import javax.websocket.OnMessage;
import javax.websocket.OnOpen;
import javax.websocket.server.PathParam;
import javax.websocket.server.ServerEndpoint;

import ca.uhn.fhir.context.FhirContext;
import health.ere.ps.event.BundleEvent;

import javax.websocket.Session;

@ServerEndpoint("/websocket")
@ApplicationScoped
public class Websocket {

    // Create a FHIR context
	FhirContext ctx = FhirContext.forR4();

    private static Logger log = Logger.getLogger(Websocket.class.getName());
    Set<Session> sessions = new HashSet<>(); 

    @OnOpen
    public void onOpen(Session session) {
        sessions.add(session);
        log.info("Websocket opened");
    }

    @OnClose
    public void onClose(Session session) {
        sessions.remove(session);
        log.info("Websocket closed");
    }

    @OnError
    public void onError(Session session, Throwable throwable) {
        sessions.remove(session);
        log.info("Websocket error: " + throwable);
    }

    @OnMessage
    public void onMessage(String message) {
        log.info("Message: "+message);
    }

    public void onFhirBundle(@Observes BundleEvent bundleEvent) {
        sessions.forEach(s -> {
            s.getAsyncRemote().sendObject("{\"type\": \"Bundle\", \"payload\": "+
                ctx.newJsonParser().encodeResourceToString(bundleEvent.getBundle())+"}", result ->  {
                if (result.getException() != null) {
                    System.out.println("Unable to send message: " + result.getException());
                }
            });
        });
    }

}