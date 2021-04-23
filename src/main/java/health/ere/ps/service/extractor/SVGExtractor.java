package health.ere.ps.service.extractor;

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
import org.apache.pdfbox.text.PDFTextStripperByArea;

import health.ere.ps.service.muster16.Muster16FormDataExtractorService;

@ApplicationScoped
public class SVGExtractor {

    private URI path;
    private static Logger log = Logger.getLogger(SVGExtractor.class.getName());

    public SVGExtractor(URI path) {
        this.path = path;
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
                        float x = java.lang.Float.parseFloat(startElement.getAttributeByName(new QName("x")).getValue());
                        float y = java.lang.Float.parseFloat(startElement.getAttributeByName(new QName("y")).getValue());
                        float width = java.lang.Float.parseFloat(startElement.getAttributeByName(new QName("width")).getValue());
                        float height = java.lang.Float.parseFloat(startElement.getAttributeByName(new QName("height")).getValue());
                        String text = extractTextAtPosition(document, id, x, y, width, height);
                        map.put(id, text);
                    }
                }
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
