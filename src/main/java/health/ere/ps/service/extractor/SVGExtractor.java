package health.ere.ps.service.extractor;

import health.ere.ps.event.PDDocumentEvent;
import health.ere.ps.event.SVGExtractorResultEvent;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.PDPageContentStream.AppendMode;
import org.apache.pdfbox.text.PDFTextStripperByArea;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Event;
import javax.enterprise.event.ObservesAsync;
import javax.inject.Inject;
import javax.xml.namespace.QName;
import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.StartElement;
import javax.xml.stream.events.XMLEvent;
import java.awt.*;
import java.awt.geom.Rectangle2D.Float;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

@ApplicationScoped
public class SVGExtractor {

    private static final Logger log = Logger.getLogger(SVGExtractor.class.getName());
    private final SVGExtractorConfiguration configuration;

    @Inject
    Event<Exception> exceptionEvent;
    @Inject
    Event<SVGExtractorResultEvent> sVGExtractorResultEvent;

    private String templatePath = "/svg-extract-templates/Muster-16-Template.svg";
    private boolean debugRectangles = false;

    public SVGExtractor(SVGExtractorConfiguration configuration) {
        if (configuration.MUSTER_16_TEMPLATE != null && !configuration.MUSTER_16_TEMPLATE.isEmpty()) {
            log.log(Level.INFO, "Using muster 16 template: " + configuration.MUSTER_16_TEMPLATE);
            this.templatePath = configuration.MUSTER_16_TEMPLATE;
        }
        this.configuration = configuration;
    }

    public SVGExtractor(SVGExtractorConfiguration configuration, boolean debugRectangles) {
        this(configuration);
        this.debugRectangles = debugRectangles;
    }

    public void analyzeDocument(@ObservesAsync PDDocumentEvent pDDocumentEvent) {
        log.info("SVGExtractor.analyzeDocument");
        try {
            Map<String, String> extractionResult = extract(pDDocumentEvent.getPDDocument());
            sVGExtractorResultEvent.fireAsync(new SVGExtractorResultEvent(extractionResult));
        } catch (Exception e) {
            log.log(Level.SEVERE, "Could not extract results", e);
            exceptionEvent.fireAsync(e);
        }
    }

    public Map<String, String> extract(PDDocument document) throws IOException, XMLStreamException {
        PDPage page = document.getDocumentCatalog().getPages().get(0);
        if (configuration.ROTATE_DEGREE != 0) {
            page.setRotation(configuration.ROTATE_DEGREE);
        }

        Map<String, String> map = new HashMap<>();
        XMLInputFactory xmlInputFactory = XMLInputFactory.newInstance();
        XMLEventReader reader = xmlInputFactory.createXMLEventReader(getTemplate());
        boolean rectFetchMode = false;

        while (reader.hasNext()) {
            XMLEvent nextEvent = reader.nextEvent();
            if (nextEvent.isStartElement()) {
                StartElement startElement = nextEvent.asStartElement();
                String localPart = startElement.getName().getLocalPart();

                if ("g".equals(localPart)
                        && "fields".equals(startElement.getAttributeByName(new QName("id")).getValue())) {
                    rectFetchMode = true;
                } else if (rectFetchMode && "rect".equals(localPart)) {
                    String id = startElement.getAttributeByName(new QName("id")).getValue();
                    float x = java.lang.Float.parseFloat(startElement.getAttributeByName(new QName("x")).getValue()) * configuration.SCALE + configuration.X_OFFSET;
                    float y = java.lang.Float.parseFloat(startElement.getAttributeByName(new QName("y")).getValue()) * configuration.SCALE + configuration.Y_OFFSET;
                    float width = java.lang.Float.parseFloat(startElement.getAttributeByName(new QName("width")).getValue()) * configuration.SCALE;
                    float height = java.lang.Float.parseFloat(startElement.getAttributeByName(new QName("height")).getValue()) * configuration.SCALE;

                    if (debugRectangles) {
                        PDPageContentStream contentStream = new PDPageContentStream(document, page, AppendMode.APPEND, true);
                        if (configuration.ROTATE_DEGREE == 90)
                            contentStream.addRect(y, x, height, width);
                        else
                            contentStream.addRect(x, y, width, height);
                        contentStream.setStrokingColor(Color.RED);
                        //Drawing a rectangle
                        contentStream.stroke();
                        contentStream.close();
                    }
                    String text = extractTextAtPosition(document, id, x, y, width, height);
                    map.put(id, text);
                }
            }
        }
        if (debugRectangles)
            saveDebugFile(document);
        return map;
    }

    private String extractTextAtPosition(PDDocument document, String id, float x, float y, float width, float height) throws IOException {
        PDFTextStripperByArea textStripper;
        textStripper = new PDFTextStripperByArea();
        PDPage docPage = document.getPage(0);
        // PDRectangle mediaBox = docPage.getMediaBox();
        // log.info("Page: x "+mediaBox.getLowerLeftX()+" y: "+mediaBox.getLowerLeftY()+" width: "+mediaBox.getWidth()+" "+mediaBox.getHeight());
        Float rect = new java.awt.geom.Rectangle2D.Float(x, y, width, height);
        // log.info("Rec: x "+rect.getX()+" y: "+rect.getY()+" width: "+rect.getWidth()+" "+rect.getHeight());
        textStripper.addRegion(id, rect);
        textStripper.extractRegions(docPage);
        return textStripper.getTextForRegion(id);
    }

    private void saveDebugFile(PDDocument document) throws IOException {
        final File file = new File("target/SVGExtractor-" + configuration.NAME + ".pdf");
        document.save(file);
        document.close();
    }

    private InputStream getTemplate() {
        return SVGExtractor.class.getResourceAsStream(templatePath);
    }
}
