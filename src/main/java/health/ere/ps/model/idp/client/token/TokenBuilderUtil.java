package health.ere.ps.model.idp.client.token;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.codec.digest.DigestUtils;

public class TokenBuilderUtil {

    private TokenBuilderUtil() {

    }

    public static String buildSubjectClaim(final String audClaim, final String idNummerClaim,
        final String serverSubjectSalt) {
        return Base64.encodeBase64URLSafeString(DigestUtils.sha256(
            audClaim + idNummerClaim + serverSubjectSalt
        ));
    }
}
