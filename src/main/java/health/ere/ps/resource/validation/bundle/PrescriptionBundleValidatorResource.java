package health.ere.ps.resource.validation.bundle;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import de.gematik.refv.commons.validation.ValidationResult;
import health.ere.ps.validation.fhir.bundle.PrescriptionBundleValidator;
import jakarta.inject.Inject;
import jakarta.json.JsonObject;
import jakarta.json.JsonObjectBuilder;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.Response.Status;

@Path("/validate")
public class PrescriptionBundleValidatorResource {

    @Inject
    PrescriptionBundleValidator prescriptionBundleValidator;

    @POST
    @Consumes("application/json")
    public Response post(JsonObject bundle) {
        JsonObjectBuilder builder = prescriptionBundleValidator.validateBundle(bundle);
        return Response.ok(builder.build()).build();
    }

    @POST
    @Consumes("application/xml")
    public Response post(String bundle) {
        List<String> errorsList = new ArrayList<>();

        ValidationResult validationResult = prescriptionBundleValidator.validateResource(bundle, true, errorsList);
        if (!validationResult.isValid()) {
            return Response.status(Status.BAD_REQUEST).entity(getXmlForErrorsList(errorsList)).build();
        } else {
            return Response.ok().build();
        }
    }

    private String getXmlForErrorsList(List<String> errorsList) {
        return "<errors>\n    <error>"+errorsList.stream().collect(Collectors.joining("</error>\n    <error>"))+"</error>\n</errors>";
    }
}
