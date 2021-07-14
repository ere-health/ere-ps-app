package health.ere.ps.service.connector.producers;


import de.gematik.ws.conn.authsignatureservice.wsdl.v7.AuthSignatureService;
import de.gematik.ws.conn.authsignatureservice.wsdl.v7.AuthSignatureServicePortType;
import de.gematik.ws.conn.cardservice.wsdl.v8.CardService;
import de.gematik.ws.conn.cardservice.wsdl.v8.CardServicePortType;
import de.gematik.ws.conn.certificateservice.wsdl.v6.CertificateService;
import de.gematik.ws.conn.certificateservice.wsdl.v6.CertificateServicePortType;
import de.gematik.ws.conn.connectorcontext.v2.ContextType;
import de.gematik.ws.conn.eventservice.wsdl.v7.EventService;
import de.gematik.ws.conn.eventservice.wsdl.v7.EventServicePortType;
import de.gematik.ws.conn.signatureservice.wsdl.v7.SignatureService;
import de.gematik.ws.conn.signatureservice.wsdl.v7.SignatureServicePortType;
import de.gematik.ws.conn.signatureservice.wsdl.v7.SignatureServicePortTypeV755;
import de.gematik.ws.conn.signatureservice.wsdl.v7.SignatureServiceV755;
import health.ere.ps.service.common.security.SecretsManagerService;
import health.ere.ps.service.connector.endpoint.EndpointDiscoveryService;
import health.ere.ps.service.connector.endpoint.SSLUtilities;
import org.eclipse.microprofile.config.inject.ConfigProperty;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Produces;
import javax.inject.Inject;
import javax.xml.ws.BindingProvider;
import java.util.logging.Logger;

@ApplicationScoped
public class ConnectorServicesProducer {
    private static final Logger log = Logger.getLogger(ConnectorServicesProducer.class.getName());

    @Inject
    AppConfig appConfig;
    @Inject
    EndpointDiscoveryService endpointDiscoveryService;
    @Inject
    SecretsManagerService secretsManagerService;

    @Produces
    public CardServicePortType cardServicePortType() {
        CardServicePortType cardService = new CardService(getClass().getResource("/CardService.wsdl"))
                .getCardServicePort();

        BindingProvider bp = (BindingProvider) cardService;
        bp.getRequestContext().put(BindingProvider.ENDPOINT_ADDRESS_PROPERTY,
                endpointDiscoveryService.getCardServiceEndpointAddress());
        configureBindingProvider(bp);

        return cardService;
    }

    @Produces
    public CertificateServicePortType certificateService() {
        CertificateServicePortType certificateService = new CertificateService(getClass()
                .getResource("/CertificateService_v6_0_1.wsdl")).getCertificateServicePort();

        BindingProvider bp = (BindingProvider) certificateService;
        bp.getRequestContext().put(BindingProvider.ENDPOINT_ADDRESS_PROPERTY,
                endpointDiscoveryService.getCertificateServiceEndpointAddress());
        configureBindingProvider(bp);

        return certificateService;
    }

    @Produces
    public EventServicePortType eventServicePortType() {
        EventServicePortType eventService = new EventService(getClass().getResource("/EventService.wsdl"))
                .getEventServicePort();

        BindingProvider bp = (BindingProvider) eventService;
        bp.getRequestContext().put(BindingProvider.ENDPOINT_ADDRESS_PROPERTY,
                endpointDiscoveryService.getEventServiceEndpointAddress());
        configureBindingProvider(bp);

        return eventService;
    }

    @Produces
    public AuthSignatureServicePortType authSignatureServicePortType() {
        AuthSignatureServicePortType authSignatureService = new AuthSignatureService(getClass().getResource(
                "/AuthSignatureService_v7_4_1.wsdl")).getAuthSignatureServicePort();
        BindingProvider bp = (BindingProvider) authSignatureService;

        bp.getRequestContext().put(BindingProvider.ENDPOINT_ADDRESS_PROPERTY,
                endpointDiscoveryService.getAuthSignatureServiceEndpointAddress());
        configureBindingProvider(bp);

        return authSignatureService;
    }

    @Produces
    public SignatureServicePortTypeV740 signatureServicePortTypeV740() {
        SignatureServicePortTypeV740 signatureService = new SignatureServiceV740(getClass()
                .getResource("/SignatureService.wsdl")).getSignatureServicePortV740();

        BindingProvider bp = (BindingProvider) signatureService;
        bp.getRequestContext().put(BindingProvider.ENDPOINT_ADDRESS_PROPERTY,
                endpointDiscoveryService.getSignatureServiceEndpointAddress());
        configureBindingProvider(bp);

        return signatureService;
    }

    @Produces
    public SignatureServicePortTypeV742 signatureServicePortTypeV742() {
        SignatureServicePortTypeV742 signatureService = new SignatureServiceV742(getClass()
                .getResource("/SignatureService_V7_4_2.wsdl")).getSignatureServicePortv742();

        BindingProvider bp = (BindingProvider) signatureService;
        bp.getRequestContext().put(BindingProvider.ENDPOINT_ADDRESS_PROPERTY,
                endpointDiscoveryService.getSignatureServiceEndpointAddress());
        configureBindingProvider(bp);

        return signatureService;
    }

    @Produces
    public SignatureServicePortTypeV755 signatureServicePortTypeV755() {
        SignatureServicePortTypeV755 signatureServiceV755 = new SignatureServiceV755(getClass()
                .getResource("/SignatureService_V7_5_5.wsdl")).getSignatureServicePortTypeV755();

        BindingProvider bp = (BindingProvider) signatureServiceV755;
        bp.getRequestContext().put(BindingProvider.ENDPOINT_ADDRESS_PROPERTY,
                endpointDiscoveryService.getSignatureServiceEndpointAddress());
        configureBindingProvider(bp);

        return signatureServiceV755;
    }

    private void configureBindingProvider(BindingProvider bindingProvider) {
        bindingProvider.getRequestContext().put("com.sun.xml.ws.transport.https.client.SSLSocketFactory",
                secretsManagerService.getSslContext().getSocketFactory());
        bindingProvider.getRequestContext().put("com.sun.xml.ws.transport.https.client.hostname.verifier",
                new SSLUtilities.FakeHostnameVerifier());
    }

    @Produces
    public ContextType contextType() {
        ContextType contextType = new ContextType();
        contextType.setMandantId(appConfig.getMandantId());
        contextType.setClientSystemId(appConfig.getClientSystemId());
        contextType.setWorkplaceId(appConfig.getWorkplaceId());
        contextType.setUserId(appConfig.getUserId());

        return contextType;
    }
}
