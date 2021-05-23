package health.ere.ps.vau;

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
import java.util.logging.Level;
import java.util.logging.Logger;

import org.bouncycastle.asn1.ASN1ObjectIdentifier;
import org.bouncycastle.asn1.teletrust.TeleTrusTObjectIdentifiers;
import org.bouncycastle.asn1.x9.ECNamedCurveTable;
import org.bouncycastle.asn1.x9.X9ECParameters;
import org.bouncycastle.crypto.BasicAgreement;
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
import org.bouncycastle.jcajce.provider.asymmetric.ec.KeyPairGeneratorSpi.ECDSA;
import org.bouncycastle.math.ec.ECCurve;

public class VAU {
    private String _useragent;
    private String _fachdienstUrl;

    private static final Logger log = Logger.getLogger(VAU.class.getName());

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
        X509Certificate z = (X509Certificate) certFactory.generateCertificate(new URL(_fachdienstUrl+"/VAUCertificate").openStream());
        ECPublicKeyParameters x = (ECPublicKeyParameters) z.getPublicKey();

        return new KeyCoords(
            new BigInteger(1, x.getQ().getXCoord().getEncoded()),
            new BigInteger(1, x.getQ().getYCoord().getEncoded())
            );
    }

    public byte[] Encrypt(String message) {
        X9ECParameters x9EC = ECNamedCurveTable.getByOID(new ASN1ObjectIdentifier(TeleTrusTObjectIdentifiers.brainpoolP256r1.getId()));
        ECDomainParameters ecDomain = new ECDomainParameters(x9EC.getCurve(), x9EC.getG(), x9EC.getN(), x9EC.getH(), x9EC.getSeed());

        KeyPair myECDHKey = GenerateNewECDHKey();
        ECPrivateKeyParameters myPrivate = (ECPrivateKeyParameters) myECDHKey.getPrivate();
        ECPublicKeyParameters myPublic = (ECPublicKeyParameters) myECDHKey.getPublic();
        log.info("MY public X=" + ByteArrayToHexString(myPublic.getQ().getXCoord().getEncoded()));
        log.info("MY public Y=" + ByteArrayToHexString(myPublic.getQ().getYCoord().getEncoded()));
        log.info("MY private =" + ByteArrayToHexString(myPrivate.getD().toByteArray()));

        KeyCoords vauPublicKeyXY = GetVauPublicKeyXY();
        var point = x9EC.getCurve().createPoint(vauPublicKeyXY.X, vauPublicKeyXY.Y);
        ECPublicKeyParameters vauPublicKey = new ECPublicKeyParameters(point, ecDomain);
        log.info("VAU X=" + vauPublicKeyXY.X.toString(16));
        log.info("VAU Y=" + vauPublicKeyXY.Y.toString(16));

        //SharedSecret
        BasicAgreement aKeyAgree = new ECDHBasicAgreement();
        aKeyAgree.init(myPrivate);
        BigInteger sharedSecret = aKeyAgree.calculateAgreement(vauPublicKey);
        byte[] sharedSecretBytes = sharedSecret.toByteArray();

        //sharedSecretBytes muss 32 Byte groß sein entweder vorn abschneiden oder mit 0 auffüllen
        if (sharedSecretBytes.length > 32) {
            sharedSecretBytes = sharedSecretBytes.Skip(sharedSecretBytes.length - 32).ToArray();
        } else if (sharedSecretBytes.length < 32) {
            sharedSecretBytes = Enumerable.Repeat((byte) 0, 32 - sharedSecretBytes.Length).Concat(sharedSecretBytes).ToArray();
        }
        log.info($"SharedSecret={ByteArrayToHexString(sharedSecretBytes)} {sharedSecretBytes.Length}");

        //HKDF
        byte[] info = "ecies-vau-transport".getBytes();
        HKDFBytesGenerator hkdfBytesGenerator = new HKDFBytesGenerator(new SHA256Digest());
        hkdfBytesGenerator.init(new HKDFParameters(sharedSecretBytes, new byte[0], info));
        byte[] aes128Key_CEK = new byte[16];
        hkdfBytesGenerator.generateBytes(aes128Key_CEK, 0, aes128Key_CEK.length);
        log.info("Schlüsselableitung AES128Key=" + ByteArrayToHexString(aes128Key_CEK));

        //AES CGM
        byte[] input = message.getBytes();
        byte[] outputAESCGM = new byte[input.length + 16];

        //random IV
        var iv = GetIv();
        log.info("IV =" + ByteArrayToHexString(iv));

        var cipher = new GCMBlockCipher(new AESEngine());
        var parameters = new AEADParameters(new KeyParameter(aes128Key_CEK), 128, iv);
        cipher.init(true, parameters);
        var len = cipher.processBytes(input, 0, input.length, outputAESCGM, 0);
        var finalData = cipher.doFinal(outputAESCGM, len);

        log.info(len + " " + finalData);

        var mem = new ByteArrayOutputStream();
        mem.write(0x01); //Version
        mem.write(myPublic.getQ().getXCoord().getEncoded(), 0, myPublic.getQ().getXCoord().getEncoded().length); //XKoordinate VAU Zert
        mem.write(myPublic.getQ().getYCoord().getEncoded(), 0, myPublic.getQ().getYCoord().getEncoded().length); //YKoordinate VAU Zert
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
        StringBuilder result = new StringBuilder(bytes.length * 2);
        char[] hexAlphabet = new char[] {'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'A', 'B', 'C', 'D', 'E', 'F'};

        for(byte B : bytes) {
            result.append(hexAlphabet[B >> 4]);
            result.append(hexAlphabet[B & 0xF]);
        }

        return result.toString();
    }

    public static byte[] HexStringToByteArray(String hex) {
        byte[] bytes = new byte[hex.length / 2];
        int[] hexValue = {
            0x00, 0x01, 0x02, 0x03, 0x04, 0x05,
            0x06, 0x07, 0x08, 0x09, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
            0x0A, 0x0B, 0x0C, 0x0D, 0x0E, 0x0F
        };

        for (int x = 0, i = 0; i < hex.length; i += 2, x += 1) {
            bytes[x] = (byte) (hexValue[char.ToUpper(hex[i + 0]) - '0'] << 4 |
                               hexValue[char.ToUpper(hex[i + 1]) - '0']);
        }
        return bytes;
    }

    public byte[] DecryptWithKey(byte[] message, byte[] key) {
        const int MAC_BIT_SIZE = 128;
        const int NONCE_BIT_SIZE = 96;
        const int KEY_LENGTH = 128;

        if (key == null || key.Length != KEY_LENGTH / 8) {
            throw new Exception($"Key needs to be {KEY_LENGTH} bit!");
        }
        if (message == null || message.Length == 0) {
            throw new Exception("Message required!");
        }

        using var cipherStream = new MemoryStream(message);
        using var cipherReader = new BinaryReader(cipherStream);
        var nonSecretPayload = cipherReader.ReadBytes(0);
        var nonce = cipherReader.ReadBytes(NONCE_BIT_SIZE / 8);
        var cipher = new GcmBlockCipher(new AesEngine());
        var parameters = new AeadParameters(new KeyParameter(key), MAC_BIT_SIZE, nonce, nonSecretPayload);
        cipher.Init(false, parameters);
        var cipherText = cipherReader.ReadBytes(message.Length - nonce.Length);
        var plainText = new byte[cipher.GetOutputSize(cipherText.Length)];
        try {
            var len = cipher.ProcessBytes(cipherText, 0, cipherText.Length, plainText, 0);
            cipher.DoFinal(plainText, len);
        } catch (InvalidCipherTextException) {
            return null;
        }
        return plainText;
    }
}
