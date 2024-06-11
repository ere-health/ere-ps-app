package health.ere.ps.service.cetp;

import static health.ere.ps.utils.Utils.printException;

import java.util.Map;
import java.util.Optional;
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
import jakarta.json.JsonArrayBuilder;

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
        cardlinkWebsocketClient.connect();
        
        @SuppressWarnings("unchecked")
        Pair<Event, UserConfigurations> input = (Pair<Event, UserConfigurations>) msg;
        Event event = input.getKey();

        if (event.getTopic().equals("CARD/INSERTED")) {
            log.info("Card inserted");
            Optional<String> cardHandleOpt = event.getMessage().getParameter().stream()
                .filter(p -> p.getKey().equals("CardHandle"))
                .map(Event.Message.Parameter::getValue)
                .findFirst();

            Optional<String> slotIdOpt = event.getMessage().getParameter().stream()
                .filter(p -> p.getKey().equals("SlotID"))
                .map(Event.Message.Parameter::getValue)
                .findFirst();

            Optional<String> ctIdOpt = event.getMessage().getParameter().stream()
                .filter(p -> p.getKey().equals("CtID"))
                .map(Event.Message.Parameter::getValue)
                .findFirst();

            if (cardHandleOpt.isPresent() && slotIdOpt.isPresent() && ctIdOpt.isPresent()) {
                String cardHandle = cardHandleOpt.get();
                Integer slotId = Integer.parseInt(slotIdOpt.get());
                String ctId = ctIdOpt.get();
                Long endTime = System.currentTimeMillis();
                Pair<Bundle, String> pair;
                try {
                    RuntimeConfig runtimeConfig = new RuntimeConfig(input.getValue());
                    pair = pharmacyService.getEPrescriptionsForCardHandle(cardHandle, null, runtimeConfig);
                    Bundle bundle = pair.getKey();
                    String eventId = pair.getValue();
                    String xml = parser.encodeToString(bundle);
                    cardlinkWebsocketClient.sendJson("eRezeptTokensFromAVS", Map.of("slotId", slotId, "ctId", ctId, "tokens", xml));

                    JsonArrayBuilder bundles = prepareBundles(bundle, runtimeConfig);
                    cardlinkWebsocketClient.sendJson("eRezeptBundlesFromAVS", Map.of("slotId", slotId, "ctId", ctId, "bundles", bundles));

                    cardlinkWebsocketClient.sendJson("vsdmSensorData", Map.of("slotId", slotId, "ctId", ctId, "endTime", endTime, "eventId", eventId));

                } catch (FaultMessage | de.gematik.ws.conn.eventservice.wsdl.v7.FaultMessage e) {
                    log.log(Level.WARNING, "Could not get prescription for Bundle", e);
                    String code = e instanceof FaultMessage
                        ? ((FaultMessage) e).getFaultInfo().getTrace().get(0).getCode().toString()
                        : ((de.gematik.ws.conn.eventservice.wsdl.v7.FaultMessage) e).getFaultInfo().getTrace().get(0).getCode().toString();
                    cardlinkWebsocketClient.sendJson("vsdmSensorData", Map.of("slotId", slotId, "ctId", ctId, "endTime", endTime, "err", code));

                    String error = "ERROR: " + printException(e);
                    cardlinkWebsocketClient.sendJson("eRezeptTokensFromAVS", Map.of("slotId", slotId, "ctId", ctId, "tokens", error));
                }
            } else {
                String msgFormat = "Error while handling \"CARD/INSERTED\" event=%s: cardHandle=%s, slotId=%s, ctId=%s";
                log.log(Level.SEVERE, String.format(msgFormat, event.getMessage(), cardHandleOpt, slotIdOpt, ctIdOpt));
            }
        }
        cardlinkWebsocketClient.close();
    }
    
    private JsonArrayBuilder prepareBundles(Bundle bundle, RuntimeConfig runtimeConfig) {
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
                    Bundle bundleEPrescription = pharmacyService.accept(token, runtimeConfig);
                    bundles.add(parser.encodeToString(bundleEPrescription));
                } catch (Exception e) {
                    bundles.add("Error for " + token + " " + e.getMessage());
                }
            }
        }
        return bundles;
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        cause.printStackTrace();
        ctx.close();
    }
}
