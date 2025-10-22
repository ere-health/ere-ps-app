package health.ere.ps.service.kbv;

import ca.uhn.fhir.context.FhirContext;
import health.ere.ps.event.HTMLBundlesEvent;
import health.ere.ps.event.ReadyToSignBundlesEvent;
import health.ere.ps.service.fhir.FHIRService;
import health.ere.ps.websocket.ExceptionWithReplyToException;
import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.Event;
import jakarta.enterprise.event.ObservesAsync;
import jakarta.inject.Inject;
import org.apache.commons.io.output.ByteArrayOutputStream;
import org.hl7.fhir.r4.model.Bundle;

import javax.xml.XMLConstants;
import javax.xml.transform.ErrorListener;
import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.Collection;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

@ApplicationScoped
public class XSLTService {

    private static final Logger log = Logger.getLogger(XSLTService.class.getName());

    private static final FhirContext fhirContext = FHIRService.getFhirContext();

    @Inject
    Event<Exception> exceptionEvent;

    @Inject
    Event<HTMLBundlesEvent> hTMLBundlesEvent;

    Transformer transformer;

    @PostConstruct
    public void init() {
        try {
            // Step 4: Setup JAXP using identity transformer
            TransformerFactory factory = TransformerFactory.newInstance("net.sf.saxon.TransformerFactoryImpl", null);
            factory.setAttribute(XMLConstants.ACCESS_EXTERNAL_DTD, "");
            factory.setAttribute(XMLConstants.ACCESS_EXTERNAL_STYLESHEET, "");

            // with XSLT:
            String xslPath = "/kbv-xslt/ERP_Stylesheet.xslt";

            InputStream inputStream = getClass().getResourceAsStream(xslPath);
            String systemId = this.getClass().getResource(xslPath).toExternalForm();
            StreamSource xslt = new StreamSource(inputStream, systemId);
            xslt.setPublicId(systemId);
            factory.setErrorListener(new ErrorListener() {
                private static final String MSG = "Error in XSLT:";

                @Override
                public void warning(TransformerException exception) {
                    log.warning(MSG + exception);

                }

                @Override
                public void fatalError(TransformerException exception) {
                    log.severe(MSG + exception);

                }

                @Override
                public void error(TransformerException exception) {
                    log.severe(MSG + exception);
                }
            });

            transformer = factory.newTransformer(xslt);
        } catch (Exception e) {
            log.log(Level.SEVERE, "Could not init XSLTService", e);
        }
    }

    public String generateHtmlForBundle(Bundle bundle) throws IOException, TransformerException {
        String xmlString = fhirContext.newXmlParser().encodeResourceToString(bundle);
        return generateHtmlForString(xmlString);
    }

    public String generateHtmlForString(String xmlString) throws IOException, TransformerException {
        File xml = Files.createTempFile("bundle-", ".xml").toFile();
        Files.writeString(xml.toPath(), xmlString);

        // Step 2: Set up output stream.
        // Note: Using BufferedOutputStream for performance reasons (helpful with
        // FileOutputStreams).
        ByteArrayOutputStream out = new ByteArrayOutputStream();

        // Step 5: Setup input and output for XSLT transformation
        // Setup input stream
        Source src = new StreamSource(xml);

        // Resulting SAX events (the generated FO) must be piped through to FOP
        Result res = new StreamResult(out);

        // Step 6: Start XSLT transformation and FOP processing
        transformer.transform(src, res);

        return new String(out.toByteArray(), StandardCharsets.UTF_8);
    }

    public void onReadyToSignBundlesEvent(@ObservesAsync ReadyToSignBundlesEvent readyToSignBundlesEvent) {
        log.info(String.format("Received %d bundles to show for signature ", readyToSignBundlesEvent.listOfListOfBundles.size()));
        try {
            List<String> htmlBundlesList = readyToSignBundlesEvent.listOfListOfBundles.stream().flatMap(Collection::stream).map(bundle -> {
                try {
                    return generateHtmlForBundle(bundle);
                } catch (Exception e) {
                    exceptionEvent.fireAsync(new ExceptionWithReplyToException(e, readyToSignBundlesEvent.getReplyTo(), readyToSignBundlesEvent.getReplyToMessageId()));
                    return "";
                }
            }).collect(Collectors.toList());
            hTMLBundlesEvent.fireAsync(new HTMLBundlesEvent(htmlBundlesList, readyToSignBundlesEvent.getReplyTo(), readyToSignBundlesEvent.getReplyToMessageId()));
        } catch(Exception ex) {
            exceptionEvent.fireAsync(new ExceptionWithReplyToException(ex, readyToSignBundlesEvent.getReplyTo(), readyToSignBundlesEvent.getReplyToMessageId()));
        }
    }
}
