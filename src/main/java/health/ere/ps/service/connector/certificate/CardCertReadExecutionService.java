package health.ere.ps.service.connector.certificate;

import de.gematik.ws.conn.cardservice.wsdl.v8.CardService;
import de.gematik.ws.conn.cardservice.wsdl.v8.CardServicePortType;
import de.gematik.ws.conn.cardservicecommon.v2.PinResultEnum;
import de.gematik.ws.conn.certificateservice.v6.ReadCardCertificate;
import de.gematik.ws.conn.certificateservice.v6.ReadCardCertificateResponse;
import de.gematik.ws.conn.certificateservice.wsdl.v6.CertificateService;
import de.gematik.ws.conn.certificateservice.wsdl.v6.CertificateServicePortType;
import de.gematik.ws.conn.certificateservice.wsdl.v6.FaultMessage;
import de.gematik.ws.conn.certificateservicecommon.v2.CertRefEnum;
import de.gematik.ws.conn.certificateservicecommon.v2.X509DataInfoListType;
import de.gematik.ws.conn.connectorcommon.v5.Status;
import de.gematik.ws.conn.connectorcontext.v2.ContextType;
import health.ere.ps.config.AppConfig;
import health.ere.ps.exception.connector.ConnectorCardCertificateReadException;
import health.ere.ps.service.common.security.SecretsManagerService;
import health.ere.ps.service.connector.endpoint.EndpointDiscoveryService;
import health.ere.ps.service.connector.endpoint.SSLUtilities;
import org.eclipse.microprofile.config.inject.ConfigProperty;

import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.net.ssl.SSLContext;
import javax.xml.ws.BindingProvider;
import javax.xml.ws.Holder;
import java.math.BigInteger;
import java.util.logging.Logger;

@ApplicationScoped
public class CardCertReadExecutionService {

    private static final Logger log = Logger.getLogger(CardCertReadExecutionService.class.getName());

    static {
        System.setProperty("javax.xml.accessExternalDTD", "all");
    }

    @Inject
    SecretsManagerService secretsManagerService;
    @Inject
    EndpointDiscoveryService endpointDiscoveryService;
    @ConfigProperty(name = "connector.cert.auth.store.file", defaultValue = "!")
    String certAuthStoreFile;
    @Inject
    AppConfig appConfig;

    private CardServicePortType cardService;
    private CertificateServicePortType certificateService;

    @PostConstruct
    void init() throws Exception {
        certificateService = new CertificateService(getClass().getResource("/CertificateService_v6_0_1.wsdl")).getCertificateServicePort();

        // Set endpoint to configured endpoint
        BindingProvider bp = (BindingProvider) certificateService;

        bp.getRequestContext().put(BindingProvider.ENDPOINT_ADDRESS_PROPERTY,
                appConfig.getCertificateServiceEndpointAddress());

        if (certAuthStoreFile != null && !("".equals(certAuthStoreFile))
                && !("!".equals(certAuthStoreFile))) {
            log.info(CardCertReadExecutionService.class.getSimpleName() + " uses titus client certifcate: " + certAuthStoreFile);
            setUpCustomSSLContext(certAuthStoreFile);
        }

        cardService = new CardService(getClass().getResource("/CardService.wsdl")).getCardServicePort();
        // Set endpoint to configured endpoint
        bp = (BindingProvider) cardService;
        bp.getRequestContext().put(BindingProvider.ENDPOINT_ADDRESS_PROPERTY,
                endpointDiscoveryService.getCardServiceEndpointAddress());
        endpointDiscoveryService.configureSSLTransportContext(bp);
    }

    public void setUpCustomSSLContext(String p12CertificateFile) {
        SSLContext customSSLContext = secretsManagerService.setUpCustomSSLContext(p12CertificateFile);
        BindingProvider bp = (BindingProvider) certificateService;

        bp.getRequestContext().put("com.sun.xml.ws.transport.https.client.SSLSocketFactory",
                customSSLContext.getSocketFactory());
        bp.getRequestContext().put("com.sun.xml.ws.transport.https.client.hostname.verifier", new SSLUtilities.FakeHostnameVerifier());
    }

    /**
     * Reads the AUT certificate of a card.
     *
     * @param invocationContext The invocation context via which the card can be accessed.
     * @param cardHandle        The handle of the card whose AUT certificate is to be read.
     * @return The read AUT certificate.
     */
    public ReadCardCertificateResponse doReadCardCertificate(
            InvocationContext invocationContext, String cardHandle)
            throws ConnectorCardCertificateReadException {
        ContextType contextType = invocationContext.convertToContextType();

        ReadCardCertificate.CertRefList certRefList = new ReadCardCertificate.CertRefList();
        certRefList.getCertRef().add(CertRefEnum.C_AUT);

        Holder<Status> statusHolder = new Holder<Status>();
        Holder<X509DataInfoListType> certHolder = new Holder<X509DataInfoListType>();

        try {
            contextType.setMandantId(appConfig.getMandantId());
            certificateService.readCardCertificate(cardHandle, contextType, certRefList,
                    statusHolder, certHolder);
        } catch (FaultMessage faultMessage) {
            // Zugriffsbedingungen nicht erfüllt
            boolean code4085 = faultMessage.getFaultInfo().getTrace().stream()
                    .anyMatch(t -> t.getCode().equals(BigInteger.valueOf(4085L)));

            if (code4085) {
                Holder<Status> status = new Holder<>();
                Holder<PinResultEnum> pinResultEnum = new Holder<>();
                Holder<BigInteger> error = new Holder<>();
                try {
                    cardService.verifyPin(contextType, cardHandle, "PIN.SMC", status, pinResultEnum, error);
                    doReadCardCertificate(invocationContext, cardHandle);
                } catch (de.gematik.ws.conn.cardservice.wsdl.v8.FaultMessage e) {
                    throw new ConnectorCardCertificateReadException("Could not get certificate", faultMessage);
                }
            } else {
                throw new ConnectorCardCertificateReadException("Could not get certificate", faultMessage);
            }
        }

        ReadCardCertificateResponse readCardCertificateResponse = new ReadCardCertificateResponse();

        readCardCertificateResponse.setStatus(statusHolder.value);
        readCardCertificateResponse.setX509DataInfoList(certHolder.value);

        return readCardCertificateResponse;
    }
}
