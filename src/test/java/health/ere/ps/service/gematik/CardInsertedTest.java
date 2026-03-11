package health.ere.ps.service.gematik;

import de.gematik.ws.conn.eventservice.v7.Event;
import de.gematik.ws.conn.eventservice.wsdl.v7.FaultMessage;
import de.gematik.ws.conn.vsds.vsdservice.v5.ReadVSDResponse;
import de.gematik.ws.tel.error.v2.Error;
import de.health.service.cetp.cardlink.CardlinkWebsocketClient;
import de.health.service.cetp.domain.eventservice.event.DecodeResult;
import health.ere.ps.config.AppConfig;
import health.ere.ps.jmx.ReadEPrescriptionsMXBeanImpl;
import health.ere.ps.model.config.UserConfigurations;
import health.ere.ps.profile.RUTestProfile;
import health.ere.ps.service.cetp.CETPServerHandler;
import health.ere.ps.service.cetp.mapper.event.EventMapper;
import health.ere.ps.service.cetp.tracker.TrackerService;
import health.ere.ps.service.idp.BearerTokenService;
import io.netty.channel.embedded.EmbeddedChannel;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.junit.TestProfile;
import jakarta.inject.Inject;
import jakarta.ws.rs.client.Client;
import jakarta.ws.rs.client.Invocation;
import jakarta.ws.rs.client.WebTarget;
import jakarta.ws.rs.core.Response;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.io.InputStream;
import java.math.BigInteger;
import java.util.List;
import java.util.Map;

import static health.ere.ps.service.gematik.ReadVSDHelper.fromBase64String;
import static health.ere.ps.service.gematik.ReadVSDHelper.fromBytes;
import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@QuarkusTest
@TestProfile(RUTestProfile.class)
class CardInsertedTest {

    private static final String READ_VSD_RESPONSE = "PD94bWwgdmVyc2lvbj0iMS4wIiBlbmNvZGluZz0iVVRGLTgiIHN0YW5kYWxvbmU9InllcyI/PjxSZWFkVlNEUmVzcG9uc2UgeG1sbnM9Imh0dHA6Ly93cy5nZW1hdGlrLmRlL2Nvbm4vdnNkcy9WU0RTZXJ2aWNlL3Y1LjIiPjxQZXJzb2VubGljaGVWZXJzaWNoZXJ0ZW5kYXRlbj5INHNJQUFBQUFBQUEvNDFTd1hMYU1CRDlGWS91ZURIVUtlbXNsV2toa3pBVG9GTlM0TVlvOWpiU1JKWTdra3pTZm5zUFhZZEFReWVIWENTOTU3ZjczdXdhTDU1cW0reklCOU80UW1ScFh5VGt5cVl5N3I0UTArV2lOeHJsNTcwc0YwbUl5bFhLTm80SzhZdUN1SkQ0ZmJ6OXlxVU5PV3RLVGF1dURkOCtFaXY1Mk14dWt2Rmt0bDFkZmx0T0YvTkM1T21nYzJCUEZ3cWhZL3o1Q2VBeHBQZFVxMmdlMG9yZ2g0SmRxT3J1Z0IzcmhjUlhmZjBKY3R2cFJHNnlySitQUG43SUJ3ai9mOFBuZUU3aUZkMjFQZ1pPMWRZeU84K3ovaUFiSXB6UXVHcThVelhKc2ZZbVJLTWNOM3loY0s1Sy9mejZRbTBrRys1VVo5TzcvY05PbWt5TmNGU3dXU2kxcFZKSE9lczhqZ2h2RGRmS2lVOFI5azljUnE5Q0lQZTU4c1EzSjI1Q3RHVGliNld0SE9TajRSbkNDWWNMSCtXVjh0MktFRHFBTjd3WmlldEd1OEFpcTNqODVIbUpKQ2NJYjlFSSs1SVhkN2tteTdOTkdIS3lBNG5YcWcydXJXdWVlb2J3Q2gwMS8yTERZZEp3dWkxNHh6OGkvd0w5V3dLRWhnSUFBQT09PC9QZXJzb2VubGljaGVWZXJzaWNoZXJ0ZW5kYXRlbj48QWxsZ2VtZWluZVZlcnNpY2hlcnVuZ3NkYXRlbj5INHNJQUFBQUFBQUEvODFTMzAvQ01CRCtWNWE5c3lzTE04eDBKUWpHRUFTTTZEUytMSFU3dG9YdFp0YUN4ci9lamhDekVZS3Z2clM1Nys3N2tWNzU2S3NzckQzV0txOG9zUHNPc3kya3VFcHlTZ043dGw3MWhrUFA3L1U5ZXlUNDh5UWFGMFdLSmVhRVljT0pNNngzbEtwRWFxVFh4YjFsMUVnRmRxYjF4elhBcDNMTXROVDUxa2tRTmhMMktpbWJBL2FlNDlyV1pMcUl3dHZIOVd5MURHeURHSGZCZjRVMTFxMnFzVkZ4dHRQZmd0OWdtaE1KbDdrRE5tQXVoeVBBNTVVeVFYUXRNVzNJblhLTFJFWkU5Sm52TWVaZitSek85cnVzUWlJbFdKc1hRVEU5WWJSN2ZDbExGRStvdEhVM0QzdnJrTU1CNGVQM0d1T01EcFAvTUI1Y3lBZW45ZGxsdk8yVTFOODViU3JWS1l4UFo1a2theTI4bHNnUmFuT2lZeHFqSHgzNEw2c0h3Zm9jbXB2RHBVazR0WVpPTU9oK0svajdMNHNmK2FkSElod0RBQUE9PC9BbGxnZW1laW5lVmVyc2ljaGVydW5nc2RhdGVuPjxHZXNjaHVldHp0ZVZlcnNpY2hlcnRlbmRhdGVuPkg0c0lBQUFBQUFBQS80VlAyMnJDUUJEOWxiRHZabUlob0RKWktTb1M4QUlORmZGRmxtVE1CcE9OWkNkcHlkZDNRd3NxTGZUbHpNeVp5NW1EODgrcTlEcHFiRkdiU0l6OVFIaGswam9yVEI2Sk9ObVBKcE53T2hxSFlpN3hmWEZlazAxMVM5d3pIWWFsVkZQRFpETGw0TGpkZU82YXNaSFF6TGNad0lmMWM2b1VGMWMvSTdnbzZHeFdEUUJkNkw4SWI3SGNuZytydHlUZTd5TGhHS2N1OGRUMlNwZXR5YTFseGEyVm1IekhBT0VuUS9oamlFcTZjdEU1TDl3b3lrbmlLelU5bCs1SE9VVzRGM2hTMnFqSDNqUGhaSDdmZ3YvTnl5K2x2RS9nVFFFQUFBPT08L0dlc2NodWV0enRlVmVyc2ljaGVydGVuZGF0ZW4+PFZTRF9TdGF0dXM+PFN0YXR1cz4wPC9TdGF0dXM+PFRpbWVzdGFtcD4yMDI0LTA0LTAyVDEyOjExOjU5LjAwMFo8L1RpbWVzdGFtcD48VmVyc2lvbj41LjIuMDwvVmVyc2lvbj48L1ZTRF9TdGF0dXM+PFBydWVmdW5nc25hY2h3ZWlzPkg0c0lBQUFBQUFBQS93Mk16UXFDUUJoRlgwWGNCdk01bzFMRU9JdDBvb0taTkg4S04yRnFtYWtJaW9wUDN5enVYWng3dU5TWG11dUpaOEp2NGZrcUhSMGpBeG02dHJSTk56aDZOWTc5SG1BZTBLZHNzL0g3UTBVSjd3eW1vV2loNzJhWWxLOHpHb1dNR01RMk1MR3dqVzNib3FBUTVZeFE0SXo2S2J0N2ZCRmViTWsxd0tMbXBsaHpGVzdLU0NacXUreU9kUkkvck5ROHVOdlhJc2ROZFNLQnlOTEd6YWZoTmpzVTFJa3F5ZjRoOSs0RnN3QUFBQT09PC9QcnVlZnVuZ3NuYWNod2Vpcz48L1JlYWRWU0RSZXNwb25zZT4=";

    @Inject
    EventMapper eventMapper;

    @Test
    void vsdmSensorDataWithEventIdIsSentOnCardInsertedEvent() throws Exception {
        PharmacyService pharmacyService = spy(createPharmacyService());
        initMocks(pharmacyService);
        ReadVSDResponse readVSDResponse = fromBase64String(READ_VSD_RESPONSE);
        doReturn(readVSDResponse).when(pharmacyService).readVSD(any(), any(), any(), any());

        TrackerService trackerService = mock(TrackerService.class);
        when(trackerService.submit(any(), any(), any(), any())).thenReturn(true);

        CardlinkWebsocketClient cardlinkWebsocketClient = mock(CardlinkWebsocketClient.class);
        AppConfig appConfig = mock(AppConfig.class);
        when(appConfig.isVsdmResponseForCardlinkEnabled()).thenReturn(true);
        CETPServerHandler cetpServerHandler = new CETPServerHandler(
            appConfig, trackerService, pharmacyService, cardlinkWebsocketClient
        );
        EmbeddedChannel channel = new EmbeddedChannel(cetpServerHandler);

        String slotIdValue = "3";
        String ctIdValue = "CtIDValue";

        channel.writeOneInbound(decode(slotIdValue, ctIdValue));
        channel.pipeline().fireChannelReadComplete();

        ArgumentCaptor<String> messageTypeCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<Map<String, Object>> mapCaptor = ArgumentCaptor.forClass(Map.class);
        verify(cardlinkWebsocketClient, times(4)).sendJson(any(), any(), messageTypeCaptor.capture(), mapCaptor.capture());

        List<String> capturedMessages = messageTypeCaptor.getAllValues();

        assertTrue(capturedMessages.get(0).contains("eRezeptTokensFromAVS"));
        assertTrue(capturedMessages.get(1).contains("eRezeptBundlesFromAVS"));
        assertTrue(capturedMessages.get(2).contains("vsdmSensorData"));
        assertTrue(capturedMessages.get(3).contains("ReadVSDFromAVS"));

        verify(trackerService).submit(any(), any(), any(), any());

        List<Map<String, Object>> maps = mapCaptor.getAllValues();
        Map<String, Object> vsdmSensorData = maps.get(2);

        assertEquals((Integer) vsdmSensorData.get("slotId"), Integer.parseInt(slotIdValue));
        assertEquals(vsdmSensorData.get("ctId"), ctIdValue);
        assertEquals(vsdmSensorData.get("eventId"), "2");
        assertNotNull(vsdmSensorData.get("endTime"));

        Map<String, Object> readVSDResponseData = maps.get(3);
        String readVSDResponseString = (String) readVSDResponseData.get("ReadVSDResponse");
        ReadVSDResponse sentReadVSDResponse = fromBytes(readVSDResponseString.getBytes());
        assertArrayEquals(readVSDResponse.getAllgemeineVersicherungsdaten(), sentReadVSDResponse.getAllgemeineVersicherungsdaten());
        assertArrayEquals(readVSDResponse.getPruefungsnachweis(), sentReadVSDResponse.getPruefungsnachweis());
        assertArrayEquals(readVSDResponse.getPersoenlicheVersichertendaten(), sentReadVSDResponse.getPersoenlicheVersichertendaten());
        assertArrayEquals(readVSDResponse.getGeschuetzteVersichertendaten(), sentReadVSDResponse.getGeschuetzteVersichertendaten());
    }

    @Test
    void vsdmSensorDataWithErrorIsSentOnCardInsertedEvent() throws Exception {
        PharmacyService pharmacyService = spy(createPharmacyService());
        initMocks(pharmacyService);
        Error faultInfo = new Error();
        Error.Trace trace = new Error.Trace();
        trace.setCode(BigInteger.TEN);
        faultInfo.getTrace().add(trace);
        doThrow(new FaultMessage("Fault", faultInfo)).when(pharmacyService).readVSD(any(), any(), any(), any());

        TrackerService trackerService = mock(TrackerService.class);
        when(trackerService.submit(any(), any(), any(), any())).thenReturn(true);

        CardlinkWebsocketClient cardlinkWebsocketClient = mock(CardlinkWebsocketClient.class);
        AppConfig appConfig = mock(AppConfig.class);
        when(appConfig.isVsdmResponseForCardlinkEnabled()).thenReturn(true);
        CETPServerHandler cetpServerHandler = new CETPServerHandler(
            appConfig, trackerService, pharmacyService, cardlinkWebsocketClient
        );
        EmbeddedChannel channel = new EmbeddedChannel(cetpServerHandler);

        String slotIdValue = "3";
        String ctIdValue = "9";

        channel.writeOneInbound(decode(slotIdValue, ctIdValue));
        channel.pipeline().fireChannelReadComplete();

        ArgumentCaptor<String> messageTypeCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<Map<String, Object>> mapCaptor = ArgumentCaptor.forClass(Map.class);
        verify(cardlinkWebsocketClient, times(2)).sendJson(any(), any(), messageTypeCaptor.capture(), mapCaptor.capture());

        List<String> capturedMessages = messageTypeCaptor.getAllValues();
        assertTrue(capturedMessages.get(0).contains("vsdmSensorData"));
        assertTrue(capturedMessages.get(1).contains("receiveTasklistError"));

        verify(trackerService, never()).submit(any(), any(), any(), any());

        List<Map<String, Object>> maps = mapCaptor.getAllValues();
        Map<String, Object> vsdmSensorData = maps.get(0);

        assertEquals((Integer) vsdmSensorData.get("slotId"), Integer.parseInt(slotIdValue));
        assertEquals(vsdmSensorData.get("ctId"), ctIdValue);
        assertEquals(vsdmSensorData.get("err"), "10");
        assertNotNull(vsdmSensorData.get("endTime"));

        Map<String, Object> receiveTasklistError = maps.get(1);

        assertEquals((Integer) receiveTasklistError.get("status"), 500);
        assertNotNull(receiveTasklistError.get("errormessage"));
    }

    private void initMocks(PharmacyService pharmacyService) {
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
    }

    private DecodeResult decode(String slotIdValue, String ctIdValue) {
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
        return new DecodeResult(eventMapper.toDomain(event), new UserConfigurations());
    }

    private static PharmacyService createPharmacyService() {
        var pharmacyService = new PharmacyService();
        pharmacyService.setReadEPrescriptionsMXBean(new ReadEPrescriptionsMXBeanImpl());    // normally done by CDI
        BearerTokenService tokenService = mock(BearerTokenService.class);
        pharmacyService.bearerTokenService = tokenService;
        when(tokenService.getBearerToken(any())).thenReturn("this_is_a_test_jwt_token");
        return pharmacyService;
    }
}
