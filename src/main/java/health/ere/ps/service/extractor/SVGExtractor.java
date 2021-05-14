package health.ere.ps.service.extractor;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.PDPageContentStream.AppendMode;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.text.PDFTextStripperByArea;

import java.awt.Color;
import java.awt.geom.Rectangle2D.Float;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.context.RequestScoped;
import javax.enterprise.event.Event;
import javax.enterprise.event.Observes;
import javax.enterprise.event.ObservesAsync;
import javax.inject.Inject;
import javax.xml.namespace.QName;
import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.StartElement;
import javax.xml.stream.events.XMLEvent;

import health.ere.ps.event.PDDocumentEvent;
import health.ere.ps.event.SVGExtractorResultEvent;

@ApplicationScoped
public class SVGExtractor {

    private static final Logger log = Logger.getLogger(SVGExtractor.class.getName()); 

    @Inject
    Event<Exception> exceptionEvent;

    @Inject
    Event<SVGExtractorResultEvent> sVGExtractorResultEvent;

    private URI path;
    private boolean debugRectangles = false;

    private SVGExtractorConfiguration configuration = SVGExtractorConfiguration.CGM_TURBO_MED;

    public SVGExtractor() throws URISyntaxException {
        this(SVGExtractor.class.getResource("/svg-extract-templates/Muster-16-Template.svg").toURI());
    }

    public SVGExtractor(SVGExtractorConfiguration configuration) throws URISyntaxException {
        this();
        this.configuration = configuration;
    }
    public SVGExtractor(SVGExtractorConfiguration configuration, boolean debugRectangles) throws URISyntaxException {
        this();
        this.configuration = configuration;
        this.setDebugRectangles(debugRectangles);
    }

    public SVGExtractor(URI path) {
        this(path, false);
    }

    public SVGExtractor(URI path, boolean debugRectangles) {
        this.setPath(path);
        this.setDebugRectangles(debugRectangles);
    }

    public void analyzeDocument(@ObservesAsync PDDocumentEvent pDDocumentEvent) {
        log.info("SVGExtractor.analyzeDocument");
        try {
            Map<String, String> extractResult = extract(pDDocumentEvent.pDDocument);
            sVGExtractorResultEvent.fireAsync(new SVGExtractorResultEvent(extractResult));
        } catch (Exception e) {
            log.log(Level.SEVERE, "Could not extract results", e);
            exceptionEvent.fireAsync(e);
        }

    }
    
    public Map<String, String> extract(PDDocument document) throws IOException, XMLStreamException {
        PDPage page = document.getDocumentCatalog().getPages().get(0);
        if(configuration.ROTATE_DEGREE != 0) {
            page.setRotation(configuration.ROTATE_DEGREE);
        }
        Map<String, String> map = new HashMap<>();
        XMLInputFactory xmlInputFactory = XMLInputFactory.newInstance();
        XMLEventReader reader = xmlInputFactory.createXMLEventReader(new FileInputStream(new File(getPath())));
        boolean rectFetchMode = false;
        while (reader.hasNext()) {
            XMLEvent nextEvent = reader.nextEvent();
            if (nextEvent.isStartElement()) {
                StartElement startElement = nextEvent.asStartElement();
                String localPart = startElement.getName().getLocalPart();
                if ("g".equals(localPart)
                        && "fields".equals(startElement.getAttributeByName(new QName("id")).getValue())) {
                    rectFetchMode = true;
                } else if(rectFetchMode && "rect".equals(localPart)) {
                    String id = startElement.getAttributeByName(new QName("id")).getValue();
                    float x = java.lang.Float.parseFloat(startElement.getAttributeByName(new QName("x")).getValue())*configuration.SCALE+configuration.X_OFFSET;
                    float y = java.lang.Float.parseFloat(startElement.getAttributeByName(new QName("y")).getValue())*configuration.SCALE+configuration.Y_OFFSET;
                    float width = java.lang.Float.parseFloat(startElement.getAttributeByName(new QName("width")).getValue())*configuration.SCALE;
                    float height = java.lang.Float.parseFloat(startElement.getAttributeByName(new QName("height")).getValue())*configuration.SCALE;
                    if(isDebugRectangles()) {
                        PDPageContentStream contentStream = new PDPageContentStream(document, page, AppendMode.APPEND, true);
                        if(configuration.ROTATE_DEGREE == 90) {
                            contentStream.addRect(y, x, height, width);
                        } else {
                            contentStream.addRect(x, y, width, height);
                        }
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
        if(isDebugRectangles()) {
            final File file = new File("target/SVGExtractor-"+configuration.NAME+".pdf");
            document.save(file);
            document.close();
        }
        return map;
    }

    public String extractTextAtPosition(PDDocument document, String id, float x, float y, float width, float height) throws IOException {
        PDFTextStripperByArea textStripper;
        textStripper = new PDFTextStripperByArea();
        PDPage docPage = document.getPage(0);
        // PDRectangle mediaBox = docPage.getMediaBox();
        // log.info("Page: x "+mediaBox.getLowerLeftX()+" y: "+mediaBox.getLowerLeftY()+" width: "+mediaBox.getWidth()+" "+mediaBox.getHeight());
        Float rect = new java.awt.geom.Rectangle2D.Float(x, y, width, height);
        // log.info("Rec: x "+rect.getX()+" y: "+rect.getY()+" width: "+rect.getWidth()+" "+rect.getHeight());
        textStripper.addRegion(id, rect);
        textStripper.extractRegions(docPage);
        String textForRegion = textStripper.getTextForRegion(id);
        return textForRegion;
    }

    public URI getPath() {
        return path;
    }

    public void setPath(URI path) {
        this.path = path;
    }

    public boolean isDebugRectangles() {
        return debugRectangles;
    }

    public void setDebugRectangles(boolean debugRectangles) {
        this.debugRectangles = debugRectangles;
    }
}
