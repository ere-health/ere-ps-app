package health.ere.ps.service.connector.certificate;

import org.apache.commons.lang3.ArrayUtils;
import org.bouncycastle.crypto.CryptoException;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.Base64;
import java.util.logging.LogManager;

import javax.inject.Inject;

import health.ere.ps.config.AppConfig;
import health.ere.ps.exception.connector.ConnectorCardCertificateReadException;
import health.ere.ps.exception.connector.ConnectorCardsException;
import health.ere.ps.service.connector.cards.ConnectorCardsService;
import health.ere.ps.service.idp.crypto.CryptoLoader;
import io.quarkus.test.junit.QuarkusTest;

@QuarkusTest
class CardCertificateReaderServiceTest {

    @Inject
    ConnectorCardsService connectorCardsService;

    @Inject
    CardCertificateReaderService cardCertificateReaderService;

    @Inject
    AppConfig appConfig;

    @BeforeEach
    void init() {
        try {
            // https://community.oracle.com/thread/1307033?start=0&tstart=0
            LogManager.getLogManager().readConfiguration(
                CardCertificateReaderServiceTest.class
                            .getResourceAsStream("/logging.properties"));
        } catch (IOException e) {
            e.printStackTrace();
        }

        System.setProperty("com.sun.xml.ws.transport.http.client.HttpTransportPipe.dump", "true");
        System.setProperty("com.sun.xml.internal.ws.transport.http.client.HttpTransportPipe.dump", "true");
        System.setProperty("com.sun.xml.ws.transport.http.HttpAdapter.dump", "true");
        System.setProperty("com.sun.xml.internal.ws.transport.http.HttpAdapter.dump", "true");
        System.setProperty("com.sun.xml.ws.transport.http.HttpAdapter.dumpTreshold", "999999");
    }

    @Test
    void test_Successful_ReadCardCertificate_API_Call() throws ConnectorCardCertificateReadException, ConnectorCardsException {
        
        String smcbHandle = connectorCardsService.getConnectorCardHandle(
                ConnectorCardsService.CardHandleType.SMC_B);
        
        Assertions.assertTrue(ArrayUtils.isNotEmpty(
                cardCertificateReaderService.readCardCertificate(appConfig.getMandantId(),
                        appConfig.getClientSystem(),
                        appConfig.getWorkplace(), smcbHandle)),
                "Smart card certificate was retrieved");
    }

    @Test
    void test_Successful_X509Certificate_Creation_From_ReadCardCertificate_API_Call()
            throws ConnectorCardCertificateReadException, IOException, CertificateException,
            CryptoException, ConnectorCardsException {

                String smcbHandle = connectorCardsService.getConnectorCardHandle(
                ConnectorCardsService.CardHandleType.SMC_B);
        
                
        byte[] base64_Decoded_Asn1_DER_Format_CertBytes =
                cardCertificateReaderService.readCardCertificate(appConfig.getMandantId(),
                        appConfig.getClientSystem(),
                        appConfig.getWorkplace(), smcbHandle);
        Assertions.assertTrue(ArrayUtils.isNotEmpty(base64_Decoded_Asn1_DER_Format_CertBytes),
                "Smart card certificate was retrieved");

        X509Certificate x509Certificate = CryptoLoader.getCertificateFromAsn1DERCertBytes(
                base64_Decoded_Asn1_DER_Format_CertBytes);

        x509Certificate.checkValidity();
    }
}