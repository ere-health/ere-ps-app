package health.ere.ps.vau;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.math.BigInteger;
import java.net.MalformedURLException;
import java.net.URL;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.Arrays;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.xml.bind.DatatypeConverter;

import org.bouncycastle.asn1.ASN1ObjectIdentifier;
import org.bouncycastle.asn1.teletrust.TeleTrusTObjectIdentifiers;
import org.bouncycastle.asn1.x9.ECNamedCurveTable;
import org.bouncycastle.asn1.x9.X9ECParameters;
import org.bouncycastle.crypto.BasicAgreement;
import org.bouncycastle.crypto.InvalidCipherTextException;
import org.bouncycastle.crypto.agreement.ECDHBasicAgreement;
import org.bouncycastle.crypto.digests.SHA256Digest;
import org.bouncycastle.crypto.engines.AESEngine;
import org.bouncycastle.crypto.generators.HKDFBytesGenerator;
import org.bouncycastle.crypto.modes.GCMBlockCipher;
import org.bouncycastle.crypto.params.AEADParameters;
import org.bouncycastle.crypto.params.ECDomainParameters;
import org.bouncycastle.crypto.params.ECKeyParameters;
import org.bouncycastle.crypto.params.ECPrivateKeyParameters;
import org.bouncycastle.crypto.params.ECPublicKeyParameters;
import org.bouncycastle.crypto.params.HKDFParameters;
import org.bouncycastle.crypto.params.KeyParameter;
import org.bouncycastle.jcajce.provider.asymmetric.dsa.KeyPairGeneratorSpi;
import org.bouncycastle.jcajce.provider.asymmetric.ec.BCECPrivateKey;
import org.bouncycastle.jcajce.provider.asymmetric.ec.BCECPublicKey;
import org.bouncycastle.jcajce.provider.asymmetric.ec.KeyPairGeneratorSpi.ECDSA;
import org.bouncycastle.math.ec.ECCurve;
import org.bouncycastle.math.ec.ECPoint;

public class VAU {
    private String _useragent;
    private String _fachdienstUrl;

    static X9ECParameters x9EC = ECNamedCurveTable
            .getByOID(new ASN1ObjectIdentifier(TeleTrusTObjectIdentifiers.brainpoolP256r1.getId()));

    private static final Logger log = Logger.getLogger(VAU.class.getName());

    public VAU() {
    }

    public VAU(String useragent, String fachdienstUrl) {
        _useragent = useragent;
        _fachdienstUrl = fachdienstUrl;
    }

    SecureRandom _random = new SecureRandom();

    public byte[] GetRandom(int cntBytes) {
        byte[] keyBytes = new byte[cntBytes];
        _random.nextBytes(keyBytes);
        return keyBytes;
    }

    protected byte[] GetIv() {
        byte[] keyBytes = new byte[96 / 8];
        _random.nextBytes(keyBytes);
        return keyBytes;
    }

    protected KeyPair GenerateNewECDHKey() throws NoSuchAlgorithmException {
        // eigener Key
        KeyPairGenerator keyGenerator;
        keyGenerator = ECDSA.getInstance(TeleTrusTObjectIdentifiers.brainpoolP256r1.getId());
        KeyPair key = keyGenerator.generateKeyPair();
        return key;

    }

    protected KeyCoords GetVauPublicKeyXY() throws CertificateException, MalformedURLException, IOException {
        CertificateFactory certFactory = CertificateFactory.getInstance("X.509");
        X509Certificate z = (X509Certificate) certFactory
                .generateCertificate(new URL(_fachdienstUrl + "/VAUCertificate").openStream());
        ECPublicKeyParameters x = (ECPublicKeyParameters) z.getPublicKey();

        return new KeyCoords(new BigInteger(1, x.getQ().getXCoord().getEncoded()),
                new BigInteger(1, x.getQ().getYCoord().getEncoded()));
    }

    public byte[] encrypt(String message) throws NoSuchAlgorithmException, IllegalStateException,
            InvalidCipherTextException, CertificateException, MalformedURLException, IOException {
        KeyPair myECDHKey = GenerateNewECDHKey();
        KeyCoords vauPublicKeyXY = GetVauPublicKeyXY();
        return encrypt(message, myECDHKey, vauPublicKeyXY, null);
    }

    public static ECDomainParameters getECDomain() {
        ECDomainParameters ecDomain = new ECDomainParameters(x9EC.getCurve(), x9EC.getG(), x9EC.getN(), x9EC.getH(),
                x9EC.getSeed());
        return ecDomain;
    }

    public byte[] encrypt(String message, KeyPair myECDHKey, KeyCoords vauPublicKeyXY, byte[] ivBytes)
            throws IllegalStateException, InvalidCipherTextException {
        ECDomainParameters ecDomain = getECDomain();

        BCECPrivateKey myPrivate = (BCECPrivateKey) myECDHKey.getPrivate();
        BCECPublicKey myPublic = (BCECPublicKey) myECDHKey.getPublic();
        log.info("MY public X=" + ByteArrayToHexString(myPublic.getQ().getXCoord().getEncoded()));
        log.info("MY public Y=" + ByteArrayToHexString(myPublic.getQ().getYCoord().getEncoded()));
        log.info("MY private =" + ByteArrayToHexString(myPrivate.getD().toByteArray()));

        ECPoint point = x9EC.getCurve().createPoint(vauPublicKeyXY.X, vauPublicKeyXY.Y);
        ECPublicKeyParameters vauPublicKey = new ECPublicKeyParameters(point, ecDomain);
        log.info("VAU X=" + vauPublicKeyXY.X.toString(16));
        log.info("VAU Y=" + vauPublicKeyXY.Y.toString(16));

        // SharedSecret
        BasicAgreement aKeyAgree = new ECDHBasicAgreement();
        aKeyAgree.init(new ECPrivateKeyParameters(myPrivate.getD(), ecDomain));
        BigInteger sharedSecret = aKeyAgree.calculateAgreement(vauPublicKey);
        byte[] sharedSecretBytes = sharedSecret.toByteArray();
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
        }
        sharedSecretBytes = sharedSecretBytesCopy;
        log.info("SharedSecret=" + ByteArrayToHexString(sharedSecretBytes) + " " + sharedSecretBytes.length);

        // HKDF
        byte[] info = "ecies-vau-transport".getBytes();
        HKDFBytesGenerator hkdfBytesGenerator = new HKDFBytesGenerator(new SHA256Digest());
        hkdfBytesGenerator.init(new HKDFParameters(sharedSecretBytes, new byte[0], info));
        byte[] aes128Key_CEK = new byte[16];
        hkdfBytesGenerator.generateBytes(aes128Key_CEK, 0, aes128Key_CEK.length);
        log.info("Schlüsselableitung AES128Key=" + ByteArrayToHexString(aes128Key_CEK));

        // AES CGM
        byte[] input = message.getBytes();
        byte[] outputAESCGM = new byte[input.length + 16];

        // random IV
        byte[] iv = ivBytes == null ? GetIv() : ivBytes;
        log.info("IV =" + ByteArrayToHexString(iv));

        GCMBlockCipher cipher = new GCMBlockCipher(new AESEngine());
        AEADParameters parameters = new AEADParameters(new KeyParameter(aes128Key_CEK), 128, iv);
        cipher.init(true, parameters);
        int len = cipher.processBytes(input, 0, input.length, outputAESCGM, 0);
        int finalData = cipher.doFinal(outputAESCGM, len);

        log.info(len + " " + finalData);

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

    public class KeyCoords {
        public BigInteger X;
        public BigInteger Y;

        public KeyCoords(BigInteger X, BigInteger Y) {
            this.X = X;
            this.Y = Y;
        }
    }

    public static String ByteArrayToHexString(byte[] bytes) {
        return DatatypeConverter.printHexBinary(bytes);
    }

    public static byte[] HexStringToByteArray(String hex) {
        return DatatypeConverter.parseHexBinary(hex);
    }

    public byte[] decrypt(byte[] message) throws Exception {
        return null;
    }

    public byte[] decryptWithKey(byte[] message, byte[] key) throws Exception {
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
        byte[] nonSecretPayload = new byte[1];
        cipherStream.read(nonSecretPayload, 0, 1);
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
}
