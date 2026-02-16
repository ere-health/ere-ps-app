package health.ere.ps.service.gematik.popp;

import de.gematik.ws.conn.certificateservice.v6.ReadCardCertificateResponse;
import de.gematik.ws.conn.signatureservice.v7.ExternalAuthenticateResponse;
import jakarta.xml.bind.JAXBContext;
import jakarta.xml.bind.Marshaller;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.StringWriter;

import static jakarta.xml.bind.Marshaller.JAXB_FORMATTED_OUTPUT;
import static jakarta.xml.bind.Marshaller.JAXB_FRAGMENT;
import static java.lang.Boolean.TRUE;

public class XmlUtils {

    private static final Logger log = LoggerFactory.getLogger(XmlUtils.class.getName());

    private static JAXBContext jaxbContext;

    private XmlUtils() {
    }

    static {
        try {
            jaxbContext = createJaxbContext();
        } catch (Exception e) {
            log.error("Could create parser", e);
        }
    }

    private static JAXBContext createJaxbContext() throws Exception {
        return JAXBContext.newInstance(
            ReadCardCertificateResponse.class,
            ExternalAuthenticateResponse.class
        );
    }

    public static String print(Object object, boolean formatted) {
        try {
            Marshaller marshaller = jaxbContext.createMarshaller();
            marshaller.setProperty(JAXB_FORMATTED_OUTPUT, formatted);
            marshaller.setProperty(JAXB_FRAGMENT, TRUE);
            StringWriter sw = new StringWriter();
            marshaller.marshal(object, sw);
            return """
                <SOAP-ENV:Envelope xmlns:SOAP-ENV="http://schemas.xmlsoap.org/soap/envelope/">
                    <SOAP-ENV:Header/>
                        <SOAP-ENV:Body>
                            %s
                    </SOAP-ENV:Body>
                </SOAP-ENV:Envelope>""".formatted(sw.toString());
        } catch (Exception e) {
            log.error("Error converting object to XML", e);
            return e.getMessage();
        }
    }
}