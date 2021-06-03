package health.ere.ps.resource.dgc;

import java.util.logging.Logger;


import javax.inject.Inject;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.core.Response;

import health.ere.ps.model.dgc.VaccinationCertificateRequest;
import health.ere.ps.service.dgc.DigitalGreenCertificateService;

@Path("/api/certify/v2")
public class DigitalGreenCertificateResource {
    private static Logger log = Logger.getLogger(DigitalGreenCertificateResource.class.getName());

    @Inject
    DigitalGreenCertificateService digitalGreenCertificateService;

    @Path("/issue")
    @POST
    public Response issue(VaccinationCertificateRequest vaccinationCertificateRequest) {
        return Response.ok(digitalGreenCertificateService.issue(vaccinationCertificateRequest), "application/pdf").build();
    }
}
