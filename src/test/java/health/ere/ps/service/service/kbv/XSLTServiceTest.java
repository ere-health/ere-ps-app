package health.ere.ps.service.kbv;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import javax.xml.transform.TransformerException;

import org.hl7.fhir.r4.model.Bundle;
import org.junit.jupiter.api.Test;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.parser.IParser;

public class XSLTServiceTest {
    IParser parser = FhirContext.forR4().newXmlParser();

    @Test
    public void testGenerateHTMLForPF01() throws IOException, TransformerException {
        Bundle bundle = parser.parseResource(Bundle.class, getXmlString("src/test/resources/kbv-zip/PF01.xml"));
        XSLTService xsltService = new XSLTService();
        xsltService.init();
        String result = xsltService.generateHtmlForBundle(bundle);

        Files.write(Paths.get("src/test/resources/kbv-xslt/PF01.html"), result.getBytes());
    }

    @Test
    public void testGenerateHTMLForPF08() throws IOException, TransformerException {
        List<Bundle> bundles = new ArrayList<>();
        bundles.add(parser.parseResource(Bundle.class, getXmlString("src/test/resources/kbv-zip/PF08_1.xml")));
        bundles.add(parser.parseResource(Bundle.class, getXmlString("src/test/resources/kbv-zip/PF08_2.xml")));
        bundles.add(parser.parseResource(Bundle.class, getXmlString("src/test/resources/kbv-zip/PF08_3.xml")));
        XSLTService xsltService = new XSLTService();
        xsltService.init();
        List<String> result = bundles.stream().map(bundle -> {
            try {
                return xsltService.generateHtmlForBundle(bundle);
            } catch (IOException | TransformerException e) {
                e.printStackTrace();
                return null;
            }
        }).collect(Collectors.toList());

        Files.write(Paths.get("src/test/resources/kbv-xslt/PF08_1.html"), result.get(0).getBytes());
        Files.write(Paths.get("src/test/resources/kbv-xslt/PF08_2.html"), result.get(1).getBytes());
        Files.write(Paths.get("src/test/resources/kbv-xslt/PF08_3.html"), result.get(2).getBytes());
    }

    @Test
    public void testGenerateHTMLForOtherPF() throws IOException, TransformerException {
        List<Integer> otherPF = List.of(2, 3, 4, 5, 7, 9, 10);
        XSLTService xsltService = new XSLTService();
        xsltService.init();
        for (Integer number : otherPF) {
            String filename = String.format("src/test/resources/kbv-zip/PF%02d.xml", number);
            System.out.println(filename);
            Bundle bundle = parser.parseResource(Bundle.class, getXmlString(filename));
            String result = xsltService.generateHtmlForBundle(bundle);

            filename = String.format("src/test/resources/kbv-xslt/PF%02d.html", number);
            Files.write(Paths.get(filename), result.getBytes());
        }
    }

    private String getXmlString(String string) throws IOException {
        return "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n"+Files.readString(Paths.get(string));
    }
}
