package health.ere.ps.websocket;

import java.io.PrintWriter;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.HashSet;
import java.util.Set;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Event;
import javax.enterprise.event.Observes;
import javax.enterprise.event.ObservesAsync;
import javax.inject.Inject;
import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonReader;
import javax.json.bind.Jsonb;
import javax.json.bind.JsonbBuilder;
import javax.json.bind.JsonbConfig;
import javax.websocket.OnClose;
import javax.websocket.OnError;
import javax.websocket.OnMessage;
import javax.websocket.OnOpen;
import javax.websocket.Session;
import javax.websocket.server.ServerEndpoint;

import ca.uhn.fhir.context.FhirContext;
import health.ere.ps.event.BundlesEvent;
import health.ere.ps.event.ERezeptDocumentsEvent;
import health.ere.ps.event.SignAndUploadBundlesEvent;
import health.ere.ps.jsonb.BundleAdapter;
import health.ere.ps.jsonb.ByteAdapter;

@ServerEndpoint("/websocket")
@ApplicationScoped
public class Websocket {

    @Inject
    Event<SignAndUploadBundlesEvent> signAndUploadBundlesEvent;

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
        log.info("Message: " + message);

        JsonReader jsonReader = Json.createReader(new StringReader(message));
        JsonObject object = jsonReader.readObject();
        if("SignAndUploadBundles".equals(object.getString("type"))) {
            SignAndUploadBundlesEvent event = new SignAndUploadBundlesEvent(object);
            signAndUploadBundlesEvent.fireAsync(event);
        }
        jsonReader.close();
    }

    public void onFhirBundle(@ObservesAsync BundlesEvent bundlesEvent) {
        sessions.forEach(s -> {
            s.getAsyncRemote().sendObject(
                    "{\"type\": \"Bundles\", \"payload\": " + generateJson(bundlesEvent) + "}", result -> {
                        if (result.getException() != null) {
                            System.out.println("Unable to send message: " + result.getException());
                        }
                    });
        });
    }

    public void onERezeptDocuments(@ObservesAsync ERezeptDocumentsEvent eRezeptDocumentsEvent) {
        sessions.forEach(s -> {
            s.getAsyncRemote().sendObject(
                    getJsonEventFor(eRezeptDocumentsEvent), result -> {
                        if (result.getException() != null) {
                            System.out.println("Unable to send message: " + result.getException());
                        }
                    });
        });
    }

    public String getJsonEventFor(ERezeptDocumentsEvent eRezeptDocumentsEvent) {
        return "{\"type\": \"ERezeptDocuments\", \"payload\": " + generateJson(eRezeptDocumentsEvent) + "}";
    }

    String generateJson(ERezeptDocumentsEvent eRezeptDocumentsEvent) {
        // Create custom configuration
        JsonbConfig config = new JsonbConfig()
            .setProperty(JsonbConfig.FORMATTING, true)
            .withAdapters(new BundleAdapter())
            .withAdapters(new ByteAdapter());
        Jsonb jsonb = JsonbBuilder.create(config);
        String result = jsonb.toJson(eRezeptDocumentsEvent.eRezeptDocuments);
        return result;
    }

    String generateJson(BundlesEvent bundlesEvent) {
        return bundlesEvent.getBundles().stream().map(bundle -> ctx.newJsonParser().encodeResourceToString(bundle))
                .collect(Collectors.joining(",\n", "[", "]"));
    }

    public void onException(@ObservesAsync Exception exception) {
        sessions.forEach(s -> {

            StringWriter sw = new StringWriter();
            PrintWriter pw = new PrintWriter(sw);
            exception.printStackTrace(pw);

            s.getAsyncRemote()
                    .sendObject("{\"type\": \"Exception\", \"payload\": { \"class\": \""
                            + exception.getClass().getName() + "\", \"message\": \"" + exception.getLocalizedMessage()
                            + "\", \"stacktrace\": \"" + sw.toString().replaceAll("\r?\n", "\\\\n").replaceAll("\t", "\\\\t") + "\"}}", result -> {
                                if (result.getException() != null) {
                                    System.out.println("Unable to send message: " + result.getException());
                                }
                            });
        });
    }

}