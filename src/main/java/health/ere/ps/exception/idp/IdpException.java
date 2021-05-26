
package health.ere.ps.exception.idp;

public class IdpException extends Exception {

    private static final long serialVersionUID = 4956462897121513838L;

    public IdpException(final Exception e) {
        super(e);
    }

    public IdpException(final String s) {
        super(s);
    }
}
