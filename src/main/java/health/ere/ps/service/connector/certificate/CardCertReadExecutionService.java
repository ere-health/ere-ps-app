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

import org.eclipse.microprofile.config.inject.ConfigProperty;

import org.apache.cxf.configuration.jsse.TLSClientParameters;
import org.apache.cxf.endpoint.Client;
import org.apache.cxf.frontend.ClientProxy;
import org.apache.cxf.transport.http.HTTPConduit;

import java.io.IOException;
import java.io.InputStream;

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
    @Inject
    SecretsManagerService secretsManagerService;
    
    @ConfigProperty(name = "idp.connector.certificate-service.endpoint.address")
    String certificateServiceEndpointAddress;

    @ConfigProperty(name = "idp.cert.store.file")
    String idpCertStoreFile;

    @ConfigProperty(name = "idp.cert.store.file.password")
    String idpCertStoreFilePassword;

    private CertificateServicePortType certificateService;

    @PostConstruct
    void init() throws Exception {
        certificateService = new CertificateService().getCertificateServicePort();

        // Set endpoint to configured endpoint
        BindingProvider bp = (BindingProvider) certificateService;

        bp.getRequestContext().put(BindingProvider.ENDPOINT_ADDRESS_PROPERTY,
                certificateServiceEndpointAddress);
//        try(InputStream certInputStream = getClass().getResourceAsStream(idpCertStoreFile)) {
//            SSLContext sc = secretsManagerService.createSSLContext(
//                    certInputStream, idpCertStoreFilePassword.toCharArray(),
//                    SecretsManagerService.SslContextType.TLS,
//                    SecretsManagerService.KeyStoreType.PKCS12);
//            bp.getRequestContext().put(
//                    "com.sun.xml.internal.ws.transport.https.client.SSLSocketFactory",
//                    sc.getSocketFactory());
//        }

        // TODO: Check with Gematik. The sslcontext code below doesn't provide any results
        //  whether it's present or not when invoking the Titus Connector CertificateReader API
        //  endpoint.
        // Get the underlying http conduit of the client proxy
        Client client = ClientProxy.getClient(certificateService);
        HTTPConduit http = (HTTPConduit) client.getConduit();

        // Set the TLS client parameters
        TLSClientParameters parameters = new TLSClientParameters();
        try(InputStream certInputStream = getClass().getResourceAsStream(idpCertStoreFile)) {
            parameters.setSSLSocketFactory(secretsManagerService.createSSLContext(
                    certInputStream, idpCertStoreFilePassword.toCharArray(),
                    SecretsManagerService.SslContextType.TLS,
                    SecretsManagerService.KeyStoreType.PKCS12).getSocketFactory());
            http.setTlsClientParameters(parameters);
        }
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
