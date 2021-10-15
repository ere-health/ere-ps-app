package health.ere.ps.service.gematik;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import javax.enterprise.event.Event;

import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import de.gematik.ws.conn.connectorcontext.v2.ContextType;
import de.gematik.ws.conn.signatureservice.wsdl.v7.SignatureServicePortTypeV755;
import health.ere.ps.event.ActivateComfortSignatureEvent;
import health.ere.ps.event.GetSignatureModeResponseEvent;
import health.ere.ps.exception.gematik.ERezeptWorkflowException;
import health.ere.ps.service.connector.cards.ConnectorCardsService;
import health.ere.ps.service.connector.provider.MultiConnectorServicesProvider;

public class ERezeptWorkflowServiceUnitTest {

    @Test
    void testActivateComfortSignatureUnit() throws ERezeptWorkflowException {
        ERezeptWorkflowService eRezeptWorkflowServiceUnit = new ERezeptWorkflowService();
        
        ConnectorCardsService connectorCardsService = mock(ConnectorCardsService.class);
        eRezeptWorkflowServiceUnit.connectorCardsService = connectorCardsService;

        MultiConnectorServicesProvider connectorServicesProvider = mock(MultiConnectorServicesProvider.class);
        eRezeptWorkflowServiceUnit.connectorServicesProvider = connectorServicesProvider;

        when(connectorServicesProvider.getContextType(any())).thenReturn(new ContextType());

        SignatureServicePortTypeV755 signatureServicePortTypeV755 = mock(SignatureServicePortTypeV755.class);
        when(connectorServicesProvider.getSignatureServicePortTypeV755(any())).thenReturn(signatureServicePortTypeV755);

        Event<GetSignatureModeResponseEvent> getSignatureModeResponseEvent = (Event<GetSignatureModeResponseEvent>) mock(Event.class);
        eRezeptWorkflowServiceUnit.getSignatureModeResponseEvent = getSignatureModeResponseEvent;

        eRezeptWorkflowServiceUnit.onActivateComfortSignatureEvent(new ActivateComfortSignatureEvent(null));

        ArgumentCaptor<GetSignatureModeResponseEvent> argumentCaptor = ArgumentCaptor.forClass(GetSignatureModeResponseEvent.class);
        verify(getSignatureModeResponseEvent).fireAsync(argumentCaptor.capture());

        GetSignatureModeResponseEvent thrownEvent = argumentCaptor.getValue();
        
        assertNotNull(thrownEvent.getUserId());
    }
}