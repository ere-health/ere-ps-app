package health.ere.ps.service.cetp;

import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.commons.lang3.tuple.Pair;
import org.hl7.fhir.r4.model.Bundle;
import org.hl7.fhir.r4.model.Bundle.BundleEntryComponent;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.parser.IParser;
import de.gematik.ws.conn.eventservice.v7.Event;
import de.gematik.ws.conn.vsds.vsdservice.v5.FaultMessage;
import health.ere.ps.config.RuntimeConfig;
import health.ere.ps.model.config.UserConfigurations;
import health.ere.ps.service.cardlink.CardlinkWebsocketClient;
import health.ere.ps.service.gematik.PharmacyService;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import jakarta.json.Json;
import jakarta.json.JsonArray;
import jakarta.json.JsonArrayBuilder;
import jakarta.json.JsonObject;
import jakarta.json.JsonObjectBuilder;
import jakarta.xml.bind.DatatypeConverter;

public class CETPServerHandler extends ChannelInboundHandlerAdapter {

    private static final Logger log = Logger.getLogger(CETPServerHandler.class.getName());

    PharmacyService pharmacyService;

    CardlinkWebsocketClient cardlinkWebsocketClient;

    IParser parser = FhirContext.forR4().newXmlParser();

    public CETPServerHandler(PharmacyService pharmacyService, CardlinkWebsocketClient cardlinkWebsocketClient) {
        this.pharmacyService = pharmacyService;
        this.cardlinkWebsocketClient = cardlinkWebsocketClient;
    }

    @Override
    public void handlerAdded(ChannelHandlerContext ctx) {
    }

    @Override
    public void handlerRemoved(ChannelHandlerContext ctx) {
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) {
        @SuppressWarnings("unchecked")
        Pair<Event, UserConfigurations> input = (Pair<Event, UserConfigurations>) msg;
        Event event = (Event) input.getKey();

        if (event.getTopic().equals("CARD/INSERTED")) {
            log.info("Card inserted");
            String cardHandle = event.getMessage().getParameter().stream().filter(p -> p.getKey().equals("CardHandle")).map(p -> p.getValue()).findFirst().get();

            Integer slotId = Integer.parseInt(event.getMessage().getParameter().stream().filter(p -> p.getKey().equals("SlotID")).map(p -> p.getValue()).findFirst().get());
            String ctId = event.getMessage().getParameter().stream().filter(p -> p.getKey().equals("CtID")).map(p -> p.getValue()).findFirst().get();

            Long endTime = System.currentTimeMillis();
            Pair<Bundle, String> pair;
            try {
                pair = pharmacyService.getEPrescriptionsForCardHandle(cardHandle, null, null);
                Bundle bundle = pair.getKey();
                String eventId = pair.getValue();
                String xml = parser.encodeToString(bundle);
                sendCardLinkMessage("eRezeptTokensFromAVS", Map.of("slotId", slotId, "ctId", ctId, "tokens", xml));

                JsonArrayBuilder bundles = prepareBundles(bundle);
                sendCardLinkMessage("eRezeptBundlesFromAVS", Map.of("slotId", slotId, "ctId", ctId, "bundles", bundles));

                sendCardLinkMessage("vsdmSensorData", Map.of("slotId", slotId, "ctId", ctId, "endTime", endTime, "eventId", eventId));

            } catch (FaultMessage | de.gematik.ws.conn.eventservice.wsdl.v7.FaultMessage e) {
                log.log(Level.WARNING, "Could not get prescription for Bundle", e);
                String code = e instanceof FaultMessage
                    ? ((FaultMessage) e).getFaultInfo().getTrace().get(0).getCode().toString()
                    : ((de.gematik.ws.conn.eventservice.wsdl.v7.FaultMessage) e).getFaultInfo().getTrace().get(0).getCode().toString();
                sendCardLinkMessage("vsdmSensorData", Map.of("slotId", slotId, "ctId", ctId, "endTime", endTime, "err", code));
            }
        }
    }
    
    private JsonArrayBuilder prepareBundles(Bundle bundle) {
        JsonArrayBuilder bundles = Json.createArrayBuilder();
        for (BundleEntryComponent entry : bundle.getEntry()) {
            if (entry.getResource() instanceof org.hl7.fhir.r4.model.Task) {
                /*
                 * <identifier>
                 *    <use value="official"/>
                 *    <system value="https://gematik.de/fhir/erp/NamingSystem/GEM_ERP_NS_PrescriptionId"/>
                 *    <value value="160.000.187.347.039.26"/>
                 *    </identifier>
                 *    <identifier>
                 *    <use value="official"/>
                 *    <system value="https://gematik.de/fhir/erp/NamingSystem/GEM_ERP_NS_AccessCode"/>
                 *    <value value="e624d3e6980e73d397517d0f2219aad553a11c9a8194fca04354eb346edbb266"/>
                 * </identifier>
                 */

                org.hl7.fhir.r4.model.Task task = (org.hl7.fhir.r4.model.Task) entry.getResource();
                String taskId = task.getIdentifier().stream().filter(t -> "https://gematik.de/fhir/erp/NamingSystem/GEM_ERP_NS_PrescriptionId".equals(t.getSystem())).map(t -> t.getValue()).findAny().orElse(null);
                String accessCode = task.getIdentifier().stream().filter(t -> "https://gematik.de/fhir/erp/NamingSystem/GEM_ERP_NS_AccessCode".equals(t.getSystem())).map(t -> t.getValue()).findAny().orElse(null);
                log.info("TaskId: " + taskId + " AccessCode: " + accessCode);
                String token = "/Task/" + taskId + "/$accept?ac=" + accessCode;
                try {
                    Bundle bundleEPrescription = pharmacyService.accept(token, new RuntimeConfig());
                    bundles.add(parser.encodeToString(bundleEPrescription));
                } catch (Exception e) {
                    bundles.add("Error for " + token + " " + e.getMessage());
                }
            }
        }
        return bundles;
    }

    private void sendCardLinkMessage(String type, Map<String, ?> payloadMap) {
        JsonObjectBuilder builder = Json.createObjectBuilder();
        for (Map.Entry<String, ?> entry : payloadMap.entrySet()) {
            if (entry.getValue() instanceof Integer) {
                builder.add(entry.getKey(), (Integer) entry.getValue());
            } else if (entry.getValue() instanceof Long) {
                builder.add(entry.getKey(), (Long) entry.getValue());
            } else if (entry.getValue() instanceof String) {
                builder.add(entry.getKey(), (String) entry.getValue());
            } else if (entry.getValue() instanceof JsonArrayBuilder) {
                builder.add(entry.getKey(), (JsonArrayBuilder) entry.getValue());
            }
        }
        String payload = builder.build().toString();
        JsonObject jsonObject = Json.createObjectBuilder()
            .add("type", type)
            .add("payload", DatatypeConverter.printBase64Binary(payload.getBytes()))
            .build();
        JsonArray jsonArray = Json.createArrayBuilder().add(jsonObject).build();
        cardlinkWebsocketClient.sendMessage(jsonArray.toString());
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        cause.printStackTrace();
        ctx.close();
    }
}
