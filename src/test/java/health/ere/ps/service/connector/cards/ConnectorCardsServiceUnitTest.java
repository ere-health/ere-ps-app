package health.ere.ps.service.connector.cards;

import static org.junit.jupiter.api.Assertions.fail;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.Test;

import de.gematik.ws.conn.cardservice.wsdl.v8.CardServicePortType;
import de.gematik.ws.conn.cardservice.wsdl.v8.FaultMessage;
import de.gematik.ws.conn.connectorcontext.v2.ContextType;
import health.ere.ps.exception.connector.ConnectorCardsException;
import health.ere.ps.service.connector.provider.MultiConnectorServicesProvider;

class ConnectorCardsServiceUnitTest {

    @Test
    void testPinStatus() throws ConnectorCardsException {
        ConnectorCardsService connectorCardsService = new ConnectorCardsService();

        connectorCardsService.connectorServicesProvider = mock(MultiConnectorServicesProvider.class);
        CardServicePortType mock = mock(CardServicePortType.class);
        when(connectorCardsService.connectorServicesProvider.getCardServicePortType(any())).thenReturn(mock);
        when(connectorCardsService.connectorServicesProvider.getContextType(any())).thenReturn(new ContextType());
        try {
            connectorCardsService.getPinStatus(null, null, null);
            verify(mock).getPinStatus(any(), any(), any(), any(), any(), any());
        } catch (FaultMessage e) {
            fail();
        }
        
    }

}