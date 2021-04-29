package health.ere.ps.model.idp.client.authentication;

import org.jose4j.jws.JsonWebSignature;
import org.jose4j.jwt.JwtClaims;
import org.jose4j.lang.JoseException;

import health.ere.ps.model.idp.client.brainPoolExtension.BrainpoolAlgorithmSuiteIdentifiers;
import health.ere.ps.model.idp.client.field.ClaimName;
import health.ere.ps.model.idp.client.token.JsonWebToken;
import health.ere.ps.model.idp.crypto.PkiIdentity;
import health.ere.ps.service.idp.crypto.KeyAnalysis;

import static org.jose4j.jws.AlgorithmIdentifiers.RSA_PSS_USING_SHA256;

public class AuthenticationResponseBuilder {

    public AuthenticationResponse buildResponseForChallenge(
        final AuthenticationChallenge authenticationChallenge,
        final PkiIdentity clientIdentity) {
        final JwtClaims claims = new JwtClaims();
        claims.setClaim(ClaimName.NESTED_JWT.getJoseName(), authenticationChallenge.getChallenge().getRawString());

        final JsonWebSignature jsonWebSignature = new JsonWebSignature();
        jsonWebSignature.setPayload(claims.toJson());

        if (KeyAnalysis.isEcKey(clientIdentity.getCertificate().getPublicKey())) {
            jsonWebSignature.setAlgorithmHeaderValue(BrainpoolAlgorithmSuiteIdentifiers.BRAINPOOL256_USING_SHA256);
        } else {
            jsonWebSignature.setAlgorithmHeaderValue(RSA_PSS_USING_SHA256);
        }
        jsonWebSignature.setKey(clientIdentity.getPrivateKey());

        jsonWebSignature.setHeader("typ", "JWT");
        jsonWebSignature.setHeader("cty", "NJWT");
        jsonWebSignature.setCertificateChainHeaderValue(clientIdentity.getCertificate());

        try {
            final String compactSerialization = jsonWebSignature.getCompactSerialization();
            return AuthenticationResponse.builder()
                .signedChallenge(new JsonWebToken(compactSerialization))
                .build();
        } catch (final JoseException e) {
            throw new RuntimeException(e);
        }
    }
}
