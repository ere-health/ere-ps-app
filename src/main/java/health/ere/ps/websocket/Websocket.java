package health.ere.ps.websocket;

import org.jboss.logging.Logger;

import java.awt.*;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringReader;
import java.io.StringWriter;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Event;
import javax.enterprise.event.ObservesAsync;
import javax.inject.Inject;
import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonObject;
import javax.json.JsonReader;
import javax.json.JsonValue;
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
import health.ere.ps.config.AppConfig;
import health.ere.ps.event.BundlesEvent;
import health.ere.ps.event.ERezeptDocumentsEvent;
import health.ere.ps.event.SignAndUploadBundlesEvent;
import health.ere.ps.jsonb.BundleAdapter;
import health.ere.ps.jsonb.ByteAdapter;
import health.ere.ps.validation.fhir.bundle.PrescriptionBundleValidator;

@ServerEndpoint("/websocket")
@ApplicationScoped
public class Websocket {

    @Inject
    Event<SignAndUploadBundlesEvent> signAndUploadBundlesEvent;

    @Inject
    PrescriptionBundleValidator prescriptionBundleValidator;

    @Inject
    AppConfig appConfig;

    private static final Logger log = Logger.getLogger(Websocket.class.getName());
    private final FhirContext ctx = FhirContext.forR4();
    private final Set<Session> sessions = new HashSet<>();

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

        try (JsonReader jsonReader = Json.createReader(new StringReader(message))) {
            JsonObject object = jsonReader.readObject();
            if ("SignAndUploadBundles".equals(object.getString("type"))) {
                if (appConfig.isValidateSignRequestBundles() &&
                        !incomingSignRequestBundlesOK(object)) {
                    log.info("Validation of incoming SignAndUploadBundles payload failed. " +
                            "The following SignAndUploadBundles payload will now be dropped:\n" +
                            message);
                    return;
                }

                SignAndUploadBundlesEvent event = new SignAndUploadBundlesEvent(object);
                signAndUploadBundlesEvent.fireAsync(event);
            }
        }
    }

    public void onFhirBundle(@ObservesAsync BundlesEvent bundlesEvent) {

        // if nobody is connected to the websocket
        if (sessions.size() == 0) {
            if (Desktop.isDesktopSupported() && Desktop.getDesktop().isSupported(Desktop.Action.BROWSE)) {
                try {
                    // Open a browser with the given URL
                    //TODO: Open a Chrome browser
                    // TODO: build link dynamically
                    Desktop.getDesktop().browse(new URI("http://localhost:8080/frontend/app/src/index.html"));
                    Thread.sleep(5000);
                } catch (IOException | URISyntaxException | InterruptedException e) {
                    log.warn("Could not open browser", e);
                }
            }
        }

        sessions.forEach(session -> session.getAsyncRemote().sendObject(
                "{\"type\": \"Bundles\", \"payload\": " + generateJson(bundlesEvent) + "}",
                result -> {
                    if (!result.isOK()) {
                        log.fatal("Unable to send bundlesEvent: " + result.getException());
                    }
                }));
    }

    public void onERezeptDocuments(@ObservesAsync ERezeptDocumentsEvent eRezeptDocumentsEvent) {
        String jsonPayload = getJson(eRezeptDocumentsEvent);
        log.info("Sending prescription receipt payload to front-end: " +
                jsonPayload);

        sessions.forEach(session -> session.getAsyncRemote().sendObject(
                jsonPayload,
                result -> {
                    if (!result.isOK()) {
                        log.fatal("Unable to send eRezeptWithDocumentsEvent: " +
                                result.getException());
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
                            log.fatal("Unable to send message: " + result.getException());
                        }
                    });
        });
    }

    boolean incomingSignRequestBundlesOK(JsonObject bundlePayload) {
        for (JsonValue jsonValue : bundlePayload.getJsonArray("payload")) {
            if (jsonValue instanceof JsonArray) {
                for (JsonValue singleBundle : (JsonArray) jsonValue) {
                    log.info("Now validating incoming sign and upload bundle:\n" +
                            singleBundle.toString());

                    String bundleJson = singleBundle.toString();
                    if (!prescriptionBundleValidator.validateResource(bundleJson,
                            false).isSuccessful()) {
                        log.info("Validation for the following incoming sign and " +
                                "upload bundle failed:\n" + singleBundle);
                        return false;
                    } else {
                        log.info("Validation for the following incoming sign and " +
                                "upload bundle passed:\n" +
                                singleBundle.toString());
                    }
                }
            }
        }

        return true;
    }
}
