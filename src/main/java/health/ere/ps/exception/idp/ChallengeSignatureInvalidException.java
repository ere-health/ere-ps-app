
package health.ere.ps.exception.idp;

public class ChallengeSignatureInvalidException extends IdpJoseException {

    private static final String ERROR_MESSAGE = "The given challenge does not have a valid signature";

    public ChallengeSignatureInvalidException() {
        super(ERROR_MESSAGE);
    }

    public ChallengeSignatureInvalidException(final Exception e) {
        super(ERROR_MESSAGE, e);
    }
}
