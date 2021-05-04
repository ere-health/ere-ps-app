package health.ere.ps.service.idp.crypto;

import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.Signature;
import java.security.SignatureException;

import health.ere.ps.exception.idp.crypto.IdpCryptoException;

public class EcSignerUtility {

    private EcSignerUtility() {

    }

    public static byte[] createEcSignature(final byte[] toBeSignedData, final PrivateKey privateKey) {
        try {
            final Signature signer = Signature.getInstance("SHA256withECDSA");
            signer.initSign(privateKey);
            signer.update(toBeSignedData);
            return signer.sign();
        } catch (final NoSuchAlgorithmException | InvalidKeyException | SignatureException e) {
            throw new IdpCryptoException(e);
        }
    }

    public static void verifyEcSignatureAndThrowExceptionWhenFail(final byte[] toBeSignedData,
        final PublicKey publicKey,
        final byte[] signature) {
        try {
            final Signature signer = Signature.getInstance("SHA256withECDSA");
            signer.initVerify(publicKey);
            signer.update(toBeSignedData);
            if (!signer.verify(signature)) {
                throw new IdpCryptoException("Signature validation failed");
            }
        } catch (final NoSuchAlgorithmException | InvalidKeyException | SignatureException e) {
            throw new IdpCryptoException(e);
        }
    }
}
