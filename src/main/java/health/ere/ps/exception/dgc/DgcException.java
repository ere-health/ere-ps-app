package health.ere.ps.exception.dgc;

public abstract class DgcException extends RuntimeException {
    private int code;

    public DgcException(int code, String message) {
        super(message);
        this.code = code;
    }

    public int getCode() {
        return code;
    }
}
