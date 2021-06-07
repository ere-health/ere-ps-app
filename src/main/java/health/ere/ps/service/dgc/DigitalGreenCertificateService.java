package health.ere.ps.service.dgc;

import java.io.InputStream;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.logging.Logger;

import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Event;
import javax.inject.Inject;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.Response;

import com.google.common.base.Strings;
import health.ere.ps.model.dgc.Nam;
import health.ere.ps.model.dgc.V;
import org.eclipse.microprofile.config.inject.ConfigProperty;

import health.ere.ps.event.RequestBearerTokenFromIdpEvent;
import health.ere.ps.model.dgc.VaccinationCertificateRequest;

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

    public byte[] issue(VaccinationCertificateRequest vaccinationCertificateRequest) {
        Response response = client.target(issuerAPIUrl)
                .request("application/pdf")
                .header("Authorization", "Bearer " + getToken())
                .post(Entity.json(vaccinationCertificateRequest));

        byte[] pdf;

        try {
            pdf = response.readEntity(InputStream.class).readAllBytes();
            if (Response.Status.Family.familyOf(response.getStatus()) != Response.Status.Family.SUCCESSFUL) {
                throw new RuntimeException(new String(pdf));
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return pdf;
    }

    /**
     * Issue a certificate based on the given values.
     *
     * @param fn  full name
     * @param gn  given name
     * @param dob date of birth
     * @param id1 administering instance id 1
     * @param tg1 illness 1
     * @param vp1 vaccine 1
     * @param mp1 product 1
     * @param ma1 manufacturer 1
     * @param dn1 dose number 1
     * @param sd1 total dose count 1
     * @param dt1 vaccination date 1
     * @param id2 administering instance id 2; the second vaccination will be added to the certificate iff this value is non-empty
     * @param tg2 illness 2
     * @param vp2 vaccine 2
     * @param mp2 product 2
     * @param ma2 manufacturer 2
     * @param dn2 dose number 2
     * @param sd2 total dose count 2
     * @param dt2 vaccination date 2
     * @return bytes of certificate pdf
     */
    public byte[] issue(String fn, String gn, String dob,
                        String id1, String tg1, String vp1, String mp1, String ma1, Integer dn1, Integer sd1,
                        String dt1,
                        String id2, String tg2, String vp2, String mp2, String ma2, Integer dn2, Integer sd2,
                        String dt2) {

        VaccinationCertificateRequest vaccinationCertificateRequest = new VaccinationCertificateRequest();

        vaccinationCertificateRequest.dob = dob;

        Nam nam = new Nam();

        nam.gn = gn;
        nam.fn = fn;

        vaccinationCertificateRequest.nam = nam;

        V v1 = createV(id1, tg1, vp1, mp1, ma1, dn1, sd1, dt1);

        if (Strings.isNullOrEmpty(id2)) {
            vaccinationCertificateRequest.v = Collections.singletonList(v1);
        } else {
            vaccinationCertificateRequest.v = List.of(v1, createV(id2, tg2, vp2, mp2, ma2, dn2, sd2, dt2));
        }

        return issue(vaccinationCertificateRequest);
    }

    private String getToken() {
        RequestBearerTokenFromIdpEvent event = new RequestBearerTokenFromIdpEvent();

        requestBearerTokenFromIdp.fire(event);

        return Optional.ofNullable(event.getBearerToken()).orElseThrow(IllegalArgumentException::new);
    }

    private static V createV(String id, String tg, String vp, String mp, String ma, Integer dn, Integer sd, String dt) {
        V v = new V();

        v.id = id;
        v.tg = tg;
        v.vp = vp;
        v.mp = mp;
        v.ma = ma;
        v.dn = dn;
        v.sd = sd;
        v.dt = dt;

        return v;
    }
}
