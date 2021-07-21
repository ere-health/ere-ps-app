package health.ere.ps.websocket;

import java.awt.Desktop;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.io.StringReader;
import java.io.StringWriter;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Event;
import javax.enterprise.event.ObservesAsync;
import javax.inject.Inject;
import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonArrayBuilder;
import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;
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

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.hl7.fhir.r4.model.Bundle;

import ca.uhn.fhir.context.FhirContext;
import health.ere.ps.config.AppConfig;
import health.ere.ps.event.AbortTasksEvent;
import health.ere.ps.event.AbortTasksStatusEvent;
import health.ere.ps.event.BundlesEvent;
import health.ere.ps.event.ERezeptDocumentsEvent;
import health.ere.ps.event.EreLogNotificationEvent;
import health.ere.ps.event.HTMLBundlesEvent;
import health.ere.ps.event.SaveSettingsEvent;
import health.ere.ps.event.SignAndUploadBundlesEvent;
import health.ere.ps.event.erixa.ErixaEvent;
import health.ere.ps.jsonb.BundleAdapter;
import health.ere.ps.jsonb.ByteAdapter;
import health.ere.ps.jsonb.ThrowableAdapter;
import health.ere.ps.model.config.UserConfigurations;
import health.ere.ps.model.websocket.OutgoingPayload;
import health.ere.ps.service.config.UserConfigurationService;
import health.ere.ps.service.fhir.XmlPrescriptionProcessor;
import health.ere.ps.service.fhir.bundle.EreBundle;
import health.ere.ps.service.logging.EreLogger;
import health.ere.ps.validation.fhir.bundle.PrescriptionBundleValidator;

@ServerEndpoint("/websocket")
@ApplicationScoped
public class Websocket {
    @Inject
    Event<SignAndUploadBundlesEvent> signAndUploadBundlesEvent;
    @Inject
    Event<AbortTasksEvent> abortTasksEvent;
    @Inject
    Event<ErixaEvent> erixaEvent;
    @Inject
    Event<SaveSettingsEvent> saveSettingsEvent;
    
    @Inject
    PrescriptionBundleValidator prescriptionBundleValidator;
    @Inject
    AppConfig appConfig;
    @Inject
    UserConfigurationService userConfigurationService;


    JsonbConfig customConfig = new JsonbConfig()
            .setProperty(JsonbConfig.FORMATTING, true)
            .withAdapters(new BundleAdapter())
            .withAdapters(new ByteAdapter())
            .withAdapters(new ThrowableAdapter());
    Jsonb jsonbFactory = JsonbBuilder.create(customConfig);
    private static final String CHROME_X86_PATH = "C:\\Program Files (x86)\\Google\\Chrome\\Application\\chrome.exe";
    private static final String CHROME_X64_PATH = "C:\\Program Files\\Google\\Chrome\\Application\\chrome.exe";
    private static final EreLogger ereLog = EreLogger.getLogger(Websocket.class);
    private static final java.util.List<EreLogger.SystemContext> bundleValidationSysLogCtxList =
            java.util.List.of(EreLogger.SystemContext.KbvBundlesProcessing,
                    EreLogger.SystemContext.KbvBundlesSigning,
                    EreLogger.SystemContext.KbvBundleValidation);
    private final FhirContext ctx = FhirContext.forR4();
    private final Set<Session> sessions = new HashSet<>();

    ObjectMapper objectMapper = new ObjectMapper();

    @OnOpen
    public void onOpen(Session session) {
        sessions.add(session);
        ereLog.info("Websocket opened");
    }

    void sendAllKBVExamples() {
        sessions.forEach(session -> {

            try (Stream<Path> paths = Files.walk(Paths.get("../src/test/resources/simplifier_erezept"))) {
                paths
                        .filter(Files::isRegularFile)
                        .forEach(f -> {
                            try (InputStream inputStream = new FileInputStream(f.toFile())) {
                                String xml = new String(inputStream.readAllBytes(), "UTF-8").replaceAll("<!--.*-->", "");
                                Bundle bundle = ctx.newXmlParser().parseResource(Bundle.class, xml);
                                bundle.setId(UUID.randomUUID().toString());
                                onFhirBundle(new BundlesEvent(Collections.singletonList(bundle)));
                            } catch (IOException ex) {
                                ereLog.warn("Could read all files", ex);
                            }
                        });
            } catch (IOException ex) {
                ereLog.warn("Could read all files", ex);
            }
        });
    }

    @OnClose
    public void onClose(Session session) {
        sessions.remove(session);
        ereLog.info("Websocket closed");
    }

    @OnError
    public void onError(Session session, Throwable throwable) {
        sessions.remove(session);

        throwable.printStackTrace();

        ereLog.info("Websocket error: " + throwable);
    }

    @OnMessage
    public void onMessage(String message) {
        ereLog.info("Message: " + message);

        try (JsonReader jsonReader = Json.createReader(new StringReader(message))) {
            JsonObject object = jsonReader.readObject();
            if ("SignAndUploadBundles".equals(object.getString("type"))) {
                JsonObject bundlesValidationResultMessage = bundlesValidationResult(object);
                
                boolean bundlesValid = true;
                bundlesValid = bundlesValidationResultMessage.getJsonArray("payload")
                .stream().filter(jo -> jo instanceof JsonObject)
                    .map(jo -> ((JsonObject) jo).getBoolean("valid"))
                    .filter(b -> !b)
                    .count() == 0;
                if(bundlesValid || object.getBoolean("ignoreValidation", false)) {
                    SignAndUploadBundlesEvent event = new SignAndUploadBundlesEvent(object);
                    signAndUploadBundlesEvent.fireAsync(event);
                } else {
                    sessions.forEach(session -> session.getAsyncRemote().sendObject(
                        bundlesValidationResultMessage.toString(),
                        result -> {
                            if (!result.isOK()) {
                                ereLog.fatal("Unable to sent bundlesValidationResult event: " + result.getException());
                            }
                        }));
                }
            } else if ("XMLBundle".equals(object.getString("type"))) {
                Bundle[] bundles = XmlPrescriptionProcessor.parseFromString(object.getString("payload"));
                onFhirBundle(new BundlesEvent(Arrays.asList(bundles)));
            } else if ("AbortTasks".equals(object.getString("type"))) {
                abortTasksEvent.fireAsync(new AbortTasksEvent(object.getJsonArray("payload")));
            } else if ("ErixaEvent".equals(object.getString("type"))) {
                ErixaEvent event = new ErixaEvent(object);
                erixaEvent.fireAsync(event);
            } else if ("RequestSettings".equals(object.getString("type"))) {
                UserConfigurations userConfigurations = userConfigurationService.getConfig();
                String payload = jsonbFactory.toJson(userConfigurations);
                sessions.forEach(session -> session.getAsyncRemote().sendObject(
                    "{\"type\": \"Settings\", \"payload\": " + payload + "}",
                    result -> {
                        if (!result.isOK()) {
                            ereLog.fatal("Unable to sent settings event: " + result.getException());
                        }
                    }));
            } else if("SaveSettings".equals(object.getString("type"))) {
                String userConfiguration = object.getJsonObject("payload").toString();
                UserConfigurations userConfigurations = jsonbFactory.fromJson(userConfiguration, UserConfigurations.class);
                saveSettingsEvent.fireAsync(new SaveSettingsEvent(userConfigurations));
            } else if ("Publish".equals(object.getString("type"))) {
                sessions.forEach(session -> session.getAsyncRemote().sendObject(
                        object.getString("payload"),
                        result -> {
                            if (!result.isOK()) {
                                ereLog.fatal("Unable to publish event: " + result.getException());
                            }
                        }));
            } else if ("AllKBVExamples".equals(object.getString("type"))) {
                sendAllKBVExamples();
            }
        }
    }

    public void onFhirBundle(@ObservesAsync BundlesEvent bundlesEvent) {
        assureChromeIsOpen();
        String bundlesString = generateJson(bundlesEvent);
        sessions.forEach(session -> session.getAsyncRemote().sendObject(
                "{\"type\": \"Bundles\", \"payload\": " + bundlesString + "}",
                result -> {
                    if (!result.isOK()) {
                        ereLog.fatal("Unable to send bundlesEvent: " + result.getException());
                    }
                }));
    }

    public void onAbortTasksStatusEvent(@ObservesAsync AbortTasksStatusEvent abortTasksStatusEvent) {
        assureChromeIsOpen();
        String abortTasksStatusString = generateJson(abortTasksStatusEvent);
        sessions.forEach(session -> session.getAsyncRemote().sendObject(
                "{\"type\": \"AbortTasksStatus\", \"payload\": " + abortTasksStatusString + "}",
                result -> {
                    if (!result.isOK()) {
                        ereLog.fatal("Unable to send bundlesEvent: " + result.getException());
                    }
                }));
    }

    String generateJson(AbortTasksStatusEvent abortTasksStatusEvent) {
        return jsonbFactory.toJson(abortTasksStatusEvent.getTasks());
    }

    void assureChromeIsOpen() {
        // if nobody is connected to the websocket
        if (sessions.size() == 0) {
            try {
                startWebappInChrome();
                Thread.sleep(5000);
            } catch (InterruptedException e) {
                ereLog.warn("Could not open browser", e);
            }
        }
    }

    public void onERezeptDocuments(@ObservesAsync ERezeptDocumentsEvent eRezeptDocumentsEvent) {
        String jsonPayload = generateJson(eRezeptDocumentsEvent);
        ereLog.info("Sending prescription receipt payload to front-end: " +
                jsonPayload);

        sessions.forEach(session -> session.getAsyncRemote().sendObject(
                jsonPayload,
                result -> {
                    if (!result.isOK()) {
                        ereLog.fatal("Unable to send eRezeptWithDocumentsEvent: " +
                                result.getException());
                    }
                }));
    }

    public String generateJson(ERezeptDocumentsEvent eRezeptDocumentsEvent) {
        return "{\"type\": \"ERezeptWithDocuments\", \"payload\": " +
                jsonbFactory.toJson(eRezeptDocumentsEvent.getERezeptWithDocuments()) + "}";
    }

    String generateJson(BundlesEvent bundlesEvent) {

        bundlesEvent.getBundles().forEach(bundle -> {
            if (bundle instanceof EreBundle) {
                ereLog.info("Filled bundle json template result shown below. Null value place" +
                        " holders present.");
                ereLog.info("==============================================");

                ereLog.info(((EreBundle) bundle).encodeToJson());
            }
        });

        if (bundlesEvent.getBundles().stream().anyMatch(b -> b instanceof EreBundle)) {
            return bundlesEvent.getBundles().stream().map(bundle ->
                    ((EreBundle) bundle).encodeToJson())
                    .collect(Collectors.joining(",\n", "[", "]"));
        } else {
            return bundlesEvent.getBundles().stream().map(bundle ->
                    ctx.newJsonParser().encodeResourceToString(bundle))
                    .collect(Collectors.joining(",\n", "[", "]"));
        }
    }

    public void onException(@ObservesAsync Exception exception) {
        sessions.forEach(session -> {
            StringWriter sw = new StringWriter();
            PrintWriter pw = new PrintWriter(sw);
            exception.printStackTrace(pw);

            String localizedMessage;
            try {
                localizedMessage = objectMapper.writeValueAsString(exception.getLocalizedMessage());
                
                String stackTrace = objectMapper.writeValueAsString(sw.toString());
                session.getAsyncRemote()
                    .sendObject("{\"type\": \"Exception\", \"payload\": { \"class\": \""
                            + exception.getClass().getName() + "\", \"message\": " + localizedMessage
                            + ", \"stacktrace\": " + stackTrace + "}}", result -> {
                        if (result.getException() != null) {
                            ereLog.fatal("Unable to send message: " + result.getException());
                        }
                    });
            } catch (JsonProcessingException e) {
                ereLog.error("Could not generate json", e);
            }
                
        });
    }

    public void onEreLogNotificationEvent(@ObservesAsync EreLogNotificationEvent event) {
        sessions.forEach(session -> {
            OutgoingPayload<EreLogNotificationEvent> outgoingPayload = new OutgoingPayload(event);

            outgoingPayload.setType("Notification");

            session.getAsyncRemote()
                    .sendObject(outgoingPayload.toString(), result -> {
                        if (result.getException() != null) {
                            ereLog.fatal("Unable to send message: " + result.getException());
                        }
                    });
        });
    }

    
    public void onHTMLBundlesEvent(@ObservesAsync HTMLBundlesEvent event) {
        sessions.forEach(session -> {
        
            session.getAsyncRemote()
                    .sendObject("{\"type\": \"HTMLBundles\", \"payload\": " +
                    jsonbFactory.toJson(event.getBundles()) + "}", result -> {
                        if (result.getException() != null) {
                            ereLog.fatal("Unable to send message: " + result.getException());
                        }
                    });
        });
    }

    JsonObject bundlesValidationResult(JsonObject bundlePayload) {
        JsonObjectBuilder builder = Json.createObjectBuilder();
        builder.add("type", "BundlesValidationResult");
        JsonArrayBuilder payload = Json.createArrayBuilder();
        for (JsonValue jsonValue : bundlePayload.getJsonArray("payload")) {
            if (jsonValue instanceof JsonArray) {
                for (JsonValue singleBundle : (JsonArray) jsonValue) {
                    JsonObjectBuilder singleBundleResults = Json.createObjectBuilder();
                    ereLog.info("Now validating incoming sign and upload bundle:\n" +
                    singleBundle.toString());
                    
                    String bundleJson = singleBundle.toString();
                    List<String> errorsList = new ArrayList<>(1);

                    if (!prescriptionBundleValidator.validateResource(bundleJson,
                    true, errorsList).isSuccessful()) {
                        JsonArrayBuilder errorsJson = Json.createArrayBuilder();
                        errorsList.stream().forEach(s -> errorsJson.add(s));
                        singleBundleResults.add("errors", errorsJson);
                        singleBundleResults.add("valid", false);
                    } else {
                        singleBundleResults.add("valid", true);
                        ereLog.info("Validation for the following incoming sign and " +
                        "upload bundle passed:\n" +
                        singleBundle.toString());
                    }
                    payload.add(singleBundleResults);
                }
            }
        }
        builder.add("payload", payload);
        return builder.build();
    }

    private void startWebappInChrome() {
        try {
            if (Files.exists(Path.of(CHROME_X86_PATH))) {
                Runtime.getRuntime().exec(CHROME_X86_PATH + " http://localhost:8080/frontend/app/src/index.html");
            } else if (Files.exists(Path.of(CHROME_X64_PATH))) {
                Runtime.getRuntime().exec(CHROME_X64_PATH + " http://localhost:8080/frontend/app/src/index.html");
            } else {
                ereLog.warn("Could not start the webapp on Chrome as no Chrome was detected");
                // If you're not on Windows but have Chrome as a default browser
                if (Desktop.isDesktopSupported() && Desktop.getDesktop().isSupported(Desktop.Action.BROWSE)) {
                    Desktop.getDesktop().browse(new URI("http://localhost:8080/frontend/app/src/index.html"));
                }
            }
        } catch (IOException | URISyntaxException e) {
            ereLog.error("There was a problem when opening the browser:");
            e.printStackTrace();
        }
    }
}
