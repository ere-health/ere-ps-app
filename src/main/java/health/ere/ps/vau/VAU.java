package health.ere.ps.vau;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigInteger;
import java.net.URL;
import java.security.InvalidAlgorithmParameterException;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.SecureRandom;
import java.security.Security;
import java.security.cert.CertificateEncodingException;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.Arrays;
import java.util.GregorianCalendar;
import java.util.TimeZone;
import java.util.logging.Logger;

import javax.xml.bind.DatatypeConverter;
import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;
import javax.xml.ws.Holder;

import org.bouncycastle.asn1.ASN1ObjectIdentifier;
import org.bouncycastle.asn1.teletrust.TeleTrusTObjectIdentifiers;
import org.bouncycastle.asn1.x9.X9ECParameters;
import org.bouncycastle.cert.ocsp.BasicOCSPResp;
import org.bouncycastle.cert.ocsp.CertificateID;
import org.bouncycastle.cert.ocsp.OCSPException;
import org.bouncycastle.cert.ocsp.OCSPResp;
import org.bouncycastle.cert.ocsp.RevokedStatus;
import org.bouncycastle.cert.ocsp.SingleResp;
import org.bouncycastle.crypto.BasicAgreement;
import org.bouncycastle.crypto.InvalidCipherTextException;
import org.bouncycastle.crypto.agreement.ECDHBasicAgreement;
import org.bouncycastle.crypto.digests.SHA256Digest;
import org.bouncycastle.crypto.engines.AESEngine;
import org.bouncycastle.crypto.generators.HKDFBytesGenerator;
import org.bouncycastle.crypto.modes.GCMBlockCipher;
import org.bouncycastle.crypto.params.AEADParameters;
import org.bouncycastle.crypto.params.ECDomainParameters;
import org.bouncycastle.crypto.params.ECPrivateKeyParameters;
import org.bouncycastle.crypto.params.ECPublicKeyParameters;
import org.bouncycastle.crypto.params.HKDFParameters;
import org.bouncycastle.crypto.params.KeyParameter;
import org.bouncycastle.jcajce.provider.asymmetric.ec.BCECPrivateKey;
import org.bouncycastle.jcajce.provider.asymmetric.ec.BCECPublicKey;
import org.bouncycastle.jce.ECNamedCurveTable;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.jce.spec.ECNamedCurveParameterSpec;
import org.bouncycastle.math.ec.ECPoint;

import de.gematik.ws.conn.certificateservice.v6.VerificationResultType;
import de.gematik.ws.conn.certificateservice.v6.VerifyCertificateResponse;
import de.gematik.ws.conn.certificateservice.wsdl.v6.CertificateServicePortType;
import de.gematik.ws.conn.certificateservice.wsdl.v6.FaultMessage;
import de.gematik.ws.conn.connectorcommon.v5.Status;
import de.gematik.ws.conn.connectorcontext.v2.ContextType;

public class VAU {
    private static final Logger log = Logger.getLogger(VAU.class.getName());
    static X9ECParameters x9EC = org.bouncycastle.asn1.x9.ECNamedCurveTable
            .getByOID(new ASN1ObjectIdentifier(TeleTrusTObjectIdentifiers.brainpoolP256r1.getId()));

    static {
        Security.addProvider(new BouncyCastleProvider());
    }

    private final SecureRandom secureRandom = new SecureRandom();
    String fachdienstUrl;
    CertificateServicePortType certificateService;
    ContextType contextType;

    public VAU() {
    }

    public VAU(String fachdienstUrl) {
        this.fachdienstUrl = fachdienstUrl;
    }

    public VAU(String fachdienstUrl, ContextType contextType, CertificateServicePortType certificateService) {
        this.fachdienstUrl = fachdienstUrl;
        this.certificateService = certificateService;
        this.contextType = contextType;
    }

    static ECDomainParameters getECDomain() {
        return new ECDomainParameters(x9EC.getCurve(), x9EC.getG(), x9EC.getN(), x9EC.getH(), x9EC.getSeed());
    }

    static String byteArrayToHexString(byte[] bytes) {
        return DatatypeConverter.printHexBinary(bytes);
    }

    static byte[] decryptWithKey(byte[] message, byte[] key) throws Exception {
        int MAC_BIT_SIZE = 128;
        int NONCE_BIT_SIZE = 96;
        int KEY_LENGTH = 128;

        if (key == null || key.length != KEY_LENGTH / 8) {
            throw new Exception("Key needs to be " + KEY_LENGTH + " bit!");
        }
        if (message == null || message.length == 0) {
            throw new Exception("Message required!");
        }

        var cipherStream = new ByteArrayInputStream(message);
        byte[] nonSecretPayload = new byte[0];
        cipherStream.read(nonSecretPayload, 0, 0);
        byte[] nonce = new byte[NONCE_BIT_SIZE / 8];
        cipherStream.read(nonce, 0, NONCE_BIT_SIZE / 8);
        var cipher = new GCMBlockCipher(new AESEngine());
        var parameters = new AEADParameters(new KeyParameter(key), MAC_BIT_SIZE, nonce, nonSecretPayload);
        cipher.init(false, parameters);
        byte[] cipherText = new byte[message.length - nonce.length];
        cipherStream.read(cipherText, 0, message.length - nonce.length);
        var plainText = new byte[cipher.getOutputSize(cipherText.length)];
        var len = cipher.processBytes(cipherText, 0, cipherText.length, plainText, 0);
        cipher.doFinal(plainText, len);

        return plainText;
    }

    byte[] getRandom(int cntBytes) {
        byte[] keyBytes = new byte[cntBytes];
        secureRandom.nextBytes(keyBytes);
        return keyBytes;
    }

    private byte[] getIv() {
        byte[] keyBytes = new byte[96 / 8];
        secureRandom.nextBytes(keyBytes);
        return keyBytes;
    }

    private KeyPair generateNewECDHKey() throws NoSuchAlgorithmException, NoSuchProviderException, InvalidAlgorithmParameterException {
        // eigener Key
        KeyPairGenerator keyGenerator;
        keyGenerator = KeyPairGenerator.getInstance("ECDH", BouncyCastleProvider.PROVIDER_NAME); //ECDSA.getInstance(TeleTrusTObjectIdentifiers.brainpoolP256r1.getId());
        // TeleTrusTObjectIdentifiers.brainpoolP256r1.
        ECNamedCurveParameterSpec parameterSpec = ECNamedCurveTable.getParameterSpec("brainpoolp256r1");
        keyGenerator.initialize(parameterSpec, secureRandom);

        return keyGenerator.generateKeyPair();
    }

    KeyCoords getVauPublicKeyXY() throws CertificateException, IOException, NoSuchProviderException {
        CertificateFactory certFactory = CertificateFactory.getInstance("X.509", BouncyCastleProvider.PROVIDER_NAME);
        X509Certificate z = (X509Certificate) certFactory
                .generateCertificate(new URL(fachdienstUrl + "/VAUCertificate").openStream());
        if(certificateService != null) {
            verifyCertificate(z);
        }
        BCECPublicKey x = (BCECPublicKey) z.getPublicKey();

        return new KeyCoords(new BigInteger(1, x.getQ().getXCoord().getEncoded()),
                new BigInteger(1, x.getQ().getYCoord().getEncoded()));
    }

    void verifyCertificate(X509Certificate z) {
        Holder<Status> status = new Holder<>();
        Holder<VerifyCertificateResponse.VerificationStatus> verificationStatus = new Holder<>();
        Holder<VerifyCertificateResponse.RoleList> arg5 = new Holder<>();
        XMLGregorianCalendar now = null;;
        try {
            now = DatatypeFactory.newInstance().newXMLGregorianCalendar(new GregorianCalendar(TimeZone.getTimeZone("UTC")));
        } catch (DatatypeConfigurationException e) {
            throw new IllegalStateException("Can not verify certificate from VAU", e);
        }
        try {
            certificateService.verifyCertificate(contextType, z.getEncoded(), now, status, verificationStatus, arg5);
        } catch (CertificateEncodingException | FaultMessage e) {
            throw new IllegalStateException("Can not verify certificate from VAU", e);
        }
        if(verificationStatus.value.getVerificationResult() != VerificationResultType.VALID) {
            throw new IllegalStateException("VAU certificate is not valid");
        }

        // Code based on: https://github.com/apache/nifi/blob/master/nifi-nar-bundles/nifi-framework-bundle/nifi-framework/nifi-web/nifi-web-security/src/main/java/org/apache/nifi/web/security/x509/ocsp/OcspCertificateValidator.java#L278
        InputStream ocspResponseStream;
        BasicOCSPResp basicOcspResponse;
        try {
            ocspResponseStream = new URL(fachdienstUrl + "/VAUCertificateOCSPResponse").openStream();
            OCSPResp oCSPResp = new OCSPResp(ocspResponseStream);
            basicOcspResponse = (BasicOCSPResp) oCSPResp.getResponseObject();
        } catch (IOException | OCSPException e2) {
            throw new IllegalArgumentException("Could not parse OCSP response", e2);
        }

        BigInteger subjectSerialNumber = z.getSerialNumber();
        // validate the response
        final SingleResp[] responses = basicOcspResponse.getResponses();
        for (SingleResp singleResponse : responses) {
            final CertificateID responseCertificateId = singleResponse.getCertID();
            final BigInteger responseSerialNumber = responseCertificateId.getSerialNumber();

            if (responseSerialNumber.equals(subjectSerialNumber)) {
                Object certStatus = singleResponse.getCertStatus();

                // interpret the certificate status
                if (certStatus instanceof RevokedStatus) {
                    throw new IllegalStateException("VAU certificate status is revoked");
                }
            }
        }
    }

    byte[] encrypt(String message) throws NoSuchAlgorithmException, IllegalStateException,
            InvalidCipherTextException, CertificateException, IOException, NoSuchProviderException,
            InvalidAlgorithmParameterException {
        KeyPair myECDHKey = generateNewECDHKey();
        KeyCoords vauPublicKeyXY = getVauPublicKeyXY();

        return encrypt(message, myECDHKey, vauPublicKeyXY, null);
    }

    byte[] encrypt(String message, KeyPair myECDHKey, KeyCoords vauPublicKeyXY, byte[] ivBytes)
            throws IllegalStateException, InvalidCipherTextException {
        ECDomainParameters ecDomain = getECDomain();

        BCECPrivateKey myPrivate = (BCECPrivateKey) myECDHKey.getPrivate();
        BCECPublicKey myPublic = (BCECPublicKey) myECDHKey.getPublic();
        log.fine("MY public X=" + byteArrayToHexString(myPublic.getQ().getXCoord().getEncoded()));
        log.fine("MY public Y=" + byteArrayToHexString(myPublic.getQ().getYCoord().getEncoded()));
        log.fine("MY private =" + byteArrayToHexString(myPrivate.getD().toByteArray()));

        ECPoint point = x9EC.getCurve().createPoint(vauPublicKeyXY.X, vauPublicKeyXY.Y);
        ECPublicKeyParameters vauPublicKey = new ECPublicKeyParameters(point, ecDomain);
        log.fine("VAU X=" + vauPublicKeyXY.X.toString(16));
        log.fine("VAU Y=" + vauPublicKeyXY.Y.toString(16));

        // SharedSecret
        BasicAgreement aKeyAgree = new ECDHBasicAgreement();
        aKeyAgree.init(new ECPrivateKeyParameters(myPrivate.getD(), ecDomain));
        BigInteger sharedSecret = aKeyAgree.calculateAgreement(vauPublicKey);
        byte[] sharedSecretBytes = sharedSecret.toByteArray();

        sharedSecretBytes = make32ByteLong(sharedSecretBytes);
        log.fine("SharedSecret=" + byteArrayToHexString(sharedSecretBytes) + " " + sharedSecretBytes.length);

        // HKDF
        byte[] info = "ecies-vau-transport".getBytes();
        HKDFBytesGenerator hkdfBytesGenerator = new HKDFBytesGenerator(new SHA256Digest());
        hkdfBytesGenerator.init(new HKDFParameters(sharedSecretBytes, new byte[0], info));
        byte[] aes128Key_CEK = new byte[16];
        hkdfBytesGenerator.generateBytes(aes128Key_CEK, 0, aes128Key_CEK.length);
        log.fine("Schlüsselableitung AES128Key=" + byteArrayToHexString(aes128Key_CEK));

        // AES CGM
        byte[] input = message.getBytes();
        byte[] outputAESCGM = new byte[input.length + 16];

        // random IV
        byte[] iv = ivBytes == null ? getIv() : ivBytes;
        log.fine("IV =" + byteArrayToHexString(iv));

        GCMBlockCipher cipher = new GCMBlockCipher(new AESEngine());
        AEADParameters parameters = new AEADParameters(new KeyParameter(aes128Key_CEK), 128, iv);
        cipher.init(true, parameters);
        int len = cipher.processBytes(input, 0, input.length, outputAESCGM, 0);
        int finalData = cipher.doFinal(outputAESCGM, len);

        log.fine(len + " " + finalData);

        ByteArrayOutputStream mem = new ByteArrayOutputStream();
        mem.write(0x01); // Version
        mem.write(myPublic.getQ().getXCoord().getEncoded(), 0, myPublic.getQ().getXCoord().getEncoded().length); // XKoordinate
        // VAU
        // Zert
        mem.write(myPublic.getQ().getYCoord().getEncoded(), 0, myPublic.getQ().getYCoord().getEncoded().length); // YKoordinate
        // VAU
        // Zert
        mem.write(iv, 0, iv.length);
        mem.write(outputAESCGM, 0, outputAESCGM.length);

        return mem.toByteArray();
    }

    public static byte[] make32ByteLong(byte[] sharedSecretBytes) {
        byte[] sharedSecretBytesCopy = new byte[32];
        // sharedSecretBytes muss 32 Byte groß sein entweder vorn abschneiden oder mit 0
        // auffüllen
        if (sharedSecretBytes.length > 32) {
            System.arraycopy(sharedSecretBytes, sharedSecretBytes.length - 32, sharedSecretBytesCopy, 0, 32);
        } else if (sharedSecretBytes.length < 32) {
            sharedSecretBytesCopy = Arrays.copyOfRange( // Source
                    sharedSecretBytes,
                    // The Start index
                    0,
                    // The end index
                    32);
        } else {
            sharedSecretBytesCopy = sharedSecretBytes;
        }
        sharedSecretBytes = sharedSecretBytesCopy;
        return sharedSecretBytes;
    }

    public static class KeyCoords {
        public BigInteger X;
        public BigInteger Y;

        public KeyCoords(BigInteger X, BigInteger Y) {
            this.X = X;
            this.Y = Y;
        }
    }
}