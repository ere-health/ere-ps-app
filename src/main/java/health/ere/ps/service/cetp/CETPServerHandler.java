package health.ere.ps.service.cetp;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.parser.IParser;
import de.health.service.cetp.AbstractCETPEventHandler;
import de.health.service.cetp.cardlink.CardlinkClient;
import de.health.service.config.api.IUserConfigurations;
import health.ere.ps.config.RuntimeConfig;
import health.ere.ps.service.cetp.tracker.TrackerService;
import health.ere.ps.service.gematik.PharmacyService;
import jakarta.json.Json;
import jakarta.json.JsonArrayBuilder;
import org.apache.commons.lang3.tuple.Pair;
import org.hl7.fhir.r4.model.Bundle;
import org.hl7.fhir.r4.model.Bundle.BundleEntryComponent;
import org.hl7.fhir.r4.model.Identifier;
import org.hl7.fhir.r4.model.Task;
import org.jboss.logging.MDC;

import java.util.Map;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import static de.health.service.cetp.utils.Utils.printException;

public class CETPServerHandler extends AbstractCETPEventHandler {

    private static final Logger log = Logger.getLogger(CETPServerHandler.class.getName());

    TrackerService trackerService;
    PharmacyService pharmacyService;

    IParser parser = FhirContext.forR4().newXmlParser();

    public CETPServerHandler(
        TrackerService trackerService,
        PharmacyService pharmacyService,
        CardlinkClient cardlinkClient
    ) {
        super(cardlinkClient);
        this.trackerService = trackerService;
        this.pharmacyService = pharmacyService;
    }

    @Override
    protected String getTopicName() {
        return "CARD/INSERTED";
    }

    @Override
    protected void processEvent(IUserConfigurations uc, Map<String, String> paramsMap) {
        // Keep MDC names in sync with virtual-nfc-cardlink
        String correlationId = UUID.randomUUID().toString();
        MDC.put("requestCorrelationId", correlationId);
        MDC.put("iccsn", paramsMap.getOrDefault("ICCSN", "NoICCSNProvided"));
        MDC.put("ctid", paramsMap.getOrDefault("CtID", "NoCtIDProvided"));
        MDC.put("slot", paramsMap.getOrDefault("SlotID", "NoSlotIDProvided"));
        log.fine("CARD/INSERTED event received with the following payload: %s".formatted(paramsMap));

        if ("EGK".equalsIgnoreCase(paramsMap.get("CardType")) && paramsMap.containsKey("CardHandle") && paramsMap.containsKey("SlotID") && paramsMap.containsKey("CtID")) {
            String cardHandle = paramsMap.get("CardHandle");
            Integer slotId = Integer.parseInt(paramsMap.get("SlotID"));
            String ctId = paramsMap.get("CtID");
            String iccsn = paramsMap.get("ICCSN");
            String kvnr = paramsMap.get("KVNR");
            Long endTime = System.currentTimeMillis();


            String paramsStr = paramsMap.entrySet().stream()
                .filter(p -> !p.getKey().equals("CardHolderName"))
                .map(p -> String.format("key=%s value=%s", p.getKey(), p.getValue())).collect(Collectors.joining(", "));

            log.fine(String.format("[%s] Card inserted: params: %s", correlationId, paramsStr));
            try {
                RuntimeConfig runtimeConfig = new RuntimeConfig(uc);
                Pair<Bundle, String> pair = pharmacyService.getEPrescriptionsForCardHandle(
                    correlationId, cardHandle, null, runtimeConfig, kvnr
                );
                Bundle bundle = pair.getKey();
                String eventId = pair.getValue();
                String xml = parser.encodeToString(bundle);
                cardlinkClient.sendJson(correlationId, iccsn, "eRezeptTokensFromAVS", Map.of("slotId", slotId, "ctId", ctId, "tokens", xml));

                JsonArrayBuilder bundles = prepareBundles(correlationId, bundle, runtimeConfig);
                cardlinkClient.sendJson(correlationId, iccsn, "eRezeptBundlesFromAVS", Map.of("slotId", slotId, "ctId", ctId, "bundles", bundles));

                cardlinkClient.sendJson(correlationId, iccsn, "vsdmSensorData", Map.of("slotId", slotId, "ctId", ctId, "endTime", endTime, "eventId", eventId));

                trackerService.submit(ctId, uc.getMandantId(), uc.getWorkplaceId(), uc.getClientSystemId());
            } catch (Exception e ) {
                log.log(Level.WARNING, String.format("[%s] Could not get prescription for Bundle", correlationId), e);

                if (e instanceof de.gematik.ws.conn.vsds.vsdservice.v5.FaultMessage faultMessage) {
                    String code = faultMessage.getFaultInfo().getTrace().get(0).getCode().toString();
                    cardlinkClient.sendJson(correlationId, iccsn, "vsdmSensorData", Map.of("slotId", slotId, "ctId", ctId, "endTime", endTime, "err", code));
                }
                if (e instanceof de.gematik.ws.conn.eventservice.wsdl.v7.FaultMessage faultMessage) {
                    String code = faultMessage.getFaultInfo().getTrace().get(0).getCode().toString();
                    cardlinkClient.sendJson(correlationId, iccsn, "vsdmSensorData", Map.of("slotId", slotId, "ctId", ctId, "endTime", endTime, "err", code));
                }

                String error = printException(e);
                cardlinkClient.sendJson(
                    correlationId,
                    iccsn,
                    "receiveTasklistError",
                    Map.of("slotId", slotId, "cardSessionId", "null", "status", 500, "tistatus", "500", "errormessage", error)
                );
            }
        } else {
            String msgFormat = "Ignored \"CARD/INSERTED\" values=%s";
            log.log(Level.FINE, String.format(msgFormat, paramsMap));
        }
    }

    private JsonArrayBuilder prepareBundles(String correlationId, Bundle bundle, RuntimeConfig runtimeConfig) {
        JsonArrayBuilder bundles = Json.createArrayBuilder();
        for (BundleEntryComponent entry : bundle.getEntry()) {
            if (entry.getResource() instanceof Task task) {
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

                String taskId = task.getIdentifier().stream().filter(t -> "https://gematik.de/fhir/erp/NamingSystem/GEM_ERP_NS_PrescriptionId".equals(t.getSystem())).map(Identifier::getValue).findAny().orElse(null);
                String accessCode = task.getIdentifier().stream().filter(t -> "https://gematik.de/fhir/erp/NamingSystem/GEM_ERP_NS_AccessCode".equals(t.getSystem())).map(Identifier::getValue).findAny().orElse(null);
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
}
