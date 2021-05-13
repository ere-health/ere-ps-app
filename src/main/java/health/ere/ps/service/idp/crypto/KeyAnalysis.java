package health.ere.ps.service.idp.crypto;

import java.security.PublicKey;
import java.security.interfaces.ECPublicKey;

public class KeyAnalysis {
    private KeyAnalysis() {

    }

    public static boolean isEcKey(final PublicKey publicKey) {
        return publicKey instanceof ECPublicKey;
    }
}
