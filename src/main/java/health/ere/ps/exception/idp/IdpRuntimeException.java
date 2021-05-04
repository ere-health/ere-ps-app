
package health.ere.ps.exception.idp;

public class IdpRuntimeException extends RuntimeException {

    private static final long serialVersionUID = 4956462897121513838L;

    public IdpRuntimeException(final Exception e) {
        super(e);
    }

    public IdpRuntimeException(final String s) {
        super(s);
    }
}
