package health.ere.ps.service.fhir;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response.Status;

import org.hl7.fhir.r4.model.Bundle;

import ca.uhn.fhir.context.FhirContext;

public class XmlPrescriptionProcessor {
    static FhirContext fhirContext = FhirContext.forR4();

    // Get <Bundle> tag including content
    private static Pattern p = Pattern.compile("(<Bundle[^>]*>.*?</Bundle>)", Pattern.DOTALL);

    public static Bundle[] parseFromString(String xml) {
        List<Bundle> bundles = new ArrayList<>();
        Matcher m = p.matcher(xml);
        boolean found = false;
        while(m.find()) {
            found = true;
            String bundleXml = m.group(1);
            Bundle bundle = fhirContext.newXmlParser().parseResource(Bundle.class, bundleXml);
            bundles.add(bundle);
        }
        if (!found) {
            throw new WebApplicationException("Could not extract inner text", Status.NOT_ACCEPTABLE);
        }
        return bundles.toArray(new Bundle[] {});
        
    }
}
