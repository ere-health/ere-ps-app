package health.ere.ps.vau;

import java.math.BigInteger;
import java.net.URL;
import java.security.KeyPairGenerator;
import java.security.SecureRandom;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.logging.Logger;

import org.bouncycastle.asn1.ASN1ObjectIdentifier;
import org.bouncycastle.asn1.teletrust.TeleTrusTObjectIdentifiers;
import org.bouncycastle.asn1.x9.ECNamedCurveTable;
import org.bouncycastle.asn1.x9.X9ECParameters;
import org.bouncycastle.crypto.agreement.ECDHBasicAgreement;
import org.bouncycastle.crypto.params.ECDomainParameters;
import org.bouncycastle.crypto.params.ECKeyParameters;
import org.bouncycastle.crypto.params.ECPrivateKeyParameters;
import org.bouncycastle.crypto.params.ECPublicKeyParameters;
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

    protected virtual byte[] GetIv() {
        byte[] keyBytes = new byte[96 / 8];
        _random.nextBytes(keyBytes);
        return keyBytes;
    }

    protected ECKeyParameters GenerateNewECDHKey() {
        // eigener Key
        KeyPairGenerator key = ECDSA.getInstance(TeleTrusTObjectIdentifiers.brainpoolP256r1.getId()); // .Create(ECCurve.NamedCurves.brainpoolP256r1);
        //var myexportParameters = key.ExportParameters(true);
        // return myexportParameters;
        return null;
    }

    protected KeyCoords GetVauPublicKeyXY() {
        CertificateFactory certFactory = CertificateFactory.getInstance("X.509");
        X509Certificate z = (X509Certificate) certFactory.generateCertificate(new URL(_fachdienstUrl+"/VAUCertificate").openStream());
        ECPublicKeyParameters x = (ECPublicKeyParameters) z.getPublicKey();

        return new KeyCoords {
            X = new BigInteger(1, x.getQ().getXCoord().getEncoded()),
            Y = new BigInteger(1, x.getQ().getYCoord().getEncoded())
        };
    }

    public byte[] Encrypt(String message) {
        X9ECParameters x9EC = ECNamedCurveTable.getByOID(new ASN1ObjectIdentifier(TeleTrusTObjectIdentifiers.brainpoolP256r1.getId()));
        ECDomainParameters ecDomain = new ECDomainParameters(x9EC.getCurve(), x9EC.getG(), x9EC.getN(), x9EC.getH(), x9EC.getSeed());

        ECKeyParameters myECDHKey = GenerateNewECDHKey();
        ECPrivateKeyParameters myPrivate = new ECPrivateKeyParameters(new BigInteger(1, myECDHKey.D), ecDomain);
        log.info("MY public X=" + ByteArrayToHexString(myECDHKey.Q.X));
        log.info("MY public Y=" + ByteArrayToHexString(myECDHKey.Q.Y));
        log.info("MY private =" + ByteArrayToHexString(myECDHKey.D));

        KeyCoords vauPublicKeyXY = GetVauPublicKeyXY();
        var point = x9EC.getCurve().createPoint(vauPublicKeyXY.X, vauPublicKeyXY.Y);
        ECPublicKeyParameters vauPublicKey = new ECPublicKeyParameters(point, ecDomain);
        log.info("VAU X=" + vauPublicKeyXY.X.toString(16));
        log.info("VAU Y=" + vauPublicKeyXY.Y.toString(16));

        //SharedSecret
        BasicAgreement aKeyAgree = new ECDHBasicAgreement();
        aKeyAgree.Init(myPrivate);
        BigInteger sharedSecret = aKeyAgree.CalculateAgreement(vauPublicKey);
        byte[] sharedSecretBytes = sharedSecret.ToByteArray().ToArray();

        //sharedSecretBytes muss 32 Byte groß sein entweder vorn abschneiden oder mit 0 auffüllen
        if (sharedSecretBytes.Length > 32) {
            sharedSecretBytes = sharedSecretBytes.Skip(sharedSecretBytes.Length - 32).ToArray();
        } else if (sharedSecretBytes.Length < 32) {
            sharedSecretBytes = Enumerable.Repeat((byte) 0, 32 - sharedSecretBytes.Length).Concat(sharedSecretBytes).ToArray();
        }
        log.info($"SharedSecret={ByteArrayToHexString(sharedSecretBytes)} {sharedSecretBytes.Length}");

        //HKDF
        byte[] info = Encoding.UTF8.GetBytes("ecies-vau-transport");
        HkdfBytesGenerator hkdfBytesGenerator = new HkdfBytesGenerator(new Sha256Digest());
        hkdfBytesGenerator.Init(new HkdfParameters(sharedSecretBytes, new byte[0], info));
        byte[] aes128Key_CEK = new byte[16];
        hkdfBytesGenerator.GenerateBytes(aes128Key_CEK, 0, aes128Key_CEK.Length);
        log.info("Schlüsselableitung AES128Key=" + ByteArrayToHexString(aes128Key_CEK));

        //AES CGM
        byte[] input = Encoding.UTF8.GetBytes(message);
        byte[] outputAESCGM = new byte[input.Length + 16];

        //random IV
        var iv = GetIv();
        log.info("IV =" + ByteArrayToHexString(iv));

        var cipher = new GcmBlockCipher(new AesEngine());
        var parameters = new AeadParameters(new KeyParameter(aes128Key_CEK), 128, iv);
        cipher.Init(true, parameters);
        var len = cipher.ProcessBytes(input, 0, input.Length, outputAESCGM, 0);
        var final = cipher.DoFinal(outputAESCGM, len);

        log.info(len + final);

        using var mem = new MemoryStream();
        mem.WriteByte(0x01); //Version
        mem.Write(myECDHKey.Q.X, 0, myECDHKey.Q.X.Length); //XKoordinate VAU Zert
        mem.Write(myECDHKey.Q.Y, 0, myECDHKey.Q.Y.Length); //YKoordinate VAU Zert
        mem.Write(iv, 0, iv.Length);
        mem.Write(outputAESCGM, 0, outputAESCGM.Length);
        return mem.ToArray();
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
        StringBuilder result = new StringBuilder(bytes.Length * 2);
        const String hexAlphabet = "0123456789ABCDEF";

        foreach (byte B in bytes) {
            result.Append(hexAlphabet[B >> 4]);
            result.Append(hexAlphabet[B & 0xF]);
        }

        return result.ToString();
    }

    public static byte[] HexStringToByteArray(String hex) {
        byte[] bytes = new byte[hex.Length / 2];
        int[] hexValue = {
            0x00, 0x01, 0x02, 0x03, 0x04, 0x05,
            0x06, 0x07, 0x08, 0x09, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
            0x0A, 0x0B, 0x0C, 0x0D, 0x0E, 0x0F
        };

        for (int x = 0, i = 0; i < hex.Length; i += 2, x += 1) {
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
