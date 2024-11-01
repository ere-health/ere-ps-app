package health.ere.ps.service.cetp.codec;

import de.gematik.ws.conn.eventservice.v7.Event;
import de.health.service.config.api.IUserConfigurations;
import de.health.service.cetp.domain.eventservice.event.DecodeResult;
import de.health.service.cetp.domain.eventservice.event.mapper.CetpEventMapper;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;
import jakarta.xml.bind.JAXBContext;
import jakarta.xml.bind.JAXBException;

import java.io.StringReader;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

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

    IUserConfigurations configurations;
    CetpEventMapper eventMapper;

    public CETPDecoder() {
    }

    public CETPDecoder(IUserConfigurations configurations, CetpEventMapper eventMapper) {
        this.configurations = configurations;
        this.eventMapper = eventMapper;
    }

    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) { // (2)
        if (!in.isReadable(4)) {
            return;
        }
        byte[] header = new byte[4];
        in.readBytes(header);
        if (header[0] != 'C' || header[1] != 'E' || header[2] != 'T' || header[3] != 'P') {
            throw new IllegalArgumentException("Invalid CETP header");
        }

        int lengthOfMessage = in.readInt();
        String message = in.readCharSequence(lengthOfMessage, StandardCharsets.UTF_8).toString();
        log.info(message);
        try {
            Event eventType = (Event) jaxbContext.createUnmarshaller().unmarshal(new StringReader(message));
            out.add(new DecodeResult(eventMapper.toDomain(eventType), configurations));
        } catch (JAXBException e) {
            log.log(Level.SEVERE, "Failed to unmarshal CETP message", e);
        }
    }
}
