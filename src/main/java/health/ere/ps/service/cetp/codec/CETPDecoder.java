package health.ere.ps.service.cetp.codec;

import java.io.StringReader;
import java.nio.charset.Charset;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;

import de.gematik.ws.conn.eventservice.v7.Event;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;

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

    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) { // (2)
        byte[] header = new byte[4];
        in.readBytes(header);

        if(header[0] != 'C' || header[1] != 'E' || header[2] != 'T' || header[3] != 'P') {
            throw new IllegalArgumentException("Invalid CETP header");
        }

        int lengthOfMessage = in.readInt();

        String message = in.readCharSequence(lengthOfMessage, Charset.defaultCharset()).toString();

        log.info(message);

        try {
            Event eventType = (Event) jaxbContext.createUnmarshaller().unmarshal(new StringReader(message));
            out.add(eventType);
        } catch (JAXBException e) {
            log.log(Level.SEVERE, "Failed to unmarshal CETP message", e);
        }
    }
}
