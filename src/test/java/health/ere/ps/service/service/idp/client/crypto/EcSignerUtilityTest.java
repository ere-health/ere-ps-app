package health.ere.ps.service.idp.client.crypto;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import java.security.*;
import java.util.Arrays;

import org.junit.jupiter.api.Test;

import health.ere.ps.exception.idp.crypto.IdpCryptoException;
import health.ere.ps.service.idp.crypto.EcSignerUtility;

public class EcSignerUtilityTest {

    @Test
    public void testCreateEcSignatureAndVerify() {
        try {
            // Generate a key pair for testing
            KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance("EC");
            keyPairGenerator.initialize(256); // Use an appropriate key size
            KeyPair keyPair = keyPairGenerator.generateKeyPair();
            PrivateKey privateKey = keyPair.getPrivate();
            PublicKey publicKey = keyPair.getPublic();

            // Data to be signed
            byte[] dataToBeSigned = "Test data".getBytes();

            // Create an EC signature
            byte[] signature = EcSignerUtility.createEcSignature(dataToBeSigned, privateKey);

            assertNotNull(signature);
            assertTrue(signature.length > 0);

            // Verify the signature
            EcSignerUtility.verifyEcSignatureAndThrowExceptionWhenFail(dataToBeSigned, publicKey, signature);
        } catch (NoSuchAlgorithmException e) {
            fail("Exception should not be thrown: " + e.getMessage());
        }
    }

    @Test
    public void testVerifyEcSignatureWithInvalidSignature() {
        try {
            // Generate a key pair for testing
            KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance("EC");
            keyPairGenerator.initialize(256); // Use an appropriate key size
            KeyPair keyPair = keyPairGenerator.generateKeyPair();
            PrivateKey privateKey = keyPair.getPrivate();
            PublicKey publicKey = keyPair.getPublic();

            // Data to be signed
            byte[] dataToBeSigned = "Test data".getBytes();

            // Create an EC signature
            byte[] signature = EcSignerUtility.createEcSignature(dataToBeSigned, privateKey);

            assertNotNull(signature);
            assertTrue(signature.length > 0);

            // Modify the signature to make it invalid
            signature[0] = (byte) (signature[0] ^ 0xFF);

            // Verify the modified signature (should fail)
            try {
                EcSignerUtility.verifyEcSignatureAndThrowExceptionWhenFail(dataToBeSigned, publicKey, signature);
                fail("Expected IdpCryptoException not thrown.");
            } catch (IllegalStateException e) {
                assertTrue(e.getCause() instanceof IdpCryptoException);
            }
        } catch (NoSuchAlgorithmException e) {
            fail("Exception should not be thrown: " + e.getMessage());
        }
    }

    @Test
    public void testCreateEcSignatureWithInvalidPrivateKey() {
        try {
            // Generate a key pair for testing
            KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance("EC");
            keyPairGenerator.initialize(256); // Use an appropriate key size
            KeyPair keyPair = keyPairGenerator.generateKeyPair();
            PrivateKey privateKey = keyPair.getPrivate();
            PublicKey publicKey = keyPair.getPublic();

            // Data to be signed
            byte[] dataToBeSigned = "Test data".getBytes();

            // Use an invalid private key (null) for signing
            byte[] signature = EcSignerUtility.createEcSignature(dataToBeSigned, null);

            assertNull(signature);
        } catch (NoSuchAlgorithmException e) {
            fail("Exception should not be thrown: " + e.getMessage());
        }
    }
}
