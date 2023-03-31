package health.ere.ps.model.idp.client.field;

import java.util.Optional;
import java.util.stream.Stream;

public enum IdpScope {
    OPENID("openid"), EREZEPT("e-rezept"), EREZEPTDEV("e-rezept-dev"), PAIRING("pairing");

    private String jwtValue;

    IdpScope(String jwtValue) {
        this.jwtValue = jwtValue;
    }

    public static Optional<IdpScope> fromJwtValue(final String jwtValue) {
        return Stream.of(IdpScope.values())
            .filter(candidate -> candidate.getJwtValue().equals(jwtValue))
            .findAny();
    }

    public String getJwtValue() {
        return jwtValue;
    }
}
