package health.ere.ps.service.gematik;

import de.gematik.ws.conn.eventservice.v7.Event;
import health.ere.ps.config.AppConfig;
import health.ere.ps.service.cardlink.CardlinkWebsocketClient;
import health.ere.ps.service.cetp.CETPServerHandler;
import io.netty.channel.embedded.EmbeddedChannel;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import javax.json.Json;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.Invocation;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.Response;
import javax.xml.bind.DatatypeConverter;
import javax.xml.ws.Holder;
import java.io.InputStream;
import java.io.StringReader;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class CardInsertedTest {

    private static final String READ_VSD_RESPONSE = "H4sIAAAAAAAA/w2M3QqCMBhAXyV8AL+5oj/mQNyKgk3ROaKbKLT8T1L8e/q8OReHwyG+XLlMPDQPwosnbcMykYmM1ViVdWsbadc1R4ChNT9J9eyywowTeD+hb+MKmnqAfukNSlRIMcJrtMN7tMW7zYHAoginmACnxL9TzZxJsGgtcmeWjGNPOZbIIyzzVGt2fo1zVvIrFEqq7BZZwVd7Z81+vSsmfzgVNoFlskDSP8uj5+izAAAA";

    @Test
    public void vsdmSensorDataWitEventIdIsSentOnCardInsertedEvent() throws Exception {
        Holder<byte[]> holder = new Holder<>(DatatypeConverter.parseBase64Binary(READ_VSD_RESPONSE));
        PharmacyService pharmacyService = spy(new PharmacyService());
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

        doReturn(holder).when(pharmacyService).readVSD(any(), any(), any());

        CardlinkWebsocketClient cardlinkWebsocketClient = mock(CardlinkWebsocketClient.class);
        CETPServerHandler cetpServerHandler = new CETPServerHandler(pharmacyService, cardlinkWebsocketClient);
        EmbeddedChannel channel = new EmbeddedChannel(cetpServerHandler);

        String slotIdValue = "3";
        String ctIdValue = "CtIDValue";

        channel.writeOneInbound(prepareEvent(slotIdValue, ctIdValue));
        channel.pipeline().fireChannelReadComplete();

        ArgumentCaptor<String> messageCaptor = ArgumentCaptor.forClass(String.class);
        verify(cardlinkWebsocketClient, times(3)).sendMessage(messageCaptor.capture());

        List<String> capturedMessages = messageCaptor.getAllValues();

        assertTrue(capturedMessages.get(0).contains("eRezeptTokensFromAVS"));
        assertTrue(capturedMessages.get(1).contains("eRezeptBundlesFromAVS"));
        assertTrue(capturedMessages.get(2).contains("vsdmSensorData"));

        String vsdmSensorData = capturedMessages.get(2);

        String payloadBase64 = Json.createReader(new StringReader(vsdmSensorData)).readArray().get(0).asJsonObject().get("payload").toString();
        String payload = new String(DatatypeConverter.parseBase64Binary(payloadBase64));

        assertTrue(payload.contains(slotIdValue));
        assertTrue(payload.contains(ctIdValue));
        assertTrue(payload.contains("endTime"));
        assertTrue(payload.contains("eventId"));
        assertTrue(payload.contains("2"));
    }

    private Event prepareEvent(String slotIdValue, String ctIdValue) {
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
        
        message.getParameter().add(parameter);
        message.getParameter().add(parameterSlotId);
        message.getParameter().add(parameterCtId);
        event.setMessage(message);
        return event;
    }
}
