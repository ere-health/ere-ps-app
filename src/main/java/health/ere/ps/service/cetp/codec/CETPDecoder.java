package health.ere.ps.service.cetp.codec;

import java.io.StringReader;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.commons.lang3.tuple.Pair;

import de.gematik.ws.conn.eventservice.v7.Event;
import health.ere.ps.model.config.UserConfigurations;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;
import jakarta.xml.bind.JAXBContext;
import jakarta.xml.bind.JAXBException;

public class CETPDecoder extends ByteToMessageDecoder {

    private static final Logger log = Logger.getLogger(CETPDecoder.class.getName());

    static JAXBContext jaxbContext;
    
    static {
        try {
            jaxbContext = JAXBContext.newInstance(Event.class);
        } catch (JAXBException e) {
            log.log(Level.SEVERE, "Failed to create JAXB context", e);
        }
    }
    UserConfigurations userConfigurations;

    public CETPDecoder() {

    }

    public CETPDecoder(UserConfigurations userConfigurations) {
        this.userConfigurations = userConfigurations;
    }

    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) { // (2)
        if(!in.isReadable(4)) {
	    return;
	}
	byte[] header = new byte[4];
        in.readBytes(header);

        if(header[0] != 'C' || header[1] != 'E' || header[2] != 'T' || header[3] != 'P') {
            throw new IllegalArgumentException("Invalid CETP header");
        }

        int lengthOfMessage = in.readInt();

        String message = in.readCharSequence(lengthOfMessage, StandardCharsets.UTF_8).toString();

        log.info(message);

        try {
            Event eventType = (Event) jaxbContext.createUnmarshaller().unmarshal(new StringReader(message));
            out.add(Pair.of(eventType, userConfigurations));
        } catch (JAXBException e) {
            log.log(Level.SEVERE, "Failed to unmarshal CETP message", e);
        }
    }
}
