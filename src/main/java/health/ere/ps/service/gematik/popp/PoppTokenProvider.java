package health.ere.ps.service.gematik.popp;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import de.gematik.zeta.sdk.WsClientExtension;
import health.ere.ps.model.popp.messages.ConnectorScenarioMessage;
import health.ere.ps.model.popp.messages.ErrorMessage;
import health.ere.ps.model.popp.messages.PoPPMessage;
import health.ere.ps.model.popp.messages.ScenarioResponseMessage;
import health.ere.ps.model.popp.messages.StartMessage;
import health.ere.ps.model.popp.messages.TokenMessage;

import java.util.logging.Logger;

import static health.ere.ps.model.popp.enums.CardConnectionType.CONTACT_CONNECTOR;

public class PoppTokenProvider {

    private static final Logger log = Logger.getLogger(PoppTokenProvider.class.getName());

    private static final ObjectMapper mapper = new ObjectMapper();

    private final IEgkClient egkClient;

    public PoppTokenProvider(IEgkClient egkClient) {
        this.egkClient = egkClient;
    }

    public String acquireToken(WsClientExtension.WsSession session, String egkHandle) throws JsonProcessingException {
        final var sessionId = start(session, egkHandle);
        PoPPMessage poppMessage = receive(session);
        while (poppMessage != null) {
            switch (poppMessage) {
                case final ConnectorScenarioMessage connectorScenarioMessage -> {
                    final var signedScenario = connectorScenarioMessage.getSignedScenario();
                    final var responses = egkClient.secureSendApdu(signedScenario);
                    ScenarioResponseMessage responseMessage = new ScenarioResponseMessage(responses);
                    String textFrame = mapper.writeValueAsString(responseMessage);
                    log.info("SENT text frame:\n" + textFrame);
                    session.sendText(textFrame);
                }
                case final TokenMessage tokenMessage -> {
                    egkClient.stopCardSession(sessionId);
                    return tokenMessage.getToken();
                }
                case final ErrorMessage errorMessage -> {
                    String errorCode = errorMessage.getErrorCode();
                    String errorDetail = errorMessage.getErrorDetail();
                    log.warning(String.format("Error message: %s, %s", errorCode, errorDetail));
                }
                default -> log.warning("Unknown message type: " + poppMessage.getType());
            }
            poppMessage = receive(session);
        }
        return null;
    }

    private String start(WsClientExtension.WsSession session, String egkHandle) throws JsonProcessingException {
        String sessionId = egkClient.startCardSession(egkHandle);

        StartMessage startMessage = StartMessage.builder()
            .version("1.0")
            .clientSessionId(sessionId)
            .cardConnectionType(CONTACT_CONNECTOR)
            .build();

        session.sendText(mapper.writeValueAsString(startMessage));
        return sessionId;
    }

    private PoPPMessage receive(WsClientExtension.WsSession session) throws JsonProcessingException {
        WsClientExtension.WsMessage msg = session.receiveNext();
        switch (msg) {
            case WsClientExtension.WsMessage.Text textMsg -> {
                log.info("RECEIVED text frame:\n" + textMsg.getText());
                return mapper.readValue(textMsg.getText(), PoPPMessage.class);
            }
            case WsClientExtension.WsMessage.Close ignored -> {
                log.info("WebSocket closed");
                return null;
            }
            case null -> {
                log.info("WebSocket closed");
                return null;
            }
            default -> {
                log.info("Unhandled message type: " + msg.getClass());
                return null;
            }
        }
    }
}