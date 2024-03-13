package health.ere.ps.resource.gematik.writer;

import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;

import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.ext.MessageBodyWriter;
import javax.ws.rs.ext.Provider;

import org.hl7.fhir.r4.model.Bundle;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.parser.IParser;

import health.ere.ps.service.fhir.FHIRService;

@Provider
@Produces(MediaType.APPLICATION_XML)
public class XMLBundleMessageBodyWriter implements MessageBodyWriter<Bundle> {

    private static final FhirContext fhirContext = FHIRService.getFhirContext();
    static IParser xmlParser = fhirContext.newXmlParser();

    @Override
    public boolean isWriteable(Class<?> type, Type genericType, Annotation[] annotations, MediaType mediaType) {
        return type.isAssignableFrom(Bundle.class) && mediaType.isCompatible(MediaType.APPLICATION_XML_TYPE); 
    }

    @Override
    public void writeTo(Bundle t, Class<?> type, Type genericType, Annotation[] annotations, MediaType mediaType,
            MultivaluedMap<String, Object> httpHeaders, OutputStream entityStream)
            throws IOException, WebApplicationException {
        xmlParser.encodeResourceToWriter(t, new OutputStreamWriter(entityStream, "UTF-8"));
    }
}
