package health.ere.ps.model.erixa.api.credentials;

public class BasicAuthCredentials {

    private final String email;
    private final String password;

    public BasicAuthCredentials(String email, String password) {
        this.email = email;
        this.password = password;
    }

    public String getEmail() {
        return email;
    }

    public String getPassword() {
        return password;
    }
}
