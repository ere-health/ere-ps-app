package health.ere.ps.exception.idp;

import java.io.Serial;

public class IdpClientException extends Exception {

    @Serial
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
