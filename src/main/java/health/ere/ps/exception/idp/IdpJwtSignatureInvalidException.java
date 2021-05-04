
package health.ere.ps.exception.idp;

public class IdpJwtSignatureInvalidException extends IdpJoseException {

    private static final long serialVersionUID = 3925993168784136178L;

    public IdpJwtSignatureInvalidException(final Exception e) {
        super("The JWT signature was invalid", false, e);
    }
}
