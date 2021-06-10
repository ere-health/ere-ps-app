package health.ere.ps.service.dgc;

import health.ere.ps.event.RequestBearerTokenFromIdpEvent;
import health.ere.ps.model.dgc.CertificateRequest;
import health.ere.ps.model.dgc.PersonName;
import health.ere.ps.model.dgc.RecoveryCertificateRequest;
import health.ere.ps.model.dgc.RecoveryEntry;
import health.ere.ps.model.dgc.V;
import health.ere.ps.model.dgc.VaccinationCertificateRequest;
import org.eclipse.microprofile.config.inject.ConfigProperty;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Event;
import javax.inject.Inject;
import javax.validation.constraints.NotNull;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.Response;
import java.io.InputStream;
import java.time.LocalDate;
import java.util.Collections;
import java.util.Objects;
import java.util.Optional;
import java.util.logging.Logger;

@ApplicationScoped
public class DigitalGreenCertificateService {
    private static Logger log = Logger.getLogger(DigitalGreenCertificateService.class.getName());

    @ConfigProperty(name = "digital-green-certificate-service.issuerAPIUrl", defaultValue = "")
    String issuerAPIUrl;

    Client client;

    @Inject
    Event<RequestBearerTokenFromIdpEvent> requestBearerTokenFromIdp;

    @PostConstruct
    public void init() {
        ClientBuilder clientBuilder = ClientBuilder.newBuilder();
        client = clientBuilder.build();
    }

    @PreDestroy
    public void destroy() {
        if (client != null) {
            client.close();
        }
    }

    /**
     * Issue a certificate based on the given values.
     *
     * @param fn  (family) name
     * @param gn  given name
     * @param dob date of birth
     * @param id administering instance id
     * @param tg illness
     * @param vp vaccine
     * @param mp product
     * @param ma manufacturer
     * @param dn dose number
     * @param sd total dose count
     * @param dt vaccination date
     * @return bytes of certificate pdf
     */
    public byte[] issueVaccinationCertificatePdf(String fn, String gn, LocalDate dob,
                                                 String id, String tg, String vp, String mp, String ma, Integer dn,
                                                 Integer sd, String dt) {

        VaccinationCertificateRequest vaccinationCertificateRequest = new VaccinationCertificateRequest();

        vaccinationCertificateRequest.setDob(dob);
        vaccinationCertificateRequest.setNam(new PersonName(fn, gn));

        V v = new V();

        v.id = id;
        v.tg = tg;
        v.vp = vp;
        v.mp = mp;
        v.ma = ma;
        v.dn = dn;
        v.sd = sd;
        v.dt = dt;

        vaccinationCertificateRequest.v = Collections.singletonList(v);

        return issuePdf(vaccinationCertificateRequest);
    }

    /**
     * Create a recovery certificate pdf.
     *
     * @param fn  (family) name
     * @param gn  given name
     * @param dob date of birth
     * @param id  administering instance id
     * @param tg  illness
     * @param fr  date of test result, that has been positive
     * @param is  issuer of certificate
     * @param df  certificate validity date beginning
     * @param du  certificate validity date ending
     * @return bytes of certificate pdf
     */
    public byte[] issueRecoveryCertificatePdf(String fn, String gn, LocalDate dob, String id, String tg, LocalDate fr,
                                              String is, LocalDate df, LocalDate du) {

        RecoveryCertificateRequest recoveryCertificateRequest = new RecoveryCertificateRequest();

        recoveryCertificateRequest.setNam(new PersonName(fn, gn));
        recoveryCertificateRequest.setDob(dob);

        RecoveryEntry r = new RecoveryEntry();

        r.setId(id);
        r.setTg(tg);
        r.setFr(fr);
        r.setIs(is);
        r.setDf(df);
        r.setDu(du);

        recoveryCertificateRequest.setR(Collections.singletonList(r));

        return issuePdf(recoveryCertificateRequest);
    }

    /**
     * Request the certificate at the certificate backend enriched with the token for access.
     *
     * @param requestData       the data send to the backend, only allowed are: VaccinationCertificateRequest,
     *                          RecoveryCertificateRequest and TestCertificateRequest. Must be not null.
     * @return the serialized response.
     */
    public byte[] issuePdf(@NotNull CertificateRequest requestData) {
        Objects.requireNonNull(requestData); // can removed, if a validator is running.
        Response response = client.target(issuerAPIUrl)
                .request("application/pdf")
                .header("Authorization", "Bearer " + getToken())
                .post(Entity.entity(requestData, "application/vnd.dgc.v1+json"));

        byte[] responseData;

        try {
            responseData = response.readEntity(InputStream.class).readAllBytes();
            if (Response.Status.Family.familyOf(response.getStatus()) != Response.Status.Family.SUCCESSFUL) {
                throw new RuntimeException(new String(responseData));
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return responseData;
    }

    private String getToken() {
        RequestBearerTokenFromIdpEvent event = new RequestBearerTokenFromIdpEvent();

        requestBearerTokenFromIdp.fire(event);

        return Optional.ofNullable(event.getBearerToken()).orElseThrow(IllegalArgumentException::new);
    }
}
