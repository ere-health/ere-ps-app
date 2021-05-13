
package health.ere.ps.exception.idp;

public class NoNestedJwtFoundException extends IdpJoseException {

    public NoNestedJwtFoundException() {
        super("Unable to find nested JWT");
    }
}
