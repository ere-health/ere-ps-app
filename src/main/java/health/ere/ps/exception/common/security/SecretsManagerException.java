package health.ere.ps.exception.common.security;

public class SecretsManagerException extends Exception {

    public SecretsManagerException(String message) {
        super(message);
    }

    public SecretsManagerException(String message, Throwable e) {
        super(message, e);
    }
}
