
package health.ere.ps.exception.idp;

public class IdpJwtExpiredException extends IdpJoseException {

    private static final long serialVersionUID = -4315696977609167524L;

    public IdpJwtExpiredException(final Exception e) {
        super("The given JWT has expired and is no longer valid (exp is in the past)", false, e);
    }
}
