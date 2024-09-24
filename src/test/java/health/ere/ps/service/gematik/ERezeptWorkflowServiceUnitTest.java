package health.ere.ps.service.gematik;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Consumer;

import org.hl7.fhir.r4.model.Bundle;
import org.hl7.fhir.r4.model.Identifier;
import org.hl7.fhir.r4.model.Identifier.IdentifierUse;
import org.hl7.fhir.r4.model.Task;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import de.gematik.ws.conn.connectorcontext.v2.ContextType;
import de.gematik.ws.conn.signatureservice.v7.SignResponse;
import de.gematik.ws.conn.signatureservice.wsdl.v7.FaultMessage;
import de.gematik.ws.conn.signatureservice.wsdl.v7.SignatureServicePortTypeV740;
import de.gematik.ws.conn.signatureservice.wsdl.v7.SignatureServicePortTypeV755;
import health.ere.ps.config.AppConfig;
import health.ere.ps.config.UserConfig;
import health.ere.ps.event.ActivateComfortSignatureEvent;
import health.ere.ps.event.BundlesWithAccessCodeEvent;
import health.ere.ps.event.GetSignatureModeEvent;
import health.ere.ps.event.GetSignatureModeResponseEvent;
import health.ere.ps.event.SignAndUploadBundlesEvent;
import health.ere.ps.exception.gematik.ERezeptWorkflowException;
import health.ere.ps.model.gematik.BundleWithAccessCodeOrThrowable;
import health.ere.ps.service.connector.cards.ConnectorCardsService;
import health.ere.ps.service.connector.provider.MultiConnectorServicesProvider;
import health.ere.ps.service.idp.BearerTokenService;
import jakarta.enterprise.event.Event;
import jakarta.json.Json;
import jakarta.json.JsonObject;
import jakarta.ws.rs.client.Client;
import jakarta.ws.rs.client.Invocation;
import jakarta.ws.rs.client.Invocation.Builder;
import jakarta.ws.rs.client.WebTarget;
import jakarta.ws.rs.core.Response;
import oasis.names.tc.dss._1_0.core.schema.Base64Signature;
import oasis.names.tc.dss._1_0.core.schema.SignatureObject;

public class ERezeptWorkflowServiceUnitTest {

    @Test
    void testActivateComfortSignatureUnit() throws ERezeptWorkflowException {
        ERezeptWorkflowService eRezeptWorkflowServiceUnit = mockERezeptWorkflowServiceUnit();

        Event<GetSignatureModeResponseEvent> getSignatureModeResponseEvent = (Event<GetSignatureModeResponseEvent>) mock(Event.class);
        eRezeptWorkflowServiceUnit.getSignatureModeResponseEvent = getSignatureModeResponseEvent;

        eRezeptWorkflowServiceUnit.onActivateComfortSignatureEvent(new ActivateComfortSignatureEvent(null));

        ArgumentCaptor<GetSignatureModeResponseEvent> argumentCaptor = ArgumentCaptor.forClass(GetSignatureModeResponseEvent.class);
        verify(getSignatureModeResponseEvent).fireAsync(argumentCaptor.capture());

        GetSignatureModeResponseEvent thrownEvent = argumentCaptor.getValue();
        
        assertNotNull(thrownEvent.getUserId());
        assertTrue(thrownEvent.getAnswertToActivateComfortSignature());
    }

    private ERezeptWorkflowService mockERezeptWorkflowServiceUnit() {
        return mockERezeptWorkflowServiceUnit(null);
    }
    private ERezeptWorkflowService mockERezeptWorkflowServiceUnit(Consumer<SignatureServicePortTypeV740> signatureServicePortTypeV740Consumer) {
        ERezeptWorkflowService eRezeptWorkflowServiceUnit = new ERezeptWorkflowService();
        
        ConnectorCardsService connectorCardsService = mock(ConnectorCardsService.class);
        eRezeptWorkflowServiceUnit.connectorCardsService = connectorCardsService;

        MultiConnectorServicesProvider connectorServicesProvider = mock(MultiConnectorServicesProvider.class);
        eRezeptWorkflowServiceUnit.connectorServicesProvider = connectorServicesProvider;

        when(connectorServicesProvider.getContextType(any())).thenReturn(new ContextType());

        SignatureServicePortTypeV755 signatureServicePortTypeV755 = mock(SignatureServicePortTypeV755.class);
        when(connectorServicesProvider.getSignatureServicePortTypeV755(any())).thenReturn(signatureServicePortTypeV755);

        SignatureServicePortTypeV740 signatureServicePortTypeV740 = mock(SignatureServicePortTypeV740.class);
        when(connectorServicesProvider.getSignatureServicePortType(any())).thenReturn(signatureServicePortTypeV740);
        if(signatureServicePortTypeV740Consumer != null) {
            signatureServicePortTypeV740Consumer.accept(signatureServicePortTypeV740);
        }
        return eRezeptWorkflowServiceUnit;
    }

    @Test
    void testOnGetSignatureMode() {

        ERezeptWorkflowService eRezeptWorkflowServiceUnit = mockERezeptWorkflowServiceUnit();
        Event<GetSignatureModeResponseEvent> getSignatureModeResponseEvent = (Event<GetSignatureModeResponseEvent>) mock(Event.class);
        eRezeptWorkflowServiceUnit.getSignatureModeResponseEvent = getSignatureModeResponseEvent;
        ArgumentCaptor<GetSignatureModeResponseEvent> argumentCaptor = ArgumentCaptor.forClass(GetSignatureModeResponseEvent.class);
        
        JsonObject jsonObject = Json.createObjectBuilder().add("runtimeConfig",
        Json.createObjectBuilder().add("connector.user-id", "37c312a6-eb7f-11ee-8eea-6ba768ebd268")
        ).build();
        
        eRezeptWorkflowServiceUnit.onGetSignatureModeEvent(new GetSignatureModeEvent(jsonObject));
        verify(getSignatureModeResponseEvent).fireAsync(argumentCaptor.capture());
        GetSignatureModeResponseEvent thrownEvent = argumentCaptor.getValue();
        
        assertNotNull(thrownEvent.getUserId());
        assertFalse(thrownEvent.getAnswertToActivateComfortSignature());

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

    @Test
    public void testOnSignAndUploadBundlesEvent() {
        ERezeptWorkflowService eRezeptWorkflowServiceUnit = mockERezeptWorkflowServiceUnit(getSignatureConsumer());
        mockClient(eRezeptWorkflowServiceUnit);
        
        eRezeptWorkflowServiceUnit.appConfig = mock(AppConfig.class);
        when(eRezeptWorkflowServiceUnit.appConfig.getPrescriptionServiceURL()).thenReturn("http://localhost:8080");
        
        eRezeptWorkflowServiceUnit.userConfig = new UserConfig();
        Event<BundlesWithAccessCodeEvent> bundlesWithAccessCodeEvent = (Event<BundlesWithAccessCodeEvent>) mock(Event.class);
        eRezeptWorkflowServiceUnit.bundlesWithAccessCodeEvent = bundlesWithAccessCodeEvent;
        eRezeptWorkflowServiceUnit.bearerTokenService = mock(BearerTokenService.class);
        ArgumentCaptor<BundlesWithAccessCodeEvent> argumentCaptor = ArgumentCaptor.forClass(BundlesWithAccessCodeEvent.class);
        
        Bundle bundle = new Bundle();
        Bundle bundle2 = new Bundle();

        List<Bundle> bundles = Arrays.asList(bundle, bundle2);
        
        eRezeptWorkflowServiceUnit.onSignAndUploadBundlesEvent(new SignAndUploadBundlesEvent(bundles));
        verify(bundlesWithAccessCodeEvent).fireAsync(argumentCaptor.capture());
        BundlesWithAccessCodeEvent thrownEvent = argumentCaptor.getValue();
        
        assertEquals(2, thrownEvent.getBundleWithAccessCodeOrThrowable().get(0).size());
    }

    private void mockClient(ERezeptWorkflowService eRezeptWorkflowServiceUnit) {
        eRezeptWorkflowServiceUnit.client = mock(Client.class);
        WebTarget target = mock(WebTarget.class);
        when(target.path(anyString())).thenReturn(target);
        Builder invocation = mock(Invocation.Builder.class);
        when(invocation.header(anyString(), any())).thenReturn(invocation);
        Response response = mock(Response.class);
        when(response.readEntity(eq(String.class))).thenReturn("<Task></Task>");  
        when(response.readEntity(eq(InputStream.class))).thenReturn(new ByteArrayInputStream("<OperationOutcome></OperationOutcome>".getBytes()));
        when(response.getStatus()).thenReturn(Response.Status.OK.getStatusCode());
        when(invocation.post(any())).thenReturn(response);

        when(target.request()).thenReturn(invocation);
        when(eRezeptWorkflowServiceUnit.client.target(anyString())).thenReturn(target);
    }

    private Consumer<SignatureServicePortTypeV740> getSignatureConsumer() {
        return getSignatureConsumer(1);
    }
    private Consumer<SignatureServicePortTypeV740> getSignatureConsumer(int amount) {
        return (signatureServicePortTypeV740) -> {
            try {
                List<SignResponse> signResponses = new ArrayList<>();
                for(int i = 0; i < amount; i++) {
                    SignResponse signResponse = new SignResponse();
                    String requestID = String.valueOf(i+1);
                    signResponse.setRequestID(requestID);
                    SignatureObject signatureObject = new SignatureObject();
                    Base64Signature base64Data = new Base64Signature();
                    base64Data.setValue(new byte[] {(byte)i});
                    signatureObject.setBase64Signature(base64Data);
                    signResponse.setSignatureObject(signatureObject);
                    signResponses.add(signResponse);
                }
                when(signatureServicePortTypeV740.signDocument(any(), any(), any(), any(), any())).thenReturn(signResponses);
            } catch (FaultMessage e) {
                e.printStackTrace();
            }
        };
    }

    @Test
    public void testUploadSignedBundle() {
        ERezeptWorkflowService eRezeptWorkflowServiceUnit = mockERezeptWorkflowServiceUnit(getSignatureConsumer(2));
        mockClient(eRezeptWorkflowServiceUnit);
        
        eRezeptWorkflowServiceUnit.appConfig = mock(AppConfig.class);
        when(eRezeptWorkflowServiceUnit.appConfig.getPrescriptionServiceURL()).thenReturn("http://localhost:8080");
        when(eRezeptWorkflowServiceUnit.appConfig.enableBatchSign()).thenReturn(true);
  
        eRezeptWorkflowServiceUnit.userConfig = new UserConfig();

        Bundle bundle = new Bundle();
        bundle.setId("1");
        Bundle bundle2 = new Bundle();
        bundle2.setId("2");

        List<Bundle> bundles = Arrays.asList(bundle, bundle2);

        Task task = new Task();
        Task task2 = new Task();

        List<Task> tasks = Arrays.asList(task, task2);

        List<BundleWithAccessCodeOrThrowable> bundleWithAccessCodeOrThrowable = Arrays.asList(new BundleWithAccessCodeOrThrowable(bundle, "ACCESS_CODE"), new BundleWithAccessCodeOrThrowable(bundle2, "ACCESS_CODE"));

        eRezeptWorkflowServiceUnit.uploadSignedBundle(bundles, null, null, null, bundleWithAccessCodeOrThrowable, tasks);

        assertEquals(0x00, bundleWithAccessCodeOrThrowable.get(0).getSignedBundle()[0]);
        assertEquals(0x01, bundleWithAccessCodeOrThrowable.get(1).getSignedBundle()[0]);
    }
}