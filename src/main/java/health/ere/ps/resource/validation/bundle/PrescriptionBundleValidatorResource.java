package health.ere.ps.resource.validation.bundle;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import javax.inject.Inject;
import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import health.ere.ps.validation.fhir.bundle.PrescriptionBundleValidator;

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

        if (!prescriptionBundleValidator.validateResource(bundle,
            true, errorsList).isSuccessful()) {
            return Response.status(Status.BAD_REQUEST).entity(getXmlForErrorsList(errorsList)).build();
        } else {
            return Response.ok().build();
        }
    }

    private String getXmlForErrorsList(List<String> errorsList) {
        return "<errors>\n    <error>"+errorsList.stream().collect(Collectors.joining("</error>\n    <error>"))+"</error>\n</errors>";
    }
}
