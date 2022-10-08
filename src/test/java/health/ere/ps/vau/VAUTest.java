package health.ere.ps.vau;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.FileInputStream;
import java.io.IOException;
import java.math.BigInteger;
import java.security.KeyPair;
import java.security.KeyStore;
import java.util.logging.LogManager;

import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.xml.bind.DatatypeConverter;
import javax.xml.ws.BindingProvider;

import org.bouncycastle.crypto.InvalidCipherTextException;
import org.bouncycastle.crypto.params.ECDomainParameters;
import org.bouncycastle.crypto.params.ECPrivateKeyParameters;
import org.bouncycastle.crypto.params.ECPublicKeyParameters;
import org.bouncycastle.jcajce.provider.asymmetric.ec.BCECPrivateKey;
import org.bouncycastle.jcajce.provider.asymmetric.ec.BCECPublicKey;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.math.ec.ECPoint;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import de.gematik.ws.conn.certificateservice.wsdl.v6.CertificateService;
import de.gematik.ws.conn.certificateservice.wsdl.v6.CertificateServicePortType;
import de.gematik.ws.conn.connectorcontext.v2.ContextType;
import health.ere.ps.service.common.security.SecretsManagerService.KeyStoreType;
import health.ere.ps.service.common.security.SecretsManagerService.SslContextType;
import health.ere.ps.service.connector.endpoint.SSLUtilities;
import health.ere.ps.vau.VAU.KeyCoords;

public class VAUTest {


    @BeforeEach
    void init() {
        try {
            // https://community.oracle.com/thread/1307033?start=0&tstart=0
            LogManager.getLogManager().readConfiguration(
                VAUTest.class
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

    public static String Message = "Hallo Test";
    public static String CipherText = "01 754e548941e5cd073fed6d734578a484be9f0bbfa1b6fa3168ed7ffb22878f0f 9aef9bbd932a020d8828367bd080a3e72b36c41ee40c87253f9b1b0beb8371bf 257db4604af8ae0dfced37ce 86c2b491c7a8309e750b 4e6e307219863938c204dfe85502ee0a"
            .replace(" ", "").toUpperCase();
    public String CertPublicKeyX = "8634212830dad457ca05305e6687134166b9c21a65ffebf555f4e75dfb048888";
    public String CertPublicKeyY = "66e4b6843624cbda43c97ea89968bc41fd53576f82c03efa7d601b9facac2b29";
    public String EccPrivateKey = "5bbba34d47502bd588ed680dfa2309ca375eb7a35ddbbd67cc7f8b6b687a1c1d";
    public String EphemeralPublicKeyX = "754e548941e5cd073fed6d734578a484be9f0bbfa1b6fa3168ed7ffb22878f0f";
    public String EphemeralPublicKeyY = "9aef9bbd932a020d8828367bd080a3e72b36c41ee40c87253f9b1b0beb8371bf";
    public String IVBytes = "257db4604af8ae0dfced37ce";

    private static byte[] hexStringToByteArray(String hex) {
        return DatatypeConverter.parseHexBinary(hex);
    }

    protected KeyCoords getPublicKey(VAU vau) {
        return new KeyCoords(new BigInteger(CertPublicKeyX, 16), new BigInteger(CertPublicKeyY, 16));
    }

    protected KeyPair getKeyPair() {
        ECDomainParameters ecDomain = VAU.getECDomain();
        BigInteger x = new BigInteger(EphemeralPublicKeyX, 16);
        BigInteger y = new BigInteger(EphemeralPublicKeyY, 16);
        ECPoint q = VAU.x9EC.getCurve().createPoint(x, y);
        ECPublicKeyParameters pub = new ECPublicKeyParameters(q, ecDomain);
        ECPrivateKeyParameters priv = new ECPrivateKeyParameters(
                new BigInteger(hexStringToByteArray(EccPrivateKey)), ecDomain);

        return new KeyPair(new BCECPublicKey("EC", pub, BouncyCastleProvider.CONFIGURATION),
                new BCECPrivateKey("EC", priv, BouncyCastleProvider.CONFIGURATION));
    }

    @Test
    public void DemoBspAusGemSpecCrypt() throws IllegalStateException, InvalidCipherTextException {
        VAU vau = new VAU();
        byte[] gesamtoutput = vau.encrypt(Message, getKeyPair(), getPublicKey(vau),
                DatatypeConverter.parseHexBinary(IVBytes));
        String byteArrayToHexString = VAU.byteArrayToHexString(gesamtoutput);
        assertEquals(CipherText, byteArrayToHexString);
    }

    @Test
    public void testDecrypt() throws Exception {
        String cipherText = "58009E11B7D5C3B8010E46AD88B14B8F4AE1573330AA95269D0ECB015C8DBFF048215412A83727EB5F1D57C4716C3448858815E64B5814E315F44FF6148A664D510A5291A825451C726AD5ACEBD2977BB8C3933EBBD4A8ED914DFB71CCA461313105E2B196D5FDFE12AAD67AB749BD585E9E5907E09C6E912B37841355F87B8B34F5B8DD0A377DA060F236413981DDC5ED8E4E9C07FAD76174A5E6E1950CC21C595A38050C1887BF91163FCE29D4FA7FC274234E61CAF8DE6568FAEB4653F77A68FAD2FFBF81B236F7FD40C08E508FA1CB0EDB2455678CE8BA6721DDCA3E840F1A18744EB591C33750CC37E7641BBA6C2091C67506A9DAC1FACF1C2E05A277995A1316A6CEC7258EB054B4CE9FB7B78C820AE65F5BB50A9A78B71CAA3873E3B0DF34183B5E75BDDC88EE5FD00C1BEEC387740775D89CAECCC006A8DA055848C9D6849776AE6C4E287B777831DAE416038942C8561AD0BA5D4A996BF93D7257BFC814C694CC412649FDBFD700BCDAEAA4E4F5729564F1560C99049648B514B86E3B9C99069BFFACAB369BA208884D2DDFF48CBE3C660E48879AEBFD8844282666186B91D16E6B41614E54D8DC23BD5AFDB4DD5519F98BC171823F1B5C70056F06ACE2B8FF9FC80792D144DA3702526A58C1C492DBF66E0E5EAAA2CC3C700FEF80B8BAECD1BD5171279DAB307F236D9D9050C53E0E6EE5A7A8976124208C5713F1551DEB06C4EE7510F743C9A4280B172E2DA1BDCB427A4F358303EFCDB56470519A94E17302FF62DFA479370DEE15EB919F56514EE339EF2A72EDA75C062A";
        String ase128 = "42d731ad33d8bf6046caf42b4d25ef0f".toUpperCase();

        VAU.decryptWithKey(DatatypeConverter.parseHexBinary(cipherText), DatatypeConverter.parseHexBinary(ase128));
    }

    @Test
    public void testMake32ByteLong() throws Exception {
        byte[] bLength32 = new byte[] {
            0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,
            0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,
            0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,
            0x00,0x00
        };
        assertEquals(32, VAU.make32ByteLong(bLength32).length);

        byte[] bLength31 = new byte[] {
            0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,
            0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,
            0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,
            0x00
        };
        assertEquals(32, VAU.make32ByteLong(bLength31).length);

        byte[] bLength33 = new byte[] {
            0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,
            0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,
            0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,
            0x00,0x00,0x00
        };
        assertEquals(32, VAU.make32ByteLong(bLength33).length);
    }

    @Test
    @Disabled
    public void testOCSPF() throws Exception {
        ContextType contextType = new ContextType();
        contextType.setMandantId("Incentergy");
        contextType.setWorkplaceId("1786_A1");
        contextType.setClientSystemId("Incentergy");

        CertificateServicePortType service = new CertificateService(getClass()
                .getResource("/CertificateService_v6_0_1.wsdl")).getCertificateServicePort();

        BindingProvider bindingProvider = (BindingProvider) service;
        bindingProvider.getRequestContext().put(BindingProvider.ENDPOINT_ADDRESS_PROPERTY,
                "https://10.0.0.98:443/ws/CertificateService");

        String connectorTlsCertAuthStorePwd = "U9pRlw8SBfMExkycgNDs";
        FileInputStream certificateInputStream = new FileInputStream("/home/manuel/Desktop/RU-Connector-Cert/incentergy_U9pRlw8SBfMExkycgNDs.p12");

        SSLContext sslContext = SSLContext.getInstance(SslContextType.TLS.getSslContextType());

        KeyStore ks = KeyStore.getInstance(KeyStoreType.PKCS12.getKeyStoreType());
        ks.load(certificateInputStream, connectorTlsCertAuthStorePwd.toCharArray());

        KeyManagerFactory kmf = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
        kmf.init(ks, connectorTlsCertAuthStorePwd.toCharArray());

        sslContext.init(kmf.getKeyManagers(), new TrustManager[]{new SSLUtilities.FakeX509TrustManager()},
                null);

        bindingProvider.getRequestContext().put("com.sun.xml.ws.transport.https.client.SSLSocketFactory",
            sslContext.getSocketFactory());
        bindingProvider.getRequestContext().put("com.sun.xml.ws.transport.https.client.hostname.verifier",
                new SSLUtilities.FakeHostnameVerifier());
        VAU vau = new VAU("https://erp-ref.zentral.erp.splitdns.ti-dienste.de", contextType, service);
        vau.getVauPublicKeyXY();
    }

    @Test
    public void make32ByteLong() {
        assertArrayEquals(new byte[] {
            0,0,0,0,0,
            0,0,0,0,0,
            0,0,0,0,0,
            0,0,0,0,0,
            0,0,0,0,0,
            0,0,0,0,0,
            0, 1},
        VAU.make32ByteLong(new byte[] {
            0,0,0,0,0,
            0,0,0,0,0,
            0,0,0,0,0,
            0,0,0,0,0,
            0,0,0,0,0,
            0,0,0,0,0,
            0, 1}));
        
        assertArrayEquals(new byte[] {
            0,0,0,0,0,
            0,0,0,0,0,
            0,0,0,0,0,
            0,0,0,0,0,
            0,0,0,0,0,
            0,0,0,0,0,
            0, 1}, VAU.make32ByteLong(new byte[] {1}));

        assertArrayEquals(new byte[] {
            0,0,0,0,0,
            0,0,0,0,0,
            0,0,0,0,0,
            0,0,0,0,0,
            0,0,0,0,0,
            0,0,0,0,0,
            0, 1},
        VAU.make32ByteLong(new byte[] {
            1,2,3,4,5,
            0,0,0,0,0,
            0,0,0,0,0,
            0,0,0,0,0,
            0,0,0,0,0,
            0,0,0,0,0,
            0,0,0,0,0,
            0, 1}));
    }
}