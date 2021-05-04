
package health.ere.ps.exception.idp;

public class RequiredClaimException extends IdpJoseException {

    private static final long serialVersionUID = 6444996291760355447L;

    public RequiredClaimException(final String message) {
        super(message);
    }
}
