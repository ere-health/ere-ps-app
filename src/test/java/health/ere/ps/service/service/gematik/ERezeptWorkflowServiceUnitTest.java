package health.ere.ps.service.gematik;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import javax.enterprise.event.Event;

import org.hl7.fhir.r4.model.Bundle;
import org.hl7.fhir.r4.model.Identifier;
import org.hl7.fhir.r4.model.Identifier.IdentifierUse;
import org.hl7.fhir.r4.model.Task;
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

    @Test
    void testGetAccessCode() {
        Task task = new Task();
        Identifier identifier = new Identifier();
        identifier.setSystem(ERezeptWorkflowService.EREZEPT_ACCESS_CODE_SYSTEM);
        identifier.setValue("ACCESS_CODE");
        task.addIdentifier(identifier);
        assertEquals("ACCESS_CODE", ERezeptWorkflowService.getAccessCode(task));
    }

    @Test
    void testGetAccessCode_GEM() {
        Task task = new Task();
        Identifier identifier = new Identifier();
        identifier.setSystem(ERezeptWorkflowService.EREZEPT_ACCESS_CODE_SYSTEM_GEM);
        identifier.setValue("ACCESS_CODE");
        task.addIdentifier(identifier);
        assertEquals("ACCESS_CODE", ERezeptWorkflowService.getAccessCode(task));
    }

    @Test
    void testGetPrescriptionId() {
        Task task = new Task();
        Identifier identifier = new Identifier();
        identifier.setSystem(ERezeptWorkflowService.EREZEPT_IDENTIFIER_SYSTEM);
        identifier.setValue("PrescriptionId");
        task.addIdentifier(identifier);
        assertEquals("PrescriptionId", ERezeptWorkflowService.getPrescriptionId(task));
    }

    @Test
    void testGetPrescriptionId_GEM() {
        Task task = new Task();
        Identifier identifier = new Identifier();
        identifier.setSystem(ERezeptWorkflowService.EREZEPT_IDENTIFIER_SYSTEM_GEM);
        identifier.setValue("PrescriptionId");
        task.addIdentifier(identifier);
        assertEquals("PrescriptionId", ERezeptWorkflowService.getPrescriptionId(task));
    }

    @Test
    void testUpdateBundleWithTask() {
        Task task = new Task();
        Identifier identifier = new Identifier();
        identifier.setSystem(ERezeptWorkflowService.EREZEPT_IDENTIFIER_SYSTEM);
        identifier.setValue("PrescriptionId");
        task.addIdentifier(identifier);

        Bundle bundle = new Bundle();
        ERezeptWorkflowService.updateBundleWithTask(task, bundle);
        assertEquals(bundle.getIdentifier().getValue(), "PrescriptionId");
        assertEquals(bundle.getIdentifier().getSystem(), ERezeptWorkflowService.EREZEPT_IDENTIFIER_SYSTEM);
    }

    @Test
    void testUpdateBundleWithTask_GEM() {
        Task task = new Task();
        Identifier identifier = new Identifier();
        identifier.setUse(IdentifierUse.OFFICIAL);
        identifier.setSystem(ERezeptWorkflowService.EREZEPT_IDENTIFIER_SYSTEM_GEM);
        identifier.setValue("PrescriptionId");
        task.addIdentifier(identifier);

        Bundle bundle = new Bundle();
        ERezeptWorkflowService.updateBundleWithTask(task, bundle);
        assertEquals(bundle.getIdentifier().getValue(), "PrescriptionId");
        assertNull(bundle.getIdentifier().getUse());
        assertEquals(bundle.getIdentifier().getSystem(), ERezeptWorkflowService.EREZEPT_IDENTIFIER_SYSTEM_GEM);
    }

    @Test
    void testUpdateBundleWithTask_With_Profile() {
        Task task = new Task();
        Identifier identifier = new Identifier();
        identifier.setUse(IdentifierUse.OFFICIAL);
        identifier.setSystem(ERezeptWorkflowService.EREZEPT_IDENTIFIER_SYSTEM_GEM);
        identifier.setValue("PrescriptionId");
        task.addIdentifier(identifier);

        Bundle bundle = new Bundle();
        bundle.getMeta().addProfile("https://fhir.kbv.de/StructureDefinition/KBV_PR_ERP_Bundle|1.0.2");

        ERezeptWorkflowService.updateBundleWithTask(task, bundle);
        assertEquals(bundle.getIdentifier().getValue(), "PrescriptionId");
        assertNull(bundle.getIdentifier().getUse());
        assertEquals(bundle.getIdentifier().getSystem(), ERezeptWorkflowService.EREZEPT_IDENTIFIER_SYSTEM);
    }
}