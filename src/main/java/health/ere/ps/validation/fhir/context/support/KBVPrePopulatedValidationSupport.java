package health.ere.ps.validation.fhir.context.support;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

import org.hl7.fhir.common.hapi.validation.support.PrePopulatedValidationSupport;
import org.hl7.fhir.r4.model.CodeSystem;
import org.hl7.fhir.r4.model.StructureDefinition;
import org.hl7.fhir.r4.model.ValueSet;
import org.jboss.logging.Logger;

import ca.uhn.fhir.context.ConfigurationException;
import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.parser.DataFormatException;
import ca.uhn.fhir.parser.IParser;

public class KBVPrePopulatedValidationSupport extends PrePopulatedValidationSupport {
    
    Logger logger = Logger.getLogger(KBVPrePopulatedValidationSupport.class);
    IParser xmlParser = FhirContext.forR4().newXmlParser();

    static Map<String,String> bundleUrl2bundleCanonicalUrl = new HashMap<>();

    static {
        bundleUrl2bundleCanonicalUrl.put("https://fhir.kbv.de/StructureDefinition/KBV_PR_ERP_Composition", "https://fhir.kbv.de/StructureDefinition/KBV_PR_ERP_Composition|1.0.1");
        bundleUrl2bundleCanonicalUrl.put("https://fhir.kbv.de/StructureDefinition/KBV_PR_ERP_Medication_Compounding", "https://fhir.kbv.de/StructureDefinition/KBV_PR_ERP_Medication_Compounding|1.0.1");
        bundleUrl2bundleCanonicalUrl.put("https://fhir.kbv.de/StructureDefinition/KBV_PR_ERP_Medication_PZN", "https://fhir.kbv.de/StructureDefinition/KBV_PR_ERP_Medication_PZN|1.0.1");
        bundleUrl2bundleCanonicalUrl.put("https://fhir.kbv.de/StructureDefinition/KBV_PR_ERP_Medication_FreeText", "https://fhir.kbv.de/StructureDefinition/KBV_PR_ERP_Medication_FreeText|1.0.1");
        bundleUrl2bundleCanonicalUrl.put("https://fhir.kbv.de/StructureDefinition/KBV_PR_ERP_Bundle", "https://fhir.kbv.de/StructureDefinition/KBV_PR_ERP_Bundle|1.0.1");
        bundleUrl2bundleCanonicalUrl.put("https://fhir.kbv.de/StructureDefinition/KBV_PR_ERP_PracticeSupply", "https://fhir.kbv.de/StructureDefinition/KBV_PR_ERP_PracticeSupply|1.0.1");
        bundleUrl2bundleCanonicalUrl.put("https://fhir.kbv.de/StructureDefinition/KBV_PR_ERP_Medication_Ingredient", "https://fhir.kbv.de/StructureDefinition/KBV_PR_ERP_Medication_Ingredient|1.0.1");
        bundleUrl2bundleCanonicalUrl.put("https://fhir.kbv.de/StructureDefinition/KBV_PR_ERP_Prescription", "https://fhir.kbv.de/StructureDefinition/KBV_PR_ERP_Prescription|1.0.1");
        bundleUrl2bundleCanonicalUrl.put("https://fhir.kbv.de/StructureDefinition/KBV_PR_FOR_Organization", "hhttps://fhir.kbv.de/StructureDefinition/KBV_PR_FOR_Organization|1.0.3");
        bundleUrl2bundleCanonicalUrl.put("https://fhir.kbv.de/StructureDefinition/KBV_PR_FOR_PractitionerRole","https://fhir.kbv.de/StructureDefinition/KBV_PR_FOR_PractitionerRole|1.0.3");
        bundleUrl2bundleCanonicalUrl.put("https://fhir.kbv.de/StructureDefinition/KBV_PR_FOR_Patient", "https://fhir.kbv.de/StructureDefinition/KBV_PR_FOR_Patient|1.0.3");
        bundleUrl2bundleCanonicalUrl.put("https://fhir.kbv.de/StructureDefinition/KBV_PR_FOR_Practitioner", "https://fhir.kbv.de/StructureDefinition/KBV_PR_FOR_Practitioner|1.0.3");
        bundleUrl2bundleCanonicalUrl.put("https://fhir.kbv.de/StructureDefinition/KBV_PR_FOR_Organization", "https://fhir.kbv.de/StructureDefinition/KBV_PR_FOR_Organization|1.0.3");
        bundleUrl2bundleCanonicalUrl.put("https://fhir.kbv.de/StructureDefinition/KBV_PR_FOR_Coverage", "https://fhir.kbv.de/StructureDefinition/KBV_PR_FOR_Coverage|1.0.3");
        // bundleUrl2bundleCanonicalUrl.put("https://fhir.kbv.de/StructureDefinition/KBV_PR_FOR_Coverage", "https://fhir.kbv.de/StructureDefinition/KBV_PR_FOR_Coverage|1.0.1");
        bundleUrl2bundleCanonicalUrl.put("https://fhir.kbv.de/StructureDefinition/KBV_PR_Base_Patient", "https://fhir.kbv.de/StructureDefinition/KBV_PR_Base_Patient|1.1.3");
        bundleUrl2bundleCanonicalUrl.put("https://fhir.kbv.de/StructureDefinition/KBV_PR_Base_Practitioner", "https://fhir.kbv.de/StructureDefinition/KBV_PR_Base_Practitioner|1.1.3");
        bundleUrl2bundleCanonicalUrl.put("https://fhir.kbv.de/StructureDefinition/KBV_PR_Base_Organization", "https://fhir.kbv.de/StructureDefinition/KBV_PR_Base_Organization|1.1.3");
        bundleUrl2bundleCanonicalUrl.put("http://fhir.de/StructureDefinition/identifier-iknr", "http://fhir.de/StructureDefinition/identifier-iknr|0.9.13");
        bundleUrl2bundleCanonicalUrl.put("http://fhir.de/StructureDefinition/identifier-bsnr", "http://fhir.de/StructureDefinition/identifier-bsnr|0.9.13");
        bundleUrl2bundleCanonicalUrl.put("http://fhir.de/StructureDefinition/identifier-vknr", "http://fhir.de/StructureDefinition/identifier-vknr|0.9.13");
        bundleUrl2bundleCanonicalUrl.put("http://fhir.de/StructureDefinition/identifier-kzva", "http://fhir.de/StructureDefinition/identifier-kzva|0.9.13");
        bundleUrl2bundleCanonicalUrl.put("http://fhir.de/StructureDefinition/identifier-lanr", "http://fhir.de/StructureDefinition/identifier-lanr|0.9.13");
        bundleUrl2bundleCanonicalUrl.put("http://fhir.de/StructureDefinition/identifier-efn", "http://fhir.de/StructureDefinition/identifier-efn|0.9.13");
        bundleUrl2bundleCanonicalUrl.put("http://fhir.de/StructureDefinition/identifier-zanr", "http://fhir.de/StructureDefinition/identifier-zanr|0.9.13");
        bundleUrl2bundleCanonicalUrl.put("http://fhir.de/StructureDefinition/identifier-pid", "http://fhir.de/StructureDefinition/identifier-pid|0.9.13");
        bundleUrl2bundleCanonicalUrl.put("http://fhir.de/StructureDefinition/identifier-kvid-10", "http://fhir.de/StructureDefinition/identifier-kvid-10|0.9.13");
        bundleUrl2bundleCanonicalUrl.put("http://fhir.de/StructureDefinition/identifier-pkv", "http://fhir.de/StructureDefinition/identifier-pkv|0.9.13");
        bundleUrl2bundleCanonicalUrl.put("http://fhir.de/StructureDefinition/address-de-basis", "http://fhir.de/StructureDefinition/address-de-basis|0.9.13");
        bundleUrl2bundleCanonicalUrl.put("http://fhir.de/StructureDefinition/humanname-de-basis", "http://fhir.de/StructureDefinition/humanname-de-basis|0.9.13");


    }

    public KBVPrePopulatedValidationSupport(FhirContext theContext) {
        super(theContext);
        try {
                Files.walk(Paths.get(getClass().getResource("/kbv").toURI()))
                .filter(Files::isRegularFile)
                .filter(f -> f.toString().endsWith(".xml"))
                .map(f -> {
                        try {
                                return xmlParser.parseResource(new FileInputStream(f.toFile()));
                        } catch (ConfigurationException | DataFormatException | FileNotFoundException e) {
                                logger.warn("Failed parsing: "+f.toString());
                                throw new RuntimeException(e);
                        }
                })
                .forEach(o -> {
                        logger.info("Adding "+o+" to Validator");
                        if(o instanceof CodeSystem) {
                                addCodeSystem(o);
                        } else if(o instanceof ValueSet) {
                                addValueSet((ValueSet)o);
                        } else if(o instanceof StructureDefinition) {
                                StructureDefinition sd = ((StructureDefinition)o);
                                if("https://fhir.kbv.de/StructureDefinition/KBV_PR_FOR_Coverage".equals(sd.getUrl()) && "1.0.1".equals(sd.getVersion())) { 
                                    sd.setUrl("https://fhir.kbv.de/StructureDefinition/KBV_PR_FOR_Coverage|1.0.1");
                                } else if(bundleUrl2bundleCanonicalUrl.containsKey(sd.getUrl())) {
                                    sd.setUrl(bundleUrl2bundleCanonicalUrl.get(sd.getUrl()));
                                }
                                addStructureDefinition(o);
                        }
                });
        } catch (IOException | URISyntaxException e) {
                throw new RuntimeException(e);  
        }
    }
}
