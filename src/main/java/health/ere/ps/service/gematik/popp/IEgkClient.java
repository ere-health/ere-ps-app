package health.ere.ps.service.gematik.popp;

import java.util.List;

public interface IEgkClient {

    String getConnectedEgkCard();

    String startCardSession(final String cardHandle);

    void stopCardSession(final String sessionId);

    List<String> secureSendApdu(final String signedScenario);
}
