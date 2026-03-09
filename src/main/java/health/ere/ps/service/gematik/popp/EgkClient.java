package health.ere.ps.service.gematik.popp;

import de.gematik.ws.conn.cardservice.v8.CardInfoType;
import de.gematik.ws.conn.cardservice.v821.SecureSendAPDU;
import de.gematik.ws.conn.cardservice.v821.SecureSendAPDUResponse;
import de.gematik.ws.conn.cardservice.v821.StartCardSession;
import de.gematik.ws.conn.cardservice.v821.StartCardSessionResponse;
import de.gematik.ws.conn.cardservice.v821.StopCardSession;
import de.gematik.ws.conn.cardservice.wsdl.v8_2.CardServicePortType;
import de.gematik.ws.conn.cardservicecommon.v2.CardTypeType;
import de.gematik.ws.conn.eventservice.v7.GetCards;
import de.gematik.ws.conn.eventservice.v7.GetCardsResponse;
import de.gematik.ws.conn.eventservice.wsdl.v7.EventServicePortType;
import de.gematik.zeta.sdk.authentication.smcb.ConnectorApi;
import de.gematik.zeta.sdk.authentication.smcb.model.ExternalAuthenticateResponse;
import de.gematik.zeta.sdk.authentication.smcb.model.ReadCardCertificateResponse;
import de.gematik.zeta.sdk.authentication.smcb.model.SignatureObject;
import de.gematik.zeta.sdk.authentication.smcb.model.Status;
import health.ere.ps.config.RuntimeConfig;
import health.ere.ps.service.connector.auth.SmcbAuthenticatorService;
import health.ere.ps.service.connector.certificate.CardCertificateReaderService;
import health.ere.ps.service.connector.provider.MultiConnectorServicesProvider;
import health.ere.ps.zeta.utils.CoroutineInterop;
import health.ere.ps.zeta.utils.EncodingUtils;
import health.ere.ps.zeta.utils.SoapUtils;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.NotFoundException;
import kotlin.coroutines.Continuation;
import kotlin.coroutines.intrinsics.IntrinsicsKt;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

@ApplicationScoped
public class EgkClient implements ConnectorApi, IEgkClient {

    private static final Logger log = Logger.getLogger(EgkClient.class.getName());

    private final SmcbAuthenticatorService smcbAuthenticatorService;
    private final CardCertificateReaderService cardCertificateReaderService;
    private final MultiConnectorServicesProvider servicePortProvider;

    private final ConcurrentHashMap<String, RuntimeConfig> configMap = new ConcurrentHashMap<>();

    @Inject
    public EgkClient(
        SmcbAuthenticatorService smcbAuthenticatorService,
        CardCertificateReaderService cardCertificateReaderService,
        MultiConnectorServicesProvider servicePortProvider
    ) {
        this.smcbAuthenticatorService = smcbAuthenticatorService;
        this.cardCertificateReaderService = cardCertificateReaderService;
        this.servicePortProvider = servicePortProvider;
    }

    public void registerRuntimeConfig(RuntimeConfig runtimeConfig) {
        configMap.putIfAbsent(Thread.currentThread().getName(), runtimeConfig);
    }

    public void unregisterRuntimeConfig(String threadName) {
        configMap.remove(threadName);
    }

    @Override
    public Object readCertificate(
        @NotNull String cardHandle,
        @NotNull String mandantId,
        String clientSystemId,
        String workspaceId,
        String userId,
        @NotNull Continuation<? super ReadCardCertificateResponse> continuation
    ) {
        try {
            RuntimeConfig runtimeConfig = configMap.get(Thread.currentThread().getName());
            de.gematik.ws.conn.certificateservice.v6.ReadCardCertificateResponse readCardCertificateResponse;
            readCardCertificateResponse = cardCertificateReaderService.doReadCardCertificate(
                runtimeConfig.getSMCBHandle(), runtimeConfig
            );
            String certificateResponseAsString = XmlUtils.print(readCardCertificateResponse, false);
            ReadCardCertificateResponse resp = SoapUtils.deserializeCertificateResponse(certificateResponseAsString);
            CoroutineInterop.resumeOk(continuation, resp);
        } catch (Throwable t) {
            CoroutineInterop.resumeErr(continuation, t);
        }
        return IntrinsicsKt.getCOROUTINE_SUSPENDED();
    }

    @Override
    public Object externalAuthenticate(
        @NotNull String cardHandle,
        @NotNull String mandantId,
        String clientSystemId,
        String workspaceId,
        String userId,
        @NotNull String base64Challenge,
        @NotNull Continuation<? super ExternalAuthenticateResponse> continuation
    ) {
        RuntimeConfig runtimeConfig = configMap.get(Thread.currentThread().getName());
        String smcbHandle = runtimeConfig.getSMCBHandle();
        byte[] sha265Hash = EncodingUtils.base64DecodeWithAbsentPadding(base64Challenge);
        try {
            byte[] bytes = smcbAuthenticatorService.externalAuthenticate(sha265Hash, smcbHandle, runtimeConfig, true);
            String base64Signature = EncodingUtils.base64Encode(EncodingUtils.p1363ToDer(bytes));

            Status status = new Status("ok");
            SignatureObject signatureObject = new SignatureObject(base64Signature);
            ExternalAuthenticateResponse resp = new ExternalAuthenticateResponse(status, signatureObject);

            CoroutineInterop.resumeOk(continuation, resp);
        } catch (Throwable t) {
            CoroutineInterop.resumeErr(continuation, t);
        }
        return IntrinsicsKt.getCOROUTINE_SUSPENDED();
    }

    @Override
    public String getConnectedEgkCard() {
        RuntimeConfig runtimeConfig = configMap.get(Thread.currentThread().getName());
        GetCards parameter = new GetCards();
        parameter.setContext(servicePortProvider.getContextType(runtimeConfig));
        parameter.setCardType(CardTypeType.EGK);

        EventServicePortType eventService = servicePortProvider.getEventServicePortType(runtimeConfig);
        try {
            GetCardsResponse getCardsResponse = eventService.getCards(parameter);
            List<CardInfoType> cards = getCardsResponse.getCards().getCard();
            if (cards.isEmpty()) {
                throw new NotFoundException("Card not found");
            }
            return cards.getFirst().getCardHandle();
        } catch (Exception e) {
            throw new IllegalStateException("Unable to retrieve card handle", e);
        }
    }

    @Override
    public String startCardSession(String cardHandle) {
        RuntimeConfig runtimeConfig = configMap.get(Thread.currentThread().getName());
        CardServicePortType cardService = servicePortProvider.getCardServicePortType(runtimeConfig);
        StartCardSession startCardSession = new StartCardSession();
        startCardSession.setCardHandle(cardHandle);
        startCardSession.setContext(servicePortProvider.getContextType(runtimeConfig));
        try {
            StartCardSessionResponse startCardSessionResponse = cardService.startCardSession(startCardSession);
            String sessionId = startCardSessionResponse.getSessionId();
            log.info("[%s] Started card session: %s".formatted(cardHandle, sessionId));
            return sessionId;
        } catch (Exception e) {
            throw new IllegalStateException("Unable to start card session", e);
        }
    }

    @Override
    public void stopCardSession(String sessionId) {
        RuntimeConfig runtimeConfig = configMap.get(Thread.currentThread().getName());
        log.info("Stopping card session: %s".formatted(sessionId));
        StopCardSession stopCardSessionRequest = new StopCardSession();
        stopCardSessionRequest.setSessionId(sessionId);
        CardServicePortType cardService = servicePortProvider.getCardServicePortType(runtimeConfig);
        try {
            cardService.stopCardSession(stopCardSessionRequest);
        } catch (Exception e) {
            log.log(Level.SEVERE, "Unable to stop card session: " + e.getMessage());
        }
    }

    @Override
    public List<String> secureSendApdu(String signedScenario) {
        RuntimeConfig runtimeConfig = configMap.get(Thread.currentThread().getName());
        SecureSendAPDU secureSendAPDU = new SecureSendAPDU();
        secureSendAPDU.setSignedScenario(signedScenario);

        CardServicePortType cardService = servicePortProvider.getCardServicePortType(runtimeConfig);
        try {
            SecureSendAPDUResponse secureSendAPDUResponse = cardService.secureSendAPDU(secureSendAPDU);
            return secureSendAPDUResponse.getSignedScenarioResponse().getResponseApduList().getResponseApdu();
        } catch (de.gematik.ws.conn.cardservice.wsdl.v8_2.FaultMessage e) {
            throw new IllegalStateException("Unable to send secure APDU", e);
        }
    }
}
