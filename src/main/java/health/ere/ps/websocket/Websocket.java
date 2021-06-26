package health.ere.ps.websocket;

import ca.uhn.fhir.context.FhirContext;
import health.ere.ps.event.BundlesEvent;
import health.ere.ps.event.ERezeptDocumentsEvent;
import health.ere.ps.event.erixa.SendToPharmacyEvent;
import health.ere.ps.event.SignAndUploadBundlesEvent;
import health.ere.ps.jsonb.BundleAdapter;
import health.ere.ps.jsonb.ByteAdapter;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Event;
import javax.enterprise.event.ObservesAsync;
import javax.inject.Inject;
import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonReader;
import javax.json.bind.Jsonb;
import javax.json.bind.JsonbBuilder;
import javax.json.bind.JsonbConfig;
import javax.websocket.*;
import javax.websocket.server.ServerEndpoint;
import java.awt.*;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringReader;
import java.io.StringWriter;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashSet;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

@ServerEndpoint("/websocket")
@ApplicationScoped
public class Websocket {

    private static final Logger log = Logger.getLogger(Websocket.class.getName());
    private final FhirContext ctx = FhirContext.forR4();
    private final Set<Session> sessions = new HashSet<>();

    @Inject
    Event<SignAndUploadBundlesEvent> signAndUploadBundlesEvent;

    @Inject
    Event<SendToPharmacyEvent> sendPrescriptionEvent;


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
        log.severe("Websocket error: " + throwable);
    }

    @OnMessage
    public void onMessage(String message) {
        log.info("Message: " + message);

        JsonReader jsonReader = Json.createReader(new StringReader(message));
        JsonObject object = jsonReader.readObject();
        if ("SignAndUploadBundles".equals(object.getString("type"))) {
            SignAndUploadBundlesEvent event = new SignAndUploadBundlesEvent(object);
            signAndUploadBundlesEvent.fireAsync(event);
        } else if ("SendToPharmacy".equals(object.getString("type"))){
            SendToPharmacyEvent event = new SendToPharmacyEvent(object);
            sendPrescriptionEvent.fireAsync(event);
        }
        jsonReader.close();
    }

    public void onFhirBundle(@ObservesAsync BundlesEvent bundlesEvent) {

        // if nobody is connected to the websocket
        if(sessions.size() == 0) {
            if (Desktop.isDesktopSupported() && Desktop.getDesktop().isSupported(Desktop.Action.BROWSE)) {
                try {
                    // Open a browser with the given URL
                    // TODO: build link dynamically
                    Desktop.getDesktop().browse(new URI("http://localhost:8080/frontend/app/src/index.html"));
                    Thread.sleep(5000);
                } catch (IOException | URISyntaxException | InterruptedException e) {
                    log.log(Level.WARNING, "Could not open browser", e);
                }
            }
        }

        sessions.forEach(session -> session.getAsyncRemote().sendObject(
                "{\"type\": \"Bundles\", \"payload\": " + generateJson(bundlesEvent) + "}",
                result -> {
                    if (!result.isOK()) {
                        log.severe("Unable to send bundlesEvent: " + result.getException());
                    }
                }));
    }

    public void onERezeptDocuments(@ObservesAsync ERezeptDocumentsEvent eRezeptDocumentsEvent) {
        sessions.forEach(session -> session.getAsyncRemote().sendObject(
                getJson(eRezeptDocumentsEvent),
                result -> {
                    if (!result.isOK()) {
                        log.severe("Unable to send eRezeptWithDocumentsEvent: " + result.getException());
                    }
                }));
    }

    public String getJson(ERezeptDocumentsEvent eRezeptDocumentsEvent) {
        JsonbConfig customConfig = new JsonbConfig()
                .setProperty(JsonbConfig.FORMATTING, true)
                .withAdapters(new BundleAdapter())
                .withAdapters(new ByteAdapter());
        Jsonb jsonbFactory = JsonbBuilder.create(customConfig);

        return "{\"type\": \"ERezeptWithDocuments\", \"payload\": " +
                jsonbFactory.toJson(eRezeptDocumentsEvent.getERezeptWithDocuments()) + "}";
    }

    String generateJson(BundlesEvent bundlesEvent) {
        return bundlesEvent.getBundles().stream().map(bundle -> ctx.newJsonParser().encodeResourceToString(bundle))
                .collect(Collectors.joining(",\n", "[", "]"));
    }

    public void onException(@ObservesAsync Exception exception) {
        sessions.forEach(session -> {
            StringWriter sw = new StringWriter();
            PrintWriter pw = new PrintWriter(sw);
            exception.printStackTrace(pw);

            session.getAsyncRemote()
                    .sendObject("{\"type\": \"Exception\", \"payload\": { \"class\": \""
                            + exception.getClass().getName() + "\", \"message\": \"" + exception.getLocalizedMessage()
                            + "\", \"stacktrace\": \"" + sw.toString().replaceAll("\r?\n", "\\\\n").replaceAll("\t", "\\\\t") + "\"}}", result -> {
                        if (result.getException() != null) {
                            log.severe("Unable to send message: " + result.getException());
                        }
                    });
        });
    }
}
