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

import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;
import javax.xml.ws.BindingProvider;
import javax.xml.ws.Holder;

import health.ere.ps.exception.connector.ConnectorCardCertificateReadException;

@ApplicationScoped
public class CardCertReadExecutionService {
    @ConfigProperty(name = "idp.connector.certificate-service.endpoint.address")
    String certificateServiceEndpointAddress;

    private CertificateServicePortType certificateService;

    @PostConstruct
    void init() {
        certificateService = new CertificateService().getCertificateServicePort();

        /* Set endpoint to configured endpoint */
        BindingProvider bp = (BindingProvider) certificateService;

        bp.getRequestContext().put(BindingProvider.ENDPOINT_ADDRESS_PROPERTY,
                certificateServiceEndpointAddress);
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

        ReadCardCertificate readCardCertificate = new ReadCardCertificate();
        ContextType contextType = invocationContext.convertToContextType();
        readCardCertificate.setContext(contextType);

        readCardCertificate.setCardHandle(cardHandle);

        ReadCardCertificate.CertRefList certRefList = new ReadCardCertificate.CertRefList();
        certRefList.getCertRef().add(CertRefEnum.C_AUT);
        readCardCertificate.setCertRefList(certRefList);

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
