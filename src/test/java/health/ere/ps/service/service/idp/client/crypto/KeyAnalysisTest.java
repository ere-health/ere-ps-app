package health.ere.ps.service.idp.client.crypto;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.PublicKey;
import java.security.interfaces.ECPublicKey;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.ECParameterSpec;
import java.security.spec.ECPoint;

import org.junit.jupiter.api.Test;

import health.ere.ps.service.idp.crypto.KeyAnalysis;

public class KeyAnalysisTest {

    @Test
    public void testIsEcKeyWithEcPublicKey() throws Exception {
        // Generate an EC key pair
        KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance("EC");
        KeyPair keyPair = keyPairGenerator.generateKeyPair();
        PublicKey ecPublicKey = keyPair.getPublic();

        assertTrue(KeyAnalysis.isEcKey(ecPublicKey));
    }

    @Test
    public void testIsEcKeyWithRsaPublicKey() throws Exception {
        // Generate an RSA key pair
        KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance("RSA");
        KeyPair keyPair = keyPairGenerator.generateKeyPair();
        PublicKey rsaPublicKey = keyPair.getPublic();

        assertFalse(KeyAnalysis.isEcKey(rsaPublicKey));
    }

    @Test
    public void testIsEcKeyWithNullKey() {
        assertFalse(KeyAnalysis.isEcKey(null));
    }

    @Test
    public void testIsEcKeyWithNonPublicKey() {
        // Create a non-PublicKey object
        Object nonPublicKey = new Object();

        assertFalse(KeyAnalysis.isEcKey(nonPublicKey));
    }

    @Test
    public void testIsEcKeyWithCustomPublicKey() throws Exception {
        // Create a custom PublicKey class (for testing purposes)
        PublicKey customPublicKey = new CustomPublicKey();

        assertTrue(KeyAnalysis.isEcKey(customPublicKey));
    }

    // CustomPublicKey class for testing purposes
    private static class CustomPublicKey implements PublicKey, ECPublicKey {

        @Override
        public String getAlgorithm() {
            return "EC";
        }

        @Override
        public String getFormat() {
            return "X.509";
        }

        @Override
        public byte[] getEncoded() {
            return new byte[0];
        }

        @Override
        public ECPoint getW() {
            return null;
        }

        @Override
        public ECParameterSpec getParams() {
            // TODO Auto-generated method stub
            throw new UnsupportedOperationException("Unimplemented method 'getParams'");
        }
    }
}
