package health.ere.ps.service.connector.cards;

import de.gematik.ws.conn.cardservice.v821.GetPinStatusResponse;
import de.gematik.ws.conn.cardservice.v821.PinStatusEnum;
import de.gematik.ws.conn.cardservice.wsdl.v8_2.CardServicePortType;
import de.gematik.ws.conn.cardservice.wsdl.v8_2.FaultMessage;
import de.gematik.ws.conn.connectorcommon.v5.Status;
import de.gematik.ws.conn.connectorcontext.v2.ContextType;
import health.ere.ps.service.connector.provider.MultiConnectorServicesProvider;
import org.junit.jupiter.api.Test;

import java.math.BigInteger;

import static org.junit.jupiter.api.Assertions.fail;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class ConnectorCardsServiceUnitTest {

    @Test
    void testPinStatus() throws Exception {
        ConnectorCardsService connectorCardsService = new ConnectorCardsService();

        connectorCardsService.connectorServicesProvider = mock(MultiConnectorServicesProvider.class);
        CardServicePortType mock = mock(CardServicePortType.class);
        when(connectorCardsService.connectorServicesProvider.getCardServicePortType(any())).thenReturn(mock);
        when(connectorCardsService.connectorServicesProvider.getContextType(any())).thenReturn(new ContextType());

        GetPinStatusResponse pinStatusResponse = new GetPinStatusResponse();
        pinStatusResponse.setStatus(new Status());
        pinStatusResponse.setPinStatus(PinStatusEnum.VERIFIED);
        pinStatusResponse.setLeftTries(BigInteger.ONE);
        when(mock.getPinStatus(any())).thenReturn(pinStatusResponse);
        try {
            connectorCardsService.getPinStatus(null, null, null);
            verify(mock).getPinStatus(any());
        } catch (FaultMessage e) {
            fail();
        }
    }
}