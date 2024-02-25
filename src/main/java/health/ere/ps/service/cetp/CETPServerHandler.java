package health.ere.ps.service.cetp;

import java.io.StringReader;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonObject;

import org.hl7.fhir.r4.model.Bundle;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.parser.IParser;
import de.gematik.ws.conn.eventservice.v7.Event;
import de.gematik.ws.conn.vsds.vsdservice.v5.FaultMessage;
import health.ere.ps.service.cardlink.CardlinkWebsocketClient;
import health.ere.ps.service.gematik.PharmacyService;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;


public class CETPServerHandler extends ChannelInboundHandlerAdapter {

    private static final Logger log = Logger.getLogger(CETPServerHandler.class.getName());
    
    PharmacyService pharmacyService;

    CardlinkWebsocketClient cardlinkWebsocketClient;

    IParser parser = FhirContext.forR4().newXmlParser();
    
    public CETPServerHandler(PharmacyService pharmacyService) {
        this.pharmacyService = pharmacyService;
        try {
            cardlinkWebsocketClient = new CardlinkWebsocketClient(new URI("wss://cardlink.service-health.de:8444/websocket/80276003650110006580-20230112"));
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
                JsonObject j = Json.createObjectBuilder().add("type", "ERezeptTokensFromAVS").add("SlotId", SlotID).add("CtID", CtID).add("tokens", xml).build();
                JsonArray jArray = Json.createArrayBuilder().add(j).build();
                String jsonMessage = jArray.toString();
                log.info(jsonMessage);
                cardlinkWebsocketClient.sendMessage(jsonMessage);
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
