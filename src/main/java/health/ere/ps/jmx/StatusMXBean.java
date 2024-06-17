package health.ere.ps.jmx;

@SuppressWarnings("unused") //used by jmx
public interface StatusMXBean {
    boolean[] isConnectorReachable();

    String[] getConnectorInformation();

    boolean[] isIdpReachable();

    String[] getIdpInformation();

    String[] getBearerToken();

    boolean[] isIdpaccesstokenObtainable();

    String[] getIdpaccesstokenInformation();

    boolean[] isSmcbAvailable();

    String[] getSmcbInformation();

    boolean[] isCautReadable();

    String[] getCautInformation();

    boolean[] isEhbaAvailable();

    String[] getEhbaInformation();

    boolean[] isComfortsignatureAvailable();

    String[] getComfortsignatureInformation();

    boolean[] isFachdienstReachable();

    String[] getFachdienstInformation();
}
