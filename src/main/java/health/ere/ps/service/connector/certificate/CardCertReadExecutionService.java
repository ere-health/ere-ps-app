package health.ere.ps.service.connector.certificate;

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
import org.eclipse.microprofile.config.inject.ConfigProperty;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.net.ssl.SSLContext;
import javax.xml.ws.BindingProvider;
import javax.xml.ws.Holder;

import health.ere.ps.exception.connector.ConnectorCardCertificateReadException;
import health.ere.ps.service.common.security.SecretsManagerService;

@ApplicationScoped
public class CardCertReadExecutionService {

    private static Logger log = Logger.getLogger(CardCertReadExecutionService.class.getName());

    @Inject
    SecretsManagerService secretsManagerService;

    @ConfigProperty(name = "connector.cert.auth.store.file", defaultValue = "!")
    String certAuthStoreFile;

    @Inject
    AppConfig appConfig;

    private CertificateServicePortType certificateService;

    static {
        System.setProperty("javax.xml.accessExternalDTD", "all");
    }

    @PostConstruct
    void init() throws Exception {
        certificateService = new CertificateService(getClass().getResource("/CertificateService_v6_0_1.wsdl")).getCertificateServicePort();
        
        // Set endpoint to configured endpoint
        BindingProvider bp = (BindingProvider) certificateService;
        
        bp.getRequestContext().put(BindingProvider.ENDPOINT_ADDRESS_PROPERTY,
        appConfig.getCertificateServiceEndpointAddress());
        
        if (certAuthStoreFile != null && !("".equals(certAuthStoreFile))
            && !("!".equals(certAuthStoreFile))) {
            try(InputStream is = new FileInputStream(certAuthStoreFile)) {
                log.info(CardCertReadExecutionService.class.getSimpleName()+" uses titus client certifcate: "+ certAuthStoreFile);
                setUpCustomSSLContext(is);
            } catch(FileNotFoundException e) {
                log.log(Level.SEVERE, "Could find file", e);
            }
        }
    }

    public void setUpCustomSSLContext(InputStream p12Certificate) {
        SSLContext customSSLContext = secretsManagerService.setUpCustomSSLContext(p12Certificate);
        BindingProvider bp = (BindingProvider) certificateService;

        bp.getRequestContext().put("com.sun.xml.ws.transport.https.client.SSLSocketFactory",
               customSSLContext.getSocketFactory());
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
            certificateService.readCardCertificate(cardHandle, contextType, certRefList,
                    statusHolder, certHolder);
        } catch (FaultMessage faultMessage) {
            new ConnectorCardCertificateReadException("Exception reading aut certificate",
                    faultMessage);
        }

        ReadCardCertificateResponse readCardCertificateResponse = new ReadCardCertificateResponse();

        readCardCertificateResponse.setStatus(statusHolder.value);
        readCardCertificateResponse.setX509DataInfoList(certHolder.value);

        return readCardCertificateResponse;
    }

}
