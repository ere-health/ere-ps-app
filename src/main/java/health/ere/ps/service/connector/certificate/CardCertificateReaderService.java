package health.ere.ps.service.connector.certificate;

import de.gematik.ws.conn.certificateservice.v6.ReadCardCertificateResponse;
import de.gematik.ws.conn.certificateservicecommon.v2.X509DataInfoListType;
import de.gematik.ws.conn.connectorcommon.v5.Status;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.ArrayUtils;

import java.io.ByteArrayInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.cert.X509Certificate;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import health.ere.ps.config.AppConfig;
import health.ere.ps.exception.common.security.SecretsManagerException;
import health.ere.ps.exception.connector.ConnectorCardCertificateReadException;
import health.ere.ps.exception.idp.crypto.IdpCryptoException;
import health.ere.ps.model.idp.crypto.PkiIdentity;
import health.ere.ps.service.common.security.SecretsManagerService;
import health.ere.ps.service.idp.crypto.CryptoLoader;

@ApplicationScoped
public class CardCertificateReaderService {

    private static Logger log = Logger.getLogger(CardCertificateReaderService.class.getName());

    @Inject
    CardCertReadExecutionService cardCertReadExecutionService;

    @Inject
    SecretsManagerService secretsManagerService;

    @Inject
    AppConfig appConfig;

    private static final String STATUS_OK = "OK";

    /**
     * Reads the AUT certificate of a card managed in the connector.
     *
     * @param invocationContext The context for the call to the connector.
     * @param cardHandle        The handle of the card.
     * @return The card's AUT certificate.
     */
    public byte[] readCardCertificate(InvocationContext invocationContext, String cardHandle)
            throws ConnectorCardCertificateReadException {
        byte[] x509Certificate = new byte[0];

        ReadCardCertificateResponse readCardCertificateResponse =
                cardCertReadExecutionService.doReadCardCertificate(invocationContext, cardHandle);

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

    public byte[] readCardCertificate(String mandantId, String clientSystem, String workplace,
                                      String cardHandle) throws ConnectorCardCertificateReadException {
        return readCardCertificate(new InvocationContext(mandantId, clientSystem, workplace),
                cardHandle);
    }

    public byte[] readCardCertificate(String mandantId, String clientSystem, String workplace,
                                      String userId, String cardHandle)
            throws ConnectorCardCertificateReadException {
        return readCardCertificate(new InvocationContext(mandantId, clientSystem, workplace, userId),
                cardHandle);
    }

    public PkiIdentity retrieveCardCertIdentity(String p12FilePath, String password)
            throws ConnectorCardCertificateReadException, IdpCryptoException, SecretsManagerException {
        PkiIdentity identity;

        try (InputStream is = new FileInputStream(p12FilePath)) {
            identity = CryptoLoader.getIdentityFromP12(is, password);

        } catch (Throwable e) {

            throw new ConnectorCardCertificateReadException("Error getting C_AUTH PKI Identity", e);
        }

        return identity;
    }

    public X509Certificate retrieveSmcbCardCertificate(String mandantId, String clientSystem,
                                                    String workplace, String cardHandle)
            throws ConnectorCardCertificateReadException {

        byte[] connector_cert_auth = readCardCertificate(mandantId, clientSystem, workplace,
                cardHandle);
        X509Certificate x509Certificate;

        try {
            x509Certificate = CryptoLoader.getCertificateFromAsn1DERCertBytes(connector_cert_auth);

        } catch (Throwable e) {

            throw new ConnectorCardCertificateReadException("Error getting X509Certificate", e);
        }

        return x509Certificate;
    }
}
