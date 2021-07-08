package health.ere.ps.service.connector.certificate;

import de.gematik.ws.conn.certificateservice.v6.ReadCardCertificateResponse;
import de.gematik.ws.conn.certificateservicecommon.v2.X509DataInfoListType;
import de.gematik.ws.conn.connectorcommon.v5.Status;
import health.ere.ps.exception.connector.ConnectorCardCertificateReadException;
import health.ere.ps.service.idp.crypto.CryptoLoader;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.ArrayUtils;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import java.security.cert.X509Certificate;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

@ApplicationScoped
public class CardCertificateReaderService {

    private static final Logger log = Logger.getLogger(CardCertificateReaderService.class.getName());
    private static final String STATUS_OK = "OK";

    @Inject
    CardCertReadExecutionService cardCertReadExecutionService;

    /**
     * Reads the AUT certificate of a card managed in the connector.
     *
     * @param cardHandle The handle of the card.
     * @return The card's AUT certificate.
     */
    byte[] readCardCertificate(String cardHandle)
            throws ConnectorCardCertificateReadException {
        byte[] x509Certificate = new byte[0];

        ReadCardCertificateResponse readCardCertificateResponse =
                cardCertReadExecutionService.doReadCardCertificate(cardHandle);

        Status status = readCardCertificateResponse.getStatus();
        if (status != null && status.getResult().equals(STATUS_OK)) {
            X509DataInfoListType x509DataInfoList = readCardCertificateResponse.getX509DataInfoList();
            List<X509DataInfoListType.X509DataInfo> x509DataInfos = x509DataInfoList.getX509DataInfo();
            if (CollectionUtils.isNotEmpty(x509DataInfos)) {
                log.log(Level.INFO, "Certificate list size = " + x509DataInfos.size());

                x509Certificate = x509DataInfos.get(0).getX509Data().getX509Certificate();
            }
        }

        if (ArrayUtils.isEmpty(x509Certificate)) {
            throw new ConnectorCardCertificateReadException("Could not retrieve connector smart " +
                    "card certificate from the connector.");
        }

        return x509Certificate;
    }

    public X509Certificate retrieveSmcbCardCertificate(String cardHandle)
            throws ConnectorCardCertificateReadException {

        byte[] connector_cert_auth = readCardCertificate(cardHandle);
        X509Certificate x509Certificate;

        try {
            x509Certificate = CryptoLoader.getCertificateFromAsn1DERCertBytes(connector_cert_auth);
        } catch (Throwable e) {
            throw new ConnectorCardCertificateReadException("Error getting X509Certificate", e);
        }

        return x509Certificate;
    }
}
