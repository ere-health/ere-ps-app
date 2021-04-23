package health.ere.ps.service.extractor;

import java.awt.Color;
import java.awt.geom.Rectangle2D.Float;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.enterprise.context.ApplicationScoped;
import javax.xml.namespace.QName;
import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.StartElement;
import javax.xml.stream.events.XMLEvent;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.PDPageContentStream.AppendMode;
import org.apache.pdfbox.text.PDFTextStripperByArea;

import health.ere.ps.service.muster16.Muster16FormDataExtractorService;

@ApplicationScoped
public class SVGExtractor {

    private URI path;
    private boolean debugRectangles = false;
    private static Logger log = Logger.getLogger(SVGExtractor.class.getName());

    private static final float X_OFFSET = 370f;
    private static final float Y_OFFSET = 150f;
    private static final float SCALE = 0.75f;

    public SVGExtractor(URI path, boolean debugRectangles) {
        this.path = path;
        this.debugRectangles = debugRectangles;
    }

    public SVGExtractor(URI path) {
        this(path, false);
    }

    public Map<String, String> extract(InputStream muster16PdfFile) {

        Map<String, String> map = new HashMap<>();
        try {
            PDDocument document = Muster16FormDataExtractorService.createDocumentRotate90(muster16PdfFile);
            XMLInputFactory xmlInputFactory = XMLInputFactory.newInstance();
            XMLEventReader reader = xmlInputFactory.createXMLEventReader(new FileInputStream(new File(path)));
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
                        float x = java.lang.Float.parseFloat(startElement.getAttributeByName(new QName("x")).getValue())*SCALE+X_OFFSET;
                        float y = java.lang.Float.parseFloat(startElement.getAttributeByName(new QName("y")).getValue())*SCALE+Y_OFFSET;
                        float width = java.lang.Float.parseFloat(startElement.getAttributeByName(new QName("width")).getValue())*SCALE;
                        float height = java.lang.Float.parseFloat(startElement.getAttributeByName(new QName("height")).getValue())*SCALE;
                        if(debugRectangles) {
                            PDPageContentStream contentStream = new PDPageContentStream(document, document.getPage(0), AppendMode.APPEND, true);
                            contentStream.addRect(y, x, height, width);
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
            if(debugRectangles) {
                final File file = new File("target/SVGExtractor.pdf");
                document.save(file);
                document.close();
            }
        } catch (XMLStreamException | IOException e) {
            log.log(Level.WARNING, "Could not extract data from template", e);
        }
        return map;
    }

    public static String extractTextAtPosition(PDDocument document, String id, float x, float y, float width, float height) {
        PDFTextStripperByArea textStripper;
        try {
            textStripper = new PDFTextStripperByArea();
            Float rect = new java.awt.geom.Rectangle2D.Float(x, y, width, height);
            textStripper.addRegion(id, rect);
            PDPage docPage = document.getPage(0);
            textStripper.extractRegions(docPage);
            String textForRegion = textStripper.getTextForRegion(id);
            return textForRegion;
        } catch (IOException e) {
            log.log(Level.WARNING, "Could not extract: " + id, e);
            return "";
        }
    }
}
