package health.ere.ps.resource.dgc;

import health.ere.ps.model.dgc.VaccinationCertificateRequest;
import health.ere.ps.service.dgc.DigitalGreenCertificateService;
import health.ere.ps.model.dgc.RecoveryCertificateRequest;

import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Response;
import java.time.LocalDate;
import java.util.logging.Logger;

@Path("/api/certify/v2")
public class DigitalGreenCertificateResource {
    private static Logger log = Logger.getLogger(DigitalGreenCertificateResource.class.getName());

    @Inject
    DigitalGreenCertificateService digitalGreenCertificateService;

    @Path("/issue")
    @POST
    public Response issue(VaccinationCertificateRequest vaccinationCertificateRequest) {
        return okPdf(digitalGreenCertificateService.issuePdf(vaccinationCertificateRequest));
    }

    @Path("/issue")
    @GET
    public Response issue(@QueryParam("fn") String fn, @QueryParam("gn") String gn, @QueryParam("dob") LocalDate dob,
                          @QueryParam("id") String id, @QueryParam("tg") String tg, @QueryParam("vp") String vp,
                          @QueryParam("mp") String mp, @QueryParam("ma") String ma, @QueryParam("dn") Integer dn,
                          @QueryParam("sd") Integer sd, @QueryParam("dt") String dt) {

        return okPdf(digitalGreenCertificateService.issueVaccinationCertificatePdf(fn, gn, dob, id, tg, vp, mp, ma,
                dn, sd, dt));
    }

    @Path("/recovered")
    @POST
    public Response recovered(RecoveryCertificateRequest recoveryCertificateRequest) {
        return okPdf(digitalGreenCertificateService.issuePdf(recoveryCertificateRequest));
    }

    @Path("/recovered")
    @GET
    public Response recovered(@QueryParam("fn") String fn, @QueryParam("gn") String gn,
                              @QueryParam("dob") LocalDate dob, @QueryParam("id") String id,
                              @QueryParam("tg") String tg, @QueryParam("fr") LocalDate fr, @QueryParam("is") String is,
                              @QueryParam("df") LocalDate df, @QueryParam("du") LocalDate du) {

        return okPdf(digitalGreenCertificateService.issueRecoveryCertificatePdf(fn, gn, dob, id, tg, fr, is, df, du));
    }

    private static Response okPdf(byte[] bytes) {
        return Response.ok(bytes, "application/pdf").build();
    }

}
