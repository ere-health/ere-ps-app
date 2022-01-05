package health.ere.ps.websocket;

import java.awt.Desktop;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.math.BigInteger;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Event;
import javax.enterprise.event.ObservesAsync;
import javax.enterprise.inject.Instance;
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

import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.hl7.fhir.r4.model.Bundle;

import ca.uhn.fhir.context.FhirContext;
import de.gematik.ws.conn.cardservice.wsdl.v8.FaultMessage;
import de.gematik.ws.tel.error.v2.Error;
import de.gematik.ws.tel.error.v2.Error.Trace;
import health.ere.ps.config.AppConfig;
import health.ere.ps.event.AbortTasksEvent;
import health.ere.ps.event.AbortTasksStatusEvent;
import health.ere.ps.event.ActivateComfortSignatureEvent;
import health.ere.ps.event.BundlesEvent;
import health.ere.ps.event.ChangePinEvent;
import health.ere.ps.event.ChangePinResponseEvent;
import health.ere.ps.event.DeactivateComfortSignatureEvent;
import health.ere.ps.event.ERezeptWithDocumentsEvent;
import health.ere.ps.event.EreLogNotificationEvent;
import health.ere.ps.event.GetCardsEvent;
import health.ere.ps.event.GetCardsResponseEvent;
import health.ere.ps.event.GetSignatureModeEvent;
import health.ere.ps.event.GetSignatureModeResponseEvent;
import health.ere.ps.event.HTMLBundlesEvent;
import health.ere.ps.event.ReadyToSignBundlesEvent;
import health.ere.ps.event.SaveSettingsEvent;
import health.ere.ps.event.SignAndUploadBundlesEvent;
import health.ere.ps.event.VerifyPinEvent;
import health.ere.ps.event.erixa.ErixaEvent;
import health.ere.ps.jsonb.BundleAdapter;
import health.ere.ps.jsonb.ByteAdapter;
import health.ere.ps.jsonb.DurationAdapter;
import health.ere.ps.jsonb.ThrowableAdapter;
import health.ere.ps.model.config.UserConfigurations;
import health.ere.ps.model.websocket.OutgoingPayload;
import health.ere.ps.service.config.UserConfigurationService;
import health.ere.ps.service.fhir.XmlPrescriptionProcessor;
import health.ere.ps.service.fhir.bundle.EreBundle;
import health.ere.ps.service.logging.EreLogger;
import health.ere.ps.validation.fhir.bundle.PrescriptionBundleValidator;
import message.processor.incoming.IncomingBundleMessageProcessor;
import message.processor.incoming.IncomingMessageProcessor;
import message.processor.outgoing.OutgoingMessageProcessor;

@ServerEndpoint("/websocket")
@ApplicationScoped
public class Websocket {
    @Inject
    Event<SignAndUploadBundlesEvent> signAndUploadBundlesEvent;
    @Inject
    Event<ReadyToSignBundlesEvent> readyToSignBundlesEvent;
    @Inject
    Event<AbortTasksEvent> abortTasksEvent;
    @Inject
    Event<ErixaEvent> erixaEvent;
    @Inject
    Event<SaveSettingsEvent> saveSettingsEvent;

    @Inject
    Instance<IncomingMessageProcessor> messageProcessors;

    @Inject
    Instance<OutgoingMessageProcessor> outgoingMessageProcessors;

    @Inject
    Event<ActivateComfortSignatureEvent> activateComfortSignatureEvent;
    @Inject
    Event<DeactivateComfortSignatureEvent> deactivateComfortSignatureEvent;
    @Inject
    Event<GetSignatureModeEvent> getSignatureModeEvent;

    @Inject
    Event<GetCardsEvent> getCardsEvent;

    @Inject
    Event<ChangePinEvent> changePinEvent;
    
    @Inject
    Event<VerifyPinEvent> verifyPinEvent;
    
    @Inject
    PrescriptionBundleValidator prescriptionBundleValidator;
    @Inject
    AppConfig appConfig;
    @Inject
    UserConfigurationService userConfigurationService;

    @ConfigProperty(name = "ere.websocket.remove-signature-from-message", defaultValue = "true")
    boolean removeSignatureFromMessage = true;

    @ConfigProperty(name = "ere.websocket.erezeptdocuments.reply-to-all", defaultValue = "false")
    boolean erezeptdocumentsReplyToAll = false;

    JsonbConfig customConfig = new JsonbConfig()
            .setProperty(JsonbConfig.FORMATTING, true)
            .withAdapters(new BundleAdapter())
            .withAdapters(new ByteAdapter())
            .withAdapters(new ThrowableAdapter())
            .withAdapters(new DurationAdapter());
    Jsonb jsonbFactory = JsonbBuilder.create(customConfig);
    private static final String CHROME_X86_PATH = "C:\\Program Files (x86)\\Google\\Chrome\\Application\\chrome.exe";
    private static final String CHROME_X64_PATH = "C:\\Program Files\\Google\\Chrome\\Application\\chrome.exe";
    private static final EreLogger ereLog = EreLogger.getLogger(Websocket.class);

    private final FhirContext ctx = FhirContext.forR4();
    private final static Set<Session> sessions = new CopyOnWriteArraySet<>();

    @OnOpen
    public void onOpen(Session session) {
        sessions.add(session);
        ereLog.info("Websocket opened");
    }

    void sendAllKBVExamples(String folder, Session senderSession) {
        if(folder.equals("../src/test/resources/kbv-zip")) {
            try {
                Bundle bundle = ctx.newXmlParser().parseResource(Bundle.class, getXmlString(folder + "/PF01.xml"));
                bundle.setId(UUID.randomUUID().toString());
                onFhirBundle(new BundlesEvent(Collections.singletonList(bundle), senderSession, ""));

                bundle = ctx.newXmlParser().parseResource(Bundle.class, getXmlString(folder + "/PF02.xml"));
                bundle.setId(UUID.randomUUID().toString());
                onFhirBundle(new BundlesEvent(Collections.singletonList(bundle), senderSession, ""));

                Bundle bundle03 = ctx.newXmlParser().parseResource(Bundle.class, getXmlString(folder + "/PF03.xml"));
                bundle03.setId(UUID.randomUUID().toString());

                Bundle bundle04 = ctx.newXmlParser().parseResource(Bundle.class, getXmlString(folder + "/PF04.xml"));
                bundle04.setId(UUID.randomUUID().toString());

                Bundle bundle05 = ctx.newXmlParser().parseResource(Bundle.class, getXmlString(folder + "/PF05.xml"));
                bundle05.setId(UUID.randomUUID().toString());

                onFhirBundle(new BundlesEvent(Arrays.asList(bundle03, bundle04, bundle05), senderSession, ""));

                bundle = ctx.newXmlParser().parseResource(Bundle.class, getXmlString(folder + "/PF07.xml"));
                bundle.setId(UUID.randomUUID().toString());
                onFhirBundle(new BundlesEvent(Collections.singletonList(bundle), senderSession, ""));

                Bundle bundle08_1 = ctx.newXmlParser().parseResource(Bundle.class, getXmlString(folder + "/PF08_1.xml"));
                bundle08_1.setId(UUID.randomUUID().toString());

                Bundle bundle08_2 = ctx.newXmlParser().parseResource(Bundle.class, getXmlString(folder + "/PF08_2.xml"));
                bundle08_2.setId(UUID.randomUUID().toString());

                Bundle bundle08_3 = ctx.newXmlParser().parseResource(Bundle.class, getXmlString(folder + "/PF08_3.xml"));
                bundle08_3.setId(UUID.randomUUID().toString());

                onFhirBundle(new BundlesEvent(Arrays.asList(bundle08_1, bundle08_2, bundle08_3), senderSession, ""));
            } catch(IOException ex) {
                ereLog.warn("Could read all files", ex);
            }
        } else {
            try (Stream<Path> paths = Files.walk(Paths.get(folder))) {
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
        }
    }

    private String getXmlString(String string) throws IOException {
        return "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" + Files.readString(Paths.get(string));
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
    public void onMessage(String message, Session senderSession) {
        ereLog.info("Message: " + message);
        if(message == null) {
            ereLog.warn("null given as message");
            return;
        }
        String messageId = null;
        try (JsonReader jsonReader = Json.createReader(new StringReader(message))) {
            JsonObject object = jsonReader.readObject();
            messageId = object.getString("id", null);
            if ("SignAndUploadBundles".equals(object.getString("type"))) {
                processSignAndUploadBundles(senderSession, messageId, object);
            } else if ("ValidateBundles".equals(object.getString("type"))) {
                JsonObject bundlesValidationResultMessage = prescriptionBundleValidator.bundlesValidationResult(object);
                senderSession.getAsyncRemote().sendObject(
                    bundlesValidationResultMessage.toString(),
                    result -> {
                        if (!result.isOK()) {
                            ereLog.fatal("Unable to sent bundlesValidationResult event: " + result.getException());
                        }
                    });
            } else if ("XMLBundle".equals(object.getString("type"))) {
                Bundle[] bundles = XmlPrescriptionProcessor.parseFromString(object.getString("payload"));
                if(appConfig.getXmlBundleDirectProcess()) {
                    SignAndUploadBundlesEvent event = new SignAndUploadBundlesEvent(bundles, senderSession, messageId);
                    signAndUploadBundlesEvent.fireAsync(event);   
                }
                onFhirBundle(new BundlesEvent(Arrays.asList(bundles), null, messageId));
            } else if ("AbortTasks".equals(object.getString("type"))) {
                abortTasksEvent.fireAsync(new AbortTasksEvent(object, senderSession, messageId));
            } else if ("ErixaEvent".equals(object.getString("type"))) {
                ErixaEvent event = new ErixaEvent(object, senderSession, messageId);
                erixaEvent.fireAsync(event);
            } else if ("DeactivateComfortSignature".equals(object.getString("type"))) {
                DeactivateComfortSignatureEvent event = new DeactivateComfortSignatureEvent(object, senderSession, messageId);
                deactivateComfortSignatureEvent.fireAsync(event);
            } else if ("ActivateComfortSignature".equals(object.getString("type"))) {
                ActivateComfortSignatureEvent event = new ActivateComfortSignatureEvent(object, senderSession, messageId);
                activateComfortSignatureEvent.fireAsync(event);
            } else if ("GetSignatureMode".equals(object.getString("type"))) {
                GetSignatureModeEvent event = new GetSignatureModeEvent(object, senderSession, messageId);
                getSignatureModeEvent.fireAsync(event);
            } else if ("GetCards".equals(object.getString("type"))) {
                GetCardsEvent event = new GetCardsEvent(object, senderSession, messageId);
                getCardsEvent.fireAsync(event);
            } else if ("ChangePin".equals(object.getString("type"))) {
                ChangePinEvent event = new ChangePinEvent(object, senderSession, messageId);
                changePinEvent.fireAsync(event);

            } else if ("VerifyPin".equals(object.getString("type"))) {
                VerifyPinEvent event = new VerifyPinEvent(object, senderSession, messageId);
                verifyPinEvent.fireAsync(event);
            }  else if ("RequestSettings".equals(object.getString("type"))) {
                UserConfigurations userConfigurations = userConfigurationService.getConfig();
                String payload = jsonbFactory.toJson(userConfigurations);
                senderSession.getAsyncRemote().sendObject(
                    "{\"type\": \"Settings\", \"payload\": " + payload + ", \"replyToMessageId\": \""+messageId+"\"}",
                    result -> {
                        if (!result.isOK()) {
                            ereLog.fatal("Unable to sent settings event: " + result.getException());
                        }
                    });
            } else if("SaveSettings".equals(object.getString("type"))) {
                String userConfiguration = object.getJsonObject("payload").toString();
                UserConfigurations userConfigurations = jsonbFactory.fromJson(userConfiguration, UserConfigurations.class);
                saveSettingsEvent.fireAsync(new SaveSettingsEvent(userConfigurations));
            } else if ("Publish".equals(object.getString("type"))) {
                sendMessage(object.getString("payload"), "Unable to publish event");
            } else if ("AllKBVExamples".equals(object.getString("type"))) {
                sendAllKBVExamples(object.getString("folder", "../src/test/resources/simplifier_erezept"), senderSession);
            } else if ("SimulateException".equals(object.getString("type"))) {
                onException(simulateException(object));
            } else {
                processIncomingMessage(object, senderSession);
            }
        } catch(Exception ex) {
            ereLog.warn("Could not process message", ex);
            onException(new ExceptionWithReplyToExcetion(ex, senderSession, messageId));
        }
    }

    private void processSignAndUploadBundles(Session senderSession, String messageId, JsonObject object) {
        boolean bundlesValid = true;
        JsonObject bundlesValidationResultMessage = null;
        if(!object.getBoolean("ignoreValidation", false)) {
            bundlesValidationResultMessage = prescriptionBundleValidator.bundlesValidationResult(object);
            
            bundlesValid = bundlesValidationResultMessage.getJsonArray("payload")
            .stream().filter(jo -> jo instanceof JsonObject)
                .map(jo -> ((JsonObject) jo).getBoolean("valid"))
                .filter(b -> !b)
                .count() == 0;
        }
        if(bundlesValid) {
            SignAndUploadBundlesEvent event = new SignAndUploadBundlesEvent(object, senderSession, messageId);
            signAndUploadBundlesEvent.fireAsync(event);
        } else {
            senderSession.getAsyncRemote().sendObject(
                bundlesValidationResultMessage == null ? "{}" : bundlesValidationResultMessage.toString(),
                result -> {
                    if (!result.isOK()) {
                        ereLog.fatal("Unable to sent bundlesValidationResult event: " + result.getException());
                    }
                });
        }
    }

    Exception simulateException(JsonObject object) {
        // Zugriffsbedingungen nicht erfüllt
        // boolean code4085
        Error faultInfo = new Error();
        Trace trace = new Trace();
        trace.setCode(BigInteger.valueOf(4085));
        faultInfo.getTrace().add(trace);
        return new FaultMessage("Zugriffsbedingungen nicht erfüllt", faultInfo);
    }

    public void onFhirBundle(@ObservesAsync BundlesEvent bundlesEvent) {
        assureChromeIsOpen();
        String bundlesString = generateJson(bundlesEvent);
        Set<Session> localSessions = new HashSet<>();
        if(bundlesEvent.getReplyTo() != null) {
            localSessions.add(bundlesEvent.getReplyTo());
        } else {
            localSessions = sessions;
        }
        localSessions.forEach(session -> session.getAsyncRemote().sendObject(
                "{\"type\": \"Bundles\", \"payload\": " + bundlesString + ", \"replyToMessageId\": \""+bundlesEvent.getReplyToMessageId()+"\"}",
                result -> {
                    if (!result.isOK()) {
                        ereLog.fatal("Unable to send bundlesEvent: " + result.getException());
                    }
                }));
    }

    public void onAbortTasksStatusEvent(@ObservesAsync AbortTasksStatusEvent abortTasksStatusEvent) {
        assureChromeIsOpen();
        String abortTasksStatusString = generateJson(abortTasksStatusEvent);
        
        abortTasksStatusEvent.getReplyTo().getAsyncRemote().sendObject(
                "{\"type\": \"AbortTasksStatus\", \"payload\": " + abortTasksStatusString + ", \"replyToMessageId\": \""+abortTasksStatusEvent.getReplyToMessageId()+"\"}",
                result -> {
                    if (!result.isOK()) {
                        ereLog.fatal("Unable to send bundlesEvent: " + result.getException());
                    }
                });
    }

    public void onGetCardsResponseEvent(@ObservesAsync GetCardsResponseEvent getCardsResponseEvent) {
        assureChromeIsOpen();
        String abortTasksStatusString = generateJson(getCardsResponseEvent);
        
        getCardsResponseEvent.getReplyTo().getAsyncRemote().sendObject(
                "{\"type\": \"GetCardsResponse\", \"payload\": " + abortTasksStatusString + ", \"replyToMessageId\": \""+getCardsResponseEvent.getReplyToMessageId()+"\"}",
                result -> {
                    if (!result.isOK()) {
                        ereLog.fatal("Unable to get cards response: " + result.getException());
                    }
                });
    }

    public void onGetSignatureModeResponseEvent(@ObservesAsync GetSignatureModeResponseEvent getSignatureModeResponseEvent) {
        assureChromeIsOpen();
        String abortTasksStatusString = generateJson(getSignatureModeResponseEvent);
        getSignatureModeResponseEvent.getReplyTo().getAsyncRemote().sendObject(
                "{\"type\": \"GetSignatureModeResponse\", \"payload\": " + abortTasksStatusString + ", \"replyToMessageId\": \""+getSignatureModeResponseEvent.getReplyToMessageId()+"\"}",
                result -> {
                    if (!result.isOK()) {
                        ereLog.fatal("Unable to send getSignatureModeResponseEvent: " + result.getException());
                    }
                });
    }

    public void onChangePinResponseEvent(@ObservesAsync ChangePinResponseEvent changePinResponseEvent) {
        assureChromeIsOpen();
        String changePinResponseString = generateJson(changePinResponseEvent);
        changePinResponseEvent.getReplyTo().getAsyncRemote().sendObject(
                "{\"type\": \"ChangePinResponse\", \"payload\": " + changePinResponseString + ", \"replyToMessageId\": \""+changePinResponseEvent.getReplyToMessageId()+"\"}",
                result -> {
                    if (!result.isOK()) {
                        ereLog.fatal("Unable to send changePinResponseEvent: " + result.getException());
                    }
                });
    }

    String generateJson(GetSignatureModeResponseEvent getSignatureModeResponseEvent) {
        return jsonbFactory.toJson(getSignatureModeResponseEvent);
    }

    String generateJson(GetCardsResponseEvent getCardsResponseEvent) {
        return jsonbFactory.toJson(getCardsResponseEvent);
    }

    String generateJson(AbortTasksStatusEvent abortTasksStatusEvent) {
        return jsonbFactory.toJson(abortTasksStatusEvent.getTasks());
    }

    String generateJson(ChangePinResponseEvent changePinResponseEvent) {
        return jsonbFactory.toJson(changePinResponseEvent.getChangePinResponse());
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

    public void onERezeptDocuments(@ObservesAsync ERezeptWithDocumentsEvent eRezeptDocumentsEvent) {
        String jsonPayload = generateJson(eRezeptDocumentsEvent);
        ereLog.info("Sending prescription receipt payload to front-end: " +
                jsonPayload);

                Set<Session> localSessions = new HashSet<>();
        if(eRezeptDocumentsEvent.getReplyTo() != null && !erezeptdocumentsReplyToAll) {
            localSessions.add(eRezeptDocumentsEvent.getReplyTo());
        } else {
            localSessions = sessions;
        }
        localSessions.forEach(session -> {

            session.getAsyncRemote().sendObject(
                jsonPayload,
                result -> {
                    if (!result.isOK()) {
                        ereLog.fatal("Unable to send eRezeptWithDocumentsEvent: " +
                                result.getException());
                    }
                });
        });
    }

    public String generateJson(ERezeptWithDocumentsEvent eRezeptDocumentsEvent) {
        if(removeSignatureFromMessage) {
            eRezeptDocumentsEvent.getERezeptWithDocuments().stream()
                .flatMap(ezd -> ezd.getBundleWithAccessCodeOrThrowables().stream())
                .forEach(bundleWithAccessCodeOrThrowables -> bundleWithAccessCodeOrThrowables.setSignedBundle(null));
        }

        return "{\"type\": \"ERezeptWithDocuments\", \"payload\": " +
                jsonbFactory.toJson(eRezeptDocumentsEvent.getERezeptWithDocuments()) + ", \"replyToMessageId\": \""+eRezeptDocumentsEvent.getReplyToMessageId()+"\"}";
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

    public void onException(@ObservesAsync Exception exceptionParam) {

        Set<Session> localSessions = sessions;

        Exception exceptionFromReplyTo = null;
        String replyToMessageIdFromException = null; 

        // only send the exception to the session that provoked it
        if(exceptionParam instanceof ExceptionWithReplyToExcetion) {
            ExceptionWithReplyToExcetion exceptionWithReplyToExcetion = (ExceptionWithReplyToExcetion) exceptionParam;
            localSessions = new HashSet<>();
            if(exceptionWithReplyToExcetion.getReplyTo() != null) {
                localSessions.add(exceptionWithReplyToExcetion.getReplyTo());
                exceptionFromReplyTo = exceptionWithReplyToExcetion.getException();
                replyToMessageIdFromException = exceptionWithReplyToExcetion.getMessageId();
            }
        }

        final Exception exception = exceptionFromReplyTo != null ? exceptionFromReplyTo : exceptionParam;
        final String replyToMessageId = replyToMessageIdFromException != null ? replyToMessageIdFromException : "";

        localSessions.forEach(session -> {
            session.getAsyncRemote()
                .sendObject("{\"type\": \"Exception\", \"payload\": "+jsonbFactory.toJson(exception)+", \"replyToMessageId\": \""+replyToMessageId+"\"}", result -> {
                    if (result.getException() != null) {
                        ereLog.fatal("Unable to send message: " + result.getException());
                    }
                });
        });
    }

    public void onEreLogNotificationEvent(@ObservesAsync EreLogNotificationEvent event) {
        sessions.forEach(session -> {
            OutgoingPayload<EreLogNotificationEvent> outgoingPayload = new OutgoingPayload<>(event);

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
        event.getReplyTo().getAsyncRemote()
        .sendObject("{\"type\": \"HTMLBundles\", \"payload\": " +
        jsonbFactory.toJson(event.getBundles()) + ", \"replyToMessageId\": \""+event.getReplyToMessageId()+"\"}", result -> {
            if (result.getException() != null) {
                ereLog.fatal("Unable to send message: " + result.getException());
            }
        });
    }

    private void processIncomingMessage(JsonObject object, Session senderSession) {
        String messageId = object.getString("id", "");
        for (IncomingMessageProcessor messageProcessor : messageProcessors) {
            if (messageProcessor.canProcess(object.toString())) {
                if (messageProcessor instanceof IncomingBundleMessageProcessor) {
                    String response = messageProcessor.process(object.toString());
                    JsonObject bundlesJSON = Json.createReader(new StringReader(response)).readObject();
                    processSignAndUploadBundles(senderSession, messageId, bundlesJSON);
                } else {
                    messageProcessor.process(object.toString());
                }
            }
        }
    }

    private void sendMessage(String message, String errorMessage) {
        final String processedMessage = processOutgoing(message);
        sessions.forEach(session -> session.getAsyncRemote().sendObject(processedMessage, result -> {
            if (result.getException() != null)
                ereLog.fatal(errorMessage);
        }));
    }

    private String processOutgoing(String message) {
        for (OutgoingMessageProcessor processor : outgoingMessageProcessors)
            if (processor.canProcess(message))
                return processor.process(message);
        return message;
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
