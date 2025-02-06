package health.ere.ps.service.connector.certificate;

import static org.junit.jupiter.api.Assertions.fail;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.security.cert.CertificateException;
import java.security.cert.CertificateExpiredException;
import java.security.cert.X509Certificate;
import java.util.Base64;

import org.bouncycastle.crypto.CryptoException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import de.gematik.ws.conn.certificateservice.wsdl.v6.CertificateServicePortType;
import de.gematik.ws.conn.certificateservice.wsdl.v6.FaultMessage;
import de.gematik.ws.conn.certificateservicecommon.v2.CertRefEnum;
import de.gematik.ws.conn.certificateservicecommon.v2.X509DataInfoListType;
import de.gematik.ws.conn.certificateservicecommon.v2.X509DataInfoListType.X509DataInfo.X509Data;
import de.gematik.ws.conn.connectorcommon.v5.Status;
import health.ere.ps.exception.connector.ConnectorCardCertificateReadException;
import health.ere.ps.exception.connector.ConnectorCardsException;
import health.ere.ps.service.connector.provider.MultiConnectorServicesProvider;
import jakarta.xml.ws.Holder;

class CardCertificateReaderServiceTest {


    @Test
    void test_Successful_X509Certificate_Creation_From_ReadCardCertificate_API_Call()
            throws ConnectorCardCertificateReadException, IOException, CertificateException,
            CryptoException, ConnectorCardsException {

        String smcbHandle = "MOCK";
        
        CardCertificateReaderService cardCertificateReaderService = new CardCertificateReaderService();
        cardCertificateReaderService.connectorServicesProvider = mock(MultiConnectorServicesProvider.class);

        CertificateServicePortType certificateServicePortType = mock(CertificateServicePortType.class);
        when(cardCertificateReaderService.connectorServicesProvider.getCertificateServicePortType(any())).thenReturn(certificateServicePortType);

        try {
            doAnswer(invocation -> {
                Holder<Status> status =  invocation.getArgument(4);
                status.value = new Status();
                status.value.setResult("OK");
                Holder<X509DataInfoListType> listTypeHolder =  invocation.getArgument(5);
                X509DataInfoListType x509DataInfoListType = new X509DataInfoListType();
                X509DataInfoListType.X509DataInfo x509DataInfo = new X509DataInfoListType.X509DataInfo();
                x509DataInfo.setCertRef(CertRefEnum.C_AUT);
                X509Data x509Data = new X509Data();
                x509Data.setX509SubjectName("CN=Bad ApothekeTEST-ONLY,2.5.4.42=#0c064a756c69616e,2.5.4.4=#0c084e756c6c6d617972,2.5.4.5=#13143830323736383833313130303030313136333532,O=3-SMC-B-Testkarte-883110000116352,STREET=Danziger Straße 13,2.5.4.17=#0c053337303833,L=Göttingen,C=DE");
                x509Data.setX509Certificate(Base64.getDecoder().decode("MIIFQzCCBCugAwIBAgIHAM78FtdLgDANBgkqhkiG9w0BAQsFADCBmjELMAkGA1UEBhMCREUxHzAdBgNVBAoMFmdlbWF0aWsgR21iSCBOT1QtVkFMSUQxSDBGBgNVBAsMP0luc3RpdHV0aW9uIGRlcyBHZXN1bmRoZWl0c3dlc2Vucy1DQSBkZXIgVGVsZW1hdGlraW5mcmFzdHJ1a3R1cjEgMB4GA1UEAwwXR0VNLlNNQ0ItQ0EyNCBURVNULU9OTFkwHhcNMjAwMTI0MDAwMDAwWhcNMjQxMjExMjM1OTU5WjCB3zELMAkGA1UEBhMCREUxEzARBgNVBAcMCkfDtnR0aW5nZW4xDjAMBgNVBBEMBTM3MDgzMRwwGgYDVQQJDBNEYW56aWdlciBTdHJhw59lIDEzMSowKAYDVQQKDCEzLVNNQy1CLVRlc3RrYXJ0ZS04ODMxMTAwMDAxMTYzNTIxHTAbBgNVBAUTFDgwMjc2ODgzMTEwMDAwMTE2MzUyMREwDwYDVQQEDAhOdWxsbWF5cjEPMA0GA1UEKgwGSnVsaWFuMR4wHAYDVQQDDBVCYWQgQXBvdGhla2VURVNULU9OTFkwggEiMA0GCSqGSIb3DQEBAQUAA4IBDwAwggEKAoIBAQClxjtfGfewCJXzQ5OoQf1WIqKlOxM7Mm6MM/TXz6BWlg+r2oRbfhAN5qQlDp+syYJ6O16GDyGpQ77gOvVOGCzXuNCewyGO0eTkoi7KGAdnuHwjVkFEbdQrmtS2+Lew7uQOAIumyRwIZB47gkai3GkbV1fWHnoJkBtXmu0hRwC+5i/xvQ395AnmHT+0miPWJdczM6zk4R6xOdfwBNzZypkDj71iZvjsxS5OgOTveQnACxx21mL9Rq6t9lxZIe/Waims0vlk2B1lTeb6LJaFIw9WKtX4il/K04xOpnHEeMrfk28TR1Y9zBhU+1OFD0OUrZnWfQFH2G4VVVZPLON5FykvAgMBAAGjggFFMIIBQTAdBgNVHQ4EFgQUT5Q1fsFIEmw+TTo3XayHmYxJ4HUwDAYDVR0TAQH/BAIwADA4BggrBgEFBQcBAQQsMCowKAYIKwYBBQUHMAGGHGh0dHA6Ly9laGNhLmdlbWF0aWsuZGUvb2NzcC8wDgYDVR0PAQH/BAQDAgQwMB8GA1UdIwQYMBaAFHrp4W/qFFkWBe4D6dP9Iave6dmeMCAGA1UdIAQZMBcwCgYIKoIUAEwEgSMwCQYHKoIUAEwETDCBhAYFKyQIAwMEezB5pCgwJjELMAkGA1UEBhMCREUxFzAVBgNVBAoMDmdlbWF0aWsgQmVybGluME0wSzBJMEcwFwwVw5ZmZmVudGxpY2hlIEFwb3RoZWtlMAkGByqCFABMBDYTITMtU01DLUItVGVzdGthcnRlLTg4MzExMDAwMDExNjM1MjANBgkqhkiG9w0BAQsFAAOCAQEADWgBHY1xU0C4ex8MhnLFkv3n3bZ204I4b0KCBP8FL7E4KVODvklyuL+QB8qhTEBtZFLUHePXJEyN3mFDgTZ4IE0WVbRmxbA53ipd1r3XPD4K9JGd63/EceBjBi47OXevbv8PEROAfEeskjcmqofbxsg/HGwVhPWTGmaynAgD5mwQ7Qown4jZgBgVioi3eDSUQfdvmgxHWCRHxsyEVPF/N0KHuxtFP1fzKlJV1Sf0+PBE/6lQPizvL9jwQDrnZqtXbb30JdqdYOums4qa9SrCX9IIMG7DJzQX3ZwJzkMRqVuF9r1wHv6idwZvZhShMabfwl7sQEwyFtNlNDVWCXwINw=="));
                x509DataInfo.setX509Data(x509Data);
                x509DataInfoListType.getX509DataInfo().add(x509DataInfo);
                listTypeHolder.value = x509DataInfoListType;
                
                return null;
            }).when(certificateServicePortType).readCardCertificate(any(), any(), any(), any(), any(), any());
        } catch (FaultMessage e) {
            fail();
        }

        X509Certificate x509Certificate =
                cardCertificateReaderService.retrieveSmcbCardCertificate(smcbHandle);
        Assertions.assertNotNull(x509Certificate,
                "Smart card certificate was retrieved");
        try {
            x509Certificate.checkValidity();
            fail();
        } catch(CertificateExpiredException ex) {

        }
    }
}