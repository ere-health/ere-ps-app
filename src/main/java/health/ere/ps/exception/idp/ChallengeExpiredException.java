
package health.ere.ps.exception.idp;

public class ChallengeExpiredException extends IdpJoseException {

    public ChallengeExpiredException() {
        super("The given challenge is expired");
    }
}
