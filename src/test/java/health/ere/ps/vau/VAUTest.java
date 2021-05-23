package health.ere.ps.vau;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.math.BigInteger;
import java.security.KeyPair;
import java.util.logging.Logger;

import org.bouncycastle.math.ec.ECPoint;
import org.junit.jupiter.api.Test;
import org.bouncycastle.crypto.InvalidCipherTextException;
import org.bouncycastle.crypto.params.ECDomainParameters;
import org.bouncycastle.crypto.params.ECPrivateKeyParameters;
import org.bouncycastle.crypto.params.ECPublicKeyParameters;
import org.bouncycastle.jcajce.provider.asymmetric.ec.BCECPrivateKey;
import org.bouncycastle.jcajce.provider.asymmetric.ec.BCECPublicKey;
import org.bouncycastle.jce.provider.BouncyCastleProvider;

import health.ere.ps.vau.VAU.KeyCoords;

public class VAUTest {

    private static final Logger log = Logger.getLogger(VAUTest.class.getName());

    protected KeyCoords getPublicKey(VAU vau) {
        return vau.new KeyCoords(new BigInteger(CertPublicKeyX, 16), new BigInteger(CertPublicKeyY, 16));
    }

    protected KeyPair getKeyPair() {
        ECDomainParameters ecDomain = VAU.getECDomain();
        BigInteger x = new BigInteger(EphemeralPublicKeyX, 16);
        BigInteger y = new BigInteger(EphemeralPublicKeyY, 16);
        ECPoint q = VAU.x9EC.getCurve().createPoint(x,
                y);
        ECPublicKeyParameters pub = (ECPublicKeyParameters) new ECPublicKeyParameters(q, ecDomain);
        ECPrivateKeyParameters priv = (ECPrivateKeyParameters) new ECPrivateKeyParameters(
                new BigInteger(VAU.HexStringToByteArray(EccPrivateKey)), ecDomain);

        return new KeyPair(new BCECPublicKey("EC", pub, BouncyCastleProvider.CONFIGURATION),
                new BCECPrivateKey("EC", priv, BouncyCastleProvider.CONFIGURATION));
    }

    protected byte[] GetIv() {
        return VAU.HexStringToByteArray(IVBytes);
    }

    public String CertPublicKeyX = "8634212830dad457ca05305e6687134166b9c21a65ffebf555f4e75dfb048888";
    public String CertPublicKeyY = "66e4b6843624cbda43c97ea89968bc41fd53576f82c03efa7d601b9facac2b29";

    public static String Message = "Hallo Test";

    public String EccPrivateKey = "5bbba34d47502bd588ed680dfa2309ca375eb7a35ddbbd67cc7f8b6b687a1c1d";
    public String EphemeralPublicKeyX = "754e548941e5cd073fed6d734578a484be9f0bbfa1b6fa3168ed7ffb22878f0f";
    public String EphemeralPublicKeyY = "9aef9bbd932a020d8828367bd080a3e72b36c41ee40c87253f9b1b0beb8371bf";

    public String IVBytes = "257db4604af8ae0dfced37ce";

    public static String CipherText = "01 754e548941e5cd073fed6d734578a484be9f0bbfa1b6fa3168ed7ffb22878f0f 9aef9bbd932a020d8828367bd080a3e72b36c41ee40c87253f9b1b0beb8371bf 257db4604af8ae0dfced37ce 86c2b491c7a8309e750b 4e6e307219863938c204dfe85502ee0a"
            .replace(" ", "").toUpperCase();

    @Test
    public void DemoBspAusGemSpecCrypt() throws IllegalStateException, InvalidCipherTextException {
        VAU vau = new VAU();
        byte[] gesamtoutput = vau.Encrypt(Message, getKeyPair(), getPublicKey(vau));
        String byteArrayToHexString = VAU.ByteArrayToHexString(gesamtoutput);
        assertEquals(CipherText, byteArrayToHexString);
    }

}
