package health.ere.ps.service.gematik;

import de.gematik.ws.conn.vsds.vsdservice.v5.ReadVSDResponse;
import jakarta.xml.bind.JAXBContext;
import jakarta.xml.bind.JAXBException;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.zip.GZIPInputStream;

import static java.nio.charset.StandardCharsets.UTF_8;

public class ReadVSDHelper {


    private static final Logger log = Logger.getLogger(ReadVSDHelper.class.getName());

    static JAXBContext readVSDJaxbContext;

    static {
        try {
            readVSDJaxbContext = JAXBContext.newInstance(ReadVSDResponse.class);
        } catch (JAXBException e) {
            log.log(Level.SEVERE, "Could not create JAXBContext for ReadVSDResponse", e);
        }
    }

    public static String asString(ReadVSDResponse readVSDResponse) {
        try {
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            readVSDJaxbContext.createMarshaller().marshal(readVSDResponse, outputStream);
            return outputStream.toString(UTF_8);
        } catch (JAXBException e) {
            log.log(Level.SEVERE, "Could not serialize ReadVSDResponse", e);
            return "Serialization error: " + e.getMessage();
        }
    }

    public static InputStream ungzip(byte[] bytes) throws IOException {
        return new GZIPInputStream(new ByteArrayInputStream(bytes));
    }
}
