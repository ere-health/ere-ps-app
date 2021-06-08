package health.ere.ps.resource.dgc;

import health.ere.ps.model.dgc.VaccinationCertificateRequest;
import health.ere.ps.service.dgc.DigitalGreenCertificateService;

import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Response;
import java.util.logging.Logger;

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

    @Path("/issue")
    @GET
    public Response issue(@QueryParam("fn") String fn, @QueryParam("gn") String gn, @QueryParam("dob") String dob,
                          @QueryParam("id1") String id1, @QueryParam("tg1") String tg1, @QueryParam("vp") String vp1,
                          @QueryParam("mp1") String mp1, @QueryParam("ma1") String ma1, @QueryParam("dn1") Integer dn1,
                          @QueryParam("sd") Integer sd1, @QueryParam("dt1") String dt1,
                          @QueryParam("id2") String id2, @QueryParam("tg2") String tg2, @QueryParam("vp2") String vp2,
                          @QueryParam("mp2") String mp2, @QueryParam("ma2") String ma2, @QueryParam("dn2") Integer dn2,
                          @QueryParam("sd2") Integer sd2, @QueryParam("dt2") String dt2) {

        return okPdf(digitalGreenCertificateService.issueVaccinationCertificate(fn, gn, dob, id1, tg1, vp1, mp1, ma1,
                dn1, sd1, dt1, id2, tg2, vp2, mp2, ma2, dn2, sd2, dt2));
    }

    private static Response okPdf(byte[] bytes) {
        return Response.ok(bytes, "application/pdf").build();
    }

}
