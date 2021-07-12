package health.ere.ps.validation.fhir.context.support;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Paths;

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
                                sd.setUrl(sd.getUrl()+"|"+sd.getVersion());
                                addStructureDefinition(o);
                        }
                });
        } catch (IOException | URISyntaxException e) {
                throw new RuntimeException(e);  
        }
    }
}
