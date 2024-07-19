package health.ere.ps.service.gematik;

import de.gematik.ws.conn.eventservice.v7.Event;
import de.gematik.ws.conn.vsds.vsdservice.v5.FaultMessage;
import de.gematik.ws.tel.error.v2.Error;
import health.ere.ps.config.AppConfig;
import health.ere.ps.jmx.ReadEPrescriptionsMXBeanImpl;
import health.ere.ps.model.config.UserConfigurations;
import health.ere.ps.service.cardlink.CardlinkWebsocketClient;
import health.ere.ps.service.cetp.CETPServerHandler;
import health.ere.ps.service.idp.BearerTokenService;
import io.netty.channel.embedded.EmbeddedChannel;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.ws.rs.client.Client;
import jakarta.ws.rs.client.Invocation;
import jakarta.ws.rs.client.WebTarget;
import jakarta.ws.rs.core.Response;
import jakarta.xml.bind.DatatypeConverter;
import jakarta.xml.ws.Holder;
import org.apache.commons.lang3.tuple.Pair;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.io.InputStream;
import java.math.BigInteger;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@QuarkusTest
class CardInsertedTest {

    private static final String READ_VSD_RESPONSE = "H4sIAAAAAAAA/w2M3QqCMBhAXyV8AL+5oj/mQNyKgk3ROaKbKLT8T1L8e/q8OReHwyG+XLlMPDQPwosnbcMykYmM1ViVdWsbadc1R4ChNT9J9eyywowTeD+hb+MKmnqAfukNSlRIMcJrtMN7tMW7zYHAoginmACnxL9TzZxJsGgtcmeWjGNPOZbIIyzzVGt2fo1zVvIrFEqq7BZZwVd7Z81+vSsmfzgVNoFlskDSP8uj5+izAAAA";

    @Test
    void vsdmSensorDataWithEventIdIsSentOnCardInsertedEvent() throws Exception {
        PharmacyService pharmacyService = spy(createPharmacyService());
        Holder<byte[]> holder = prepareHolder(pharmacyService);
        doReturn(holder).when(pharmacyService).readVSD(any(), any(), any(), any());

        CardlinkWebsocketClient cardlinkWebsocketClient = mock(CardlinkWebsocketClient.class);
        CETPServerHandler cetpServerHandler = new CETPServerHandler(pharmacyService, cardlinkWebsocketClient);
        EmbeddedChannel channel = new EmbeddedChannel(cetpServerHandler);

        String slotIdValue = "3";
        String ctIdValue = "CtIDValue";

        channel.writeOneInbound(prepareEvent(slotIdValue, ctIdValue));
        channel.pipeline().fireChannelReadComplete();

        ArgumentCaptor<String> messageTypeCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<Map<String, Object>> mapCaptor = ArgumentCaptor.forClass(Map.class);
        verify(cardlinkWebsocketClient, times(3)).sendJson(any(), any(), messageTypeCaptor.capture(), mapCaptor.capture());

        List<String> capturedMessages = messageTypeCaptor.getAllValues();

        assertTrue(capturedMessages.get(0).contains("eRezeptTokensFromAVS"));
        assertTrue(capturedMessages.get(1).contains("eRezeptBundlesFromAVS"));
        assertTrue(capturedMessages.get(2).contains("vsdmSensorData"));

        List<Map<String, Object>> maps = mapCaptor.getAllValues();
        Map<String, Object> vsdmSensorData = maps.get(2);

        assertEquals((Integer) vsdmSensorData.get("slotId"), Integer.parseInt(slotIdValue));
        assertEquals(vsdmSensorData.get("ctId"), ctIdValue);
        assertEquals(vsdmSensorData.get("eventId"), "2");
        assertNotNull(vsdmSensorData.get("endTime"));
    }

    @Test
    void vsdmSensorDataWithErrorIsSentOnCardInsertedEvent() throws Exception {
        PharmacyService pharmacyService = spy(createPharmacyService());
        prepareHolder(pharmacyService);
        Error faultInfo = new Error();
        Error.Trace trace = new Error.Trace();
        trace.setCode(BigInteger.TEN);
        faultInfo.getTrace().add(trace);
        doThrow(new FaultMessage("Fault", faultInfo)).when(pharmacyService).readVSD(any(), any(), any(), any());

        CardlinkWebsocketClient cardlinkWebsocketClient = mock(CardlinkWebsocketClient.class);
        CETPServerHandler cetpServerHandler = new CETPServerHandler(pharmacyService, cardlinkWebsocketClient);
        EmbeddedChannel channel = new EmbeddedChannel(cetpServerHandler);

        String slotIdValue = "3";
        String ctIdValue = "9";

        channel.writeOneInbound(prepareEvent(slotIdValue, ctIdValue));
        channel.pipeline().fireChannelReadComplete();

        ArgumentCaptor<String> messageTypeCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<Map<String, Object>> mapCaptor = ArgumentCaptor.forClass(Map.class);
        verify(cardlinkWebsocketClient, times(2)).sendJson(any(), any(), messageTypeCaptor.capture(), mapCaptor.capture());

        List<String> capturedMessages = messageTypeCaptor.getAllValues();
        assertTrue(capturedMessages.get(0).contains("vsdmSensorData"));
        assertTrue(capturedMessages.get(1).contains("eRezeptTokensFromAVS"));

        List<Map<String, Object>> maps = mapCaptor.getAllValues();
        Map<String, Object> vsdmSensorData = maps.get(0);

        assertEquals((Integer) vsdmSensorData.get("slotId"), Integer.parseInt(slotIdValue));
        assertEquals(vsdmSensorData.get("ctId"), ctIdValue);
        assertEquals(vsdmSensorData.get("err"), "10");
        assertNotNull(vsdmSensorData.get("endTime"));

        Map<String, Object> eRezeptTokensError = maps.get(1);

        assertEquals((Integer) eRezeptTokensError.get("slotId"), Integer.parseInt(slotIdValue));
        assertEquals(eRezeptTokensError.get("ctId"), ctIdValue);
        assertTrue(((String) eRezeptTokensError.get("tokens")).startsWith("ERROR: "));
    }

    private Holder<byte[]> prepareHolder(PharmacyService pharmacyService) {
        Holder<byte[]> holder = new Holder<>(DatatypeConverter.parseBase64Binary(READ_VSD_RESPONSE));

        pharmacyService.client = mock(Client.class);
        pharmacyService.appConfig = mock(AppConfig.class);

        Response response = mock(Response.class);
        WebTarget webTarget = mock(WebTarget.class);
        Invocation.Builder builder = mock(Invocation.Builder.class);

        when(pharmacyService.appConfig.getPrescriptionServiceURL()).thenReturn("");
        when(pharmacyService.client.target(anyString())).thenReturn(webTarget);
        when(webTarget.path(any())).thenReturn(webTarget);
        when(webTarget.queryParam(any(), anyString())).thenReturn(webTarget);
        when(webTarget.request()).thenReturn(builder);
        when(builder.header(any(), any())).thenReturn(builder);
        when(builder.get()).thenReturn(response);
        when(response.getStatus()).thenReturn(200);

        InputStream is = CardInsertedTest.class.getResourceAsStream("/gematik/Bundle-4fe2013d-ae94-441a-a1b1-78236ae65680.xml");
        when(response.readEntity(eq(InputStream.class))).thenReturn(is);

        return holder;
    }

    private Pair<Event, UserConfigurations> prepareEvent(String slotIdValue, String ctIdValue) {
        Event event = new Event();
        event.setTopic("CARD/INSERTED");
        Event.Message message = new Event.Message();
        Event.Message.Parameter parameter = new Event.Message.Parameter();
        parameter.setKey("CardHandle");
        parameter.setValue("CardHandleValue");
        Event.Message.Parameter parameterSlotId = new Event.Message.Parameter();
        parameterSlotId.setKey("SlotID");
        parameterSlotId.setValue(slotIdValue);
        Event.Message.Parameter parameterCtId = new Event.Message.Parameter();
        parameterCtId.setKey("CtID");
        parameterCtId.setValue(ctIdValue);
        Event.Message.Parameter parameterCardType = new Event.Message.Parameter();
        parameterCardType.setKey("CardType");
        parameterCardType.setValue("EGK");

        message.getParameter().add(parameter);
        message.getParameter().add(parameterSlotId);
        message.getParameter().add(parameterCtId);
        message.getParameter().add(parameterCardType);
        event.setMessage(message);
        return Pair.of(event, new UserConfigurations());
    }

    private static PharmacyService createPharmacyService() {
        var pharmacyService = new PharmacyService();
        pharmacyService.setReadEPrescriptionsMXBean(new ReadEPrescriptionsMXBeanImpl());    //normally done by CDI
        BearerTokenService tokenService = mock(BearerTokenService.class);
        pharmacyService.bearerTokenService = tokenService;
        when(tokenService.getBearerToken(any())).thenReturn("this_is_a_test_jwt_token");
        return pharmacyService;
    }
}
