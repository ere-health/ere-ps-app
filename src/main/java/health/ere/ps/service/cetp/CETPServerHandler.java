package health.ere.ps.service.cetp;

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
import org.apache.commons.lang3.tuple.Pair;
import org.hl7.fhir.r4.model.Bundle;
import org.hl7.fhir.r4.model.Bundle.BundleEntryComponent;
import org.jboss.logging.MDC;

import java.util.Map;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import static health.ere.ps.utils.Utils.printException;

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
        try {
            String correlationId = UUID.randomUUID().toString();
            MDC.put("requestCorrelationId", correlationId); // Keep MDC name in snyc with virtual-nfc-cardlink
            cardlinkWebsocketClient.connect();

            @SuppressWarnings("unchecked")
            Pair<Event, UserConfigurations> input = (Pair<Event, UserConfigurations>) msg;
            Event event = input.getKey();

            if (event.getTopic().equals("CARD/INSERTED")) {
                final Map<String, String> eventMap = event.getMessage().getParameter().stream()
                        .collect(Collectors.toMap(Event.Message.Parameter::getKey, Event.Message.Parameter::getValue));

                // Keep MDC names in sync with virtual-nfc-cardlink
                MDC.put("iccsn", eventMap.getOrDefault("ICCSN", "NoICCSNProvided"));
                MDC.put("ctid", eventMap.getOrDefault("CtID", "NoCtIDProvided"));
                MDC.put("slot", eventMap.getOrDefault("SlotID", "NoSlotIDProvided"));
                log.fine("CARD/INSERTED event received with the following payload: %s".formatted(eventMap));

                if ("EGK".equalsIgnoreCase(eventMap.get("CardType")) && eventMap.containsKey("CardHandle") && eventMap.containsKey("SlotID") && eventMap.containsKey("CtID")) {
                    String cardHandle = eventMap.get("CardHandle");
                    Integer slotId = Integer.parseInt(eventMap.get("SlotID"));
                    String ctId = eventMap.get("CtID");
                    String iccsn = eventMap.get("ICCSN");
                    Long endTime = System.currentTimeMillis();


                    String paramsStr = event.getMessage().getParameter().stream()
                            .filter(p -> !p.getKey().equals("CardHolderName"))
                            .map(p -> String.format("key=%s value=%s", p.getKey(), p.getValue())).collect(Collectors.joining(", "));

                    log.fine(String.format("[%s] Card inserted: params: %s", correlationId, paramsStr));
                    try {
                        RuntimeConfig runtimeConfig = new RuntimeConfig(input.getValue());
                        Pair<Bundle, String> pair = pharmacyService.getEPrescriptionsForCardHandle(
                                correlationId, cardHandle, null, runtimeConfig
                        );
                        Bundle bundle = pair.getKey();
                        String eventId = pair.getValue();
                        String xml = parser.encodeToString(bundle);
                        cardlinkWebsocketClient.sendJson(correlationId, iccsn, "eRezeptTokensFromAVS", Map.of("slotId", slotId, "ctId", ctId, "tokens", xml));

                        JsonArrayBuilder bundles = prepareBundles(correlationId, bundle, runtimeConfig);
                        cardlinkWebsocketClient.sendJson(correlationId, iccsn, "eRezeptBundlesFromAVS", Map.of("slotId", slotId, "ctId", ctId, "bundles", bundles));

                        cardlinkWebsocketClient.sendJson(correlationId, iccsn, "vsdmSensorData", Map.of("slotId", slotId, "ctId", ctId, "endTime", endTime, "eventId", eventId));

                    } catch (FaultMessage | de.gematik.ws.conn.eventservice.wsdl.v7.FaultMessage e) {
                        log.log(Level.WARNING, String.format("[%s] Could not get prescription for Bundle", correlationId), e);
                        String code = e instanceof FaultMessage
                                ? ((FaultMessage) e).getFaultInfo().getTrace().get(0).getCode().toString()
                                : ((de.gematik.ws.conn.eventservice.wsdl.v7.FaultMessage) e).getFaultInfo().getTrace().get(0).getCode().toString();
                        cardlinkWebsocketClient.sendJson(correlationId, iccsn, "vsdmSensorData", Map.of("slotId", slotId, "ctId", ctId, "endTime", endTime, "err", code));

                        String error = printException(e);
                        cardlinkWebsocketClient.sendJson(
                            correlationId,
                            iccsn,
                            "receiveTasklistError",
                            Map.of("cardSessionId", "null", "status", 500, "tistatus", "500", "errormessage", error)
                        );
                    }
                } else {
                    String msgFormat = "Ignored \"CARD/INSERTED\" event=%s: values=%s";
                    log.log(Level.INFO, String.format(msgFormat, event.getMessage(), eventMap));
                }
            }

        } finally {
            cardlinkWebsocketClient.close();
            MDC.clear();
        }
    }

    private JsonArrayBuilder prepareBundles(String correlationId, Bundle bundle, RuntimeConfig runtimeConfig) {
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
                log.fine("TaskId: " + taskId + " AccessCode: " + accessCode);
                String token = "/Task/" + taskId + "/$accept?ac=" + accessCode;
                try {
                    Bundle bundleEPrescription = pharmacyService.accept(correlationId, token, runtimeConfig);
                    bundles.add(parser.encodeToString(bundleEPrescription));
                } catch (Exception e) {
                    bundles.add(String.format("[%s] Error for %s -> %s", correlationId, token, e.getMessage()));
                }
            }
        }
        return bundles;
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        log.log(Level.SEVERE, "Caught an exception handling CETP input", cause);
        ctx.close();
    }
}
