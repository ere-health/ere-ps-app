package health.ere.ps.resource.gematik.writer;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.parser.IParser;
import health.ere.ps.service.fhir.FHIRService;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.MultivaluedMap;
import jakarta.ws.rs.ext.MessageBodyWriter;
import jakarta.ws.rs.ext.Provider;
import org.hl7.fhir.r4.model.Bundle;

import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;

import static jakarta.ws.rs.core.MediaType.APPLICATION_XML;
import static jakarta.ws.rs.core.MediaType.APPLICATION_XML_TYPE;
import static java.nio.charset.StandardCharsets.UTF_8;

@Provider
@Produces(APPLICATION_XML)
public class XMLBundleMessageBodyWriter implements MessageBodyWriter<Bundle> {

    private static final FhirContext fhirContext = FHIRService.getFhirContext();
    static IParser xmlParser = fhirContext.newXmlParser();

    @Override
    public boolean isWriteable(Class<?> type, Type genericType, Annotation[] annotations, MediaType mediaType) {
        return type.isAssignableFrom(Bundle.class) && mediaType.isCompatible(APPLICATION_XML_TYPE);
    }

    @Override
    public void writeTo(
        Bundle t,
        Class<?> type,
        Type genericType,
        Annotation[] annotations,
        MediaType mediaType,
        MultivaluedMap<String, Object> httpHeaders,
        OutputStream entityStream
    ) throws IOException, WebApplicationException {
        xmlParser.encodeResourceToWriter(t, new OutputStreamWriter(entityStream, UTF_8));
    }
}
