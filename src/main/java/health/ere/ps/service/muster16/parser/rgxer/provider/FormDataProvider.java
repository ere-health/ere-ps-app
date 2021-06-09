package health.ere.ps.service.muster16.parser.rgxer.provider;

import health.ere.ps.service.muster16.parser.rgxer.model.FormRecord;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.StartElement;
import javax.xml.stream.events.XMLEvent;
import java.io.FileNotFoundException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

public class FormDataProvider implements DataProvider<FormRecord> {

    private final List<FormRecord> records;

    private static final Logger log = Logger.getLogger(DataProvider.class.getName());

    public FormDataProvider() {
        List<FormRecord> _records;
        try {
            _records = loadFromFile();
        } catch (XMLStreamException | FileNotFoundException | URISyntaxException e) {
            _records = new ArrayList<>();
            log.severe("Failed to load data");
        }
        records = _records;
    }

    @Override
    public List<FormRecord> getRecords() {
        return records;
    }

    @Override
    public String getFilePath() {
        return "/data/S_KBV_DARREICHUNGSFORM_V1.08.xml";
    }

    private List<FormRecord> loadFromFile() throws URISyntaxException, XMLStreamException, FileNotFoundException {
        List<FormRecord> records = new ArrayList<>();
        XMLInputFactory xmlInputFactory = XMLInputFactory.newInstance();
        XMLEventReader reader = xmlInputFactory.createXMLEventReader(getDataFile());
        while (reader.hasNext()) {
            XMLEvent nextEvent = reader.nextEvent();
            if (nextEvent.isStartElement()) {
                StartElement startElement = nextEvent.asStartElement();
                String localPart = startElement.getName().getLocalPart();
                if (localPart.equals("key")) {
                    String name = startElement.getAttributeByName(new QName("DN")).getValue();
                    String code = startElement.getAttributeByName(new QName("V")).getValue();
                    records.add(new FormRecord(name, code));
                }
            }
        }
        return records;
    }
}
