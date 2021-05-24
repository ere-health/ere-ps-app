package health.ere.ps.vau;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.math.BigInteger;
import java.security.KeyPair;
import java.util.logging.Logger;

import javax.xml.bind.DatatypeConverter;

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
        ECPoint q = VAU.x9EC.getCurve().createPoint(x, y);
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
        byte[] gesamtoutput = vau.Encrypt(Message, getKeyPair(), getPublicKey(vau),
                DatatypeConverter.parseHexBinary(IVBytes));
        String byteArrayToHexString = VAU.ByteArrayToHexString(gesamtoutput);
        assertEquals(CipherText, byteArrayToHexString);
    }

    @Test
    public void testDecrypt() throws Exception {
        String cipherText = "944942B9780B0FBA77E7AE3CAC9534944AF7E57E022CB3C2FCC7E573F4E4E9F5FF1FF29734B775D3B6E94E516053F3DDE361218F26F78AF097F88AAA62B15B9375BC61C0F5F3A68F87288425F2ADCF08FD3831BE2960ADD1AC8DD15F73D026BC06275CD260DDF3EBB64A8C3CA3681EE4605DD742BF4A9F54818476677BF422339BE387F7F3FF8620E6305C2D51EC83BA7E89C12F11067BCA73C03E07754AA7D98562A2E2027D0882F5C8583815F37A58EF86FBF0E6E9F32F0818DF9CBF0ECD8628DEB7C99DC057DA0B1D790EA3662211E8463CCC54EAF96D8C9FFF7387CF0324808447C0DF958E02B3668882E9CBA69FC1D775FB21A32C8A67CBD9BCADA85721943E311F0FFD6B732E6E53CD84B5A6248613B7140C464C64DA4ADCCE58AF3A50426AD6B2183EF96D650848D0A1E8A21D1B9226C0F33D394492A1CE565139DE0501F98217710927B9F562093672770608A1D1F2DE9730F2463BCA984E9E1599051238F2C60F7CBCEA67D9B258339A95DD32D4214620927E1D4C9CFFE6376A12EDFA0EA10B13B87E811DFD19B09FE8737CFFD93BA461567810E5AB8E9703F39F98A62CDB93403C76F1A0E0E5C3AF3BD6AA0AB419BA084596F8C656CE3B5168E0E38170981A40536AE2BA292C02D69E56006604F88004121B034010B9CD0EA191070C1769868347BCEFE1810DD1B2DF10F175282BD99C505FCEC9314C1D9EC7299E39C776C0F6A5240233EFD82015DCEA832CECFBB9163354B758157BEE9EF6CE954185C67D08D1343CD51F58F3B13C5472BB2A3DF5EB0951E85EB63DA91A7C";
        String ase128 = "509C9D04F6FE580BF36437CBC4C5B572";
        VAU vau = new VAU();
        vau.decryptWithKey(DatatypeConverter.parseHexBinary(cipherText), DatatypeConverter.parseHexBinary(ase128));
        
    }

}