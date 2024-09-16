
package health.ere.ps.exception.idp;

import java.io.Serial;

public class IdpException extends Exception {

    @Serial
    private static final long serialVersionUID = 4956462897121513838L;

    public IdpException(final Exception e) {
        super(e);
    }

    public IdpException(final String s) {
        super(s);
    }
}
