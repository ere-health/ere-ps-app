package health.ere.ps.service.cetp;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonArrayBuilder;
import javax.json.JsonObject;

import org.hl7.fhir.r4.model.Bundle;
import org.hl7.fhir.r4.model.Bundle.BundleEntryComponent;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.parser.IParser;
import de.gematik.ws.conn.eventservice.v7.Event;
import de.gematik.ws.conn.vsds.vsdservice.v5.FaultMessage;
import health.ere.ps.config.RuntimeConfig;
import health.ere.ps.service.cardlink.CardlinkWebsocketClient;
import health.ere.ps.service.gematik.PharmacyService;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;


public class CETPServerHandler extends ChannelInboundHandlerAdapter {

    private static final Logger log = Logger.getLogger(CETPServerHandler.class.getName());
    
    PharmacyService pharmacyService;

    CardlinkWebsocketClient cardlinkWebsocketClient;

    IParser parser = FhirContext.forR4().newXmlParser();
    
    public CETPServerHandler(PharmacyService pharmacyService, String cardLinkServer) {
        this.pharmacyService = pharmacyService;
        try {
            log.info("Starting websocket connection to: "+cardLinkServer);
            cardlinkWebsocketClient = new CardlinkWebsocketClient(new URI(cardLinkServer));
        } catch (URISyntaxException e) {
            log.log(Level.WARNING, "Could not connect to card link", e);
        }
    }

    @Override
    public void handlerAdded(ChannelHandlerContext ctx) {
    }
    
    @Override
    public void handlerRemoved(ChannelHandlerContext ctx) {
    }
    
    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) {
        Event event = (Event) msg;

        if(event.getTopic().equals("CARD/INSERTED")) {
            log.info("Card inserted");
            String cardHandle = event.getMessage().getParameter().stream().filter(p -> p.getKey().equals("CardHandle")).map(p -> p.getValue()).findFirst().get();

            int SlotID = Integer.parseInt(event.getMessage().getParameter().stream().filter(p -> p.getKey().equals("SlotID")).map(p -> p.getValue()).findFirst().get());
            String CtID = event.getMessage().getParameter().stream().filter(p -> p.getKey().equals("CtID")).map(p -> p.getValue()).findFirst().get();

            try {
                Bundle bundle = pharmacyService.getEPrescriptionsForCardHandle(cardHandle, null, null);
                String xml = parser.encodeToString(bundle);
                JsonObject j = Json.createObjectBuilder().add("type", "eRezeptTokensFromAVS").add("SlotId", SlotID).add("CtID", CtID).add("tokens", xml).build();
                JsonArray jArray = Json.createArrayBuilder().add(j).build();
                String jsonMessage = jArray.toString();
                log.info(jsonMessage);
                cardlinkWebsocketClient.sendMessage(jsonMessage);

                JsonArrayBuilder bundles = Json.createArrayBuilder();
                for(BundleEntryComponent entry : bundle.getEntry()) {
                    if(entry.getResource() instanceof org.hl7.fhir.r4.model.Task) {
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
                        log.info("TaskId: "+taskId+" AccessCode: "+accessCode);
                        String token = "/Task/"+taskId+"/$accept?ac="+accessCode;
                        try {
                            Bundle bundleEPrescription = pharmacyService.accept(token, new RuntimeConfig());
                            bundles.add(parser.encodeToString(bundleEPrescription));
                            
                        } catch (Exception e) {
                            bundles.add("Error for "+token+" "+e.getMessage());
                        }
                    }
                }

                JsonObject eRezeptBundlesFromAVS = Json.createObjectBuilder().add("type", "eRezeptBundlesFromAVS").add("SlotId", SlotID).add("CtID", CtID).add("bundles", bundles).build();

                jArray = Json.createArrayBuilder().add(eRezeptBundlesFromAVS).build();
                log.info(jArray.toString());
                cardlinkWebsocketClient.sendMessage(jArray.toString());
                
            } catch (FaultMessage | de.gematik.ws.conn.eventservice.wsdl.v7.FaultMessage e) {
                log.log(Level.WARNING, "Could not get prescription for Bundle", e);
            }
        }
        
    }
    
    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        cause.printStackTrace();
        ctx.close();
    }
}
