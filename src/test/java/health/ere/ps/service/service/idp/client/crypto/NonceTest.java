package health.ere.ps.service.idp.client.crypto;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import java.util.Base64;
import java.util.regex.Pattern;

import org.junit.jupiter.api.Test;

import health.ere.ps.service.idp.crypto.Nonce;

public class NonceTest {

    @Test
    public void testGetNonceAsBase64UrlEncodedString() {
        Nonce nonceGenerator = new Nonce();

        // Test with invalid random byte amount (less than minimum)
        try {
            nonceGenerator.getNonceAsBase64UrlEncodedString(0);
            fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            assertEquals("Amount of random bytes is expected to be between 1 and 512", e.getMessage());
        }

        // Test with invalid random byte amount (greater than maximum)
        try {
            nonceGenerator.getNonceAsBase64UrlEncodedString(513);
            fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            assertEquals("Amount of random bytes is expected to be between 1 and 512", e.getMessage());
        }
    }

    @Test
    public void testGetNonceAsHex() {
        Nonce nonceGenerator = new Nonce();

        // Test with valid string length (even)
        String nonce = nonceGenerator.getNonceAsHex(64);
        assertNotNull(nonce);
        assertTrue(Pattern.matches("[0-9A-Fa-f]+", nonce));
        assertEquals(64, nonce.length());

        // Test with minimum string length
        nonce = nonceGenerator.getNonceAsHex(2);
        assertNotNull(nonce);
        assertTrue(Pattern.matches("[0-9A-Fa-f]+", nonce));
        assertEquals(2, nonce.length());

        // Test with maximum string length
        nonce = nonceGenerator.getNonceAsHex(512);
        assertNotNull(nonce);
        assertTrue(Pattern.matches("[0-9A-Fa-f]+", nonce));
        assertEquals(512, nonce.length());

        // Test with invalid string length (less than minimum)
        try {
            nonceGenerator.getNonceAsHex(1);
            fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            assertEquals("Requested string length is expected to be between 2 and 512", e.getMessage());
        }

        // Test with invalid string length (greater than maximum)
        try {
            nonceGenerator.getNonceAsHex(513);
            fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            assertEquals("Requested string length is expected to be between 2 and 512", e.getMessage());
        }

        // Test with invalid string length (odd)
        try {
            nonceGenerator.getNonceAsHex(63);
            fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            assertEquals("Requested string length is expected to be even.", e.getMessage());
        }
    }
}
