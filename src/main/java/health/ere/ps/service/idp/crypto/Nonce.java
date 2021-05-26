package health.ere.ps.service.idp.crypto;

import health.ere.ps.exception.idp.crypto.IdpCryptoException;

import org.bouncycastle.util.encoders.Hex;

import java.util.Base64;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;

public class Nonce {

    private static final int NONCE_BYTE_AMOUNT_MIN = 1;
    private static final int NONCE_BYTE_AMOUNT_MAX = 512;
    private static final int NONCE_STRLEN_MIN = 2;
    private static final int NONCE_STRLEN_MAX = 512;

    public String getNonceAsBase64UrlEncodedString(final int randomByteAmount) {
        if (randomByteAmount < NONCE_BYTE_AMOUNT_MIN || randomByteAmount > NONCE_BYTE_AMOUNT_MAX) {
            throw new IllegalArgumentException(
                "Amount of random bytes is expected to be between " + NONCE_BYTE_AMOUNT_MIN + " and "
                    + NONCE_BYTE_AMOUNT_MAX);
        }

        final Random random = ThreadLocalRandom.current();
        final byte[] randomArray = new byte[randomByteAmount];
        random.nextBytes(randomArray);
        return new String(Base64.getUrlEncoder().withoutPadding().encode(randomArray));
    }

    public String getNonceAsHex(final int strlen) {
        if (strlen < NONCE_STRLEN_MIN || strlen > NONCE_STRLEN_MAX) {
            throw new IllegalArgumentException(
                "Requested string length is expected to be between " + NONCE_STRLEN_MIN + " and " + NONCE_STRLEN_MAX);
        }
        if (strlen % 2 != 0) {
            throw new IllegalArgumentException("Requested string length is expected to be even.");
        }
        final Random random = ThreadLocalRandom.current();
        final byte[] randomArray = new byte[strlen / 2];
        random.nextBytes(randomArray);
        return Hex.toHexString(randomArray);
    }

}
