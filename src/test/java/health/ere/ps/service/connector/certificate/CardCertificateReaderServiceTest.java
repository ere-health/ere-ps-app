package health.ere.ps.service.connector.certificate;

import org.apache.commons.lang3.ArrayUtils;
import org.bouncycastle.crypto.CryptoException;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.Base64;

import javax.inject.Inject;

import health.ere.ps.config.AppConfig;
import health.ere.ps.exception.connector.ConnectorCardCertificateReadException;
import health.ere.ps.service.idp.crypto.CryptoLoader;
import io.quarkus.test.junit.QuarkusTest;

@QuarkusTest
class CardCertificateReaderServiceTest {
    @Inject
    CardCertificateReaderService cardCertificateReaderService;

    @Inject
    AppConfig appConfig;

    @Test
    void test_Successful_ReadCardCertificate_API_Call() throws ConnectorCardCertificateReadException {
        Assertions.assertTrue(ArrayUtils.isNotEmpty(
                cardCertificateReaderService.readCardCertificate(appConfig.getClientId(),
                        appConfig.getClientSystem(),
                        appConfig.getWorkplace(), appConfig.getCardHandle())),
                "Smart card certificate was retrieved");
    }

    @Test
    void test_Successful_X509Certificate_Creation_From_ReadCardCertificate_API_Call()
            throws ConnectorCardCertificateReadException, IOException, CertificateException,
            CryptoException {
        byte[] base64_Decoded_Asn1_DER_Format_CertBytes =
                cardCertificateReaderService.readCardCertificate(appConfig.getClientId(),
                        appConfig.getClientSystem(),
                        appConfig.getWorkplace(), appConfig.getCardHandle());
        Assertions.assertTrue(ArrayUtils.isNotEmpty(base64_Decoded_Asn1_DER_Format_CertBytes),
                "Smart card certificate was retrieved");

        X509Certificate x509Certificate = CryptoLoader.getCertificateFromAsn1DERCertBytes(
                base64_Decoded_Asn1_DER_Format_CertBytes);

        x509Certificate.checkValidity();
    }
}