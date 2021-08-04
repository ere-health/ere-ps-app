package health.ere.ps.resource.validation.bundle;

import javax.inject.Inject;
import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.core.Response;

import health.ere.ps.validation.fhir.bundle.PrescriptionBundleValidator;

@Path("/validate")
public class PrescriptionBundleValidatorResource {

    @Inject
    PrescriptionBundleValidator prescriptionBundleValidator;

    @POST
    public Response post(JsonObject bundle) {
        JsonObjectBuilder builder = prescriptionBundleValidator.validateBundle(bundle);
        return Response.ok(builder.build()).build();
    }
}
