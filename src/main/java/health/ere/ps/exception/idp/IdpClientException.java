package health.ere.ps.exception.idp;

public class IdpClientException extends Exception {

    private static final long serialVersionUID = -3280232274428362763L;

    public IdpClientException(final Exception e) {
        super(e);
    }

    public IdpClientException(final String s) {
        super(s);
    }

    public IdpClientException(final String message, final Exception e) {
        super(message, e);
    }
}
