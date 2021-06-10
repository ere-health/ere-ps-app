package health.ere.ps.service.dgc;

import health.ere.ps.event.RequestBearerTokenFromIdpEvent;
import health.ere.ps.model.dgc.CertificateRequest;
import health.ere.ps.model.dgc.PersonName;
import health.ere.ps.model.dgc.V;
import health.ere.ps.model.dgc.VaccinationCertificateRequest;
import health.ere.ps.ssl.SSLUtilities;

import org.eclipse.microprofile.config.inject.ConfigProperty;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Event;
import javax.inject.Inject;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.Response;
import java.io.InputStream;
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
        SSLUtilities.trustAllHostnames();
        SSLUtilities.trustAllHttpsCertificates();
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
     * @param fn  full name
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
    public byte[] issueVaccinationCertificatePdf(String fn, String gn, String dob,
                                                 String id, String tg, String vp, String mp, String ma, Integer dn,
                                                 Integer sd, String dt) {

        VaccinationCertificateRequest vaccinationCertificateRequest = new VaccinationCertificateRequest();

        vaccinationCertificateRequest.dob = dob;

        PersonName nam = new PersonName();

        nam.gn = gn;
        nam.fn = fn;
        nam.fnt = standardize(fn);

        vaccinationCertificateRequest.nam = nam;

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

    private String standardize(String fn) {
        // TODO: implement me in a better way
        return fn.toUpperCase();
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
        Entity entity = Entity.entity(requestData, "application/vnd.dgc.v1+json");
        entity = Entity.entity("{\r\n  \"ver\": \"1.0.0\",\r\n  \"nam\": {\r\n    \"fn\": \"d'Ars\u00F8ns - van Halen\",\r\n    \"gn\": \"Fran\u00E7ois-Joan\",\r\n    \"fnt\": \"DARSONS<VAN<HALEN\",\r\n    \"gnt\": \"FRANCOIS<JOAN\"\r\n  },\r\n  \"dob\": \"2009-02-28\",\r\n  \"v\": [\r\n    {\r\n      \"id\": \"123456\",\r\n      \"tg\": \"840539006\",\r\n      \"vp\": \"1119349007\",\r\n      \"mp\": \"EU/1/20/1528\",\r\n      \"ma\": \"ORG-100030215\",\r\n      \"dn\": 2,\r\n      \"sd\": 2,\r\n      \"dt\": \"2021-04-21\",\r\n      \"co\": \"NL\",\r\n      \"is\": \"Ministry of Public Health, Welfare and Sport\",\r\n      \"ci\": \"urn:uvci:01:NL:PlA8UWS60Z4RZXVALl6GAZ\"\r\n    }\r\n  ]\r\n}", "application/vnd.dgc.v1+json");
        Response response = client.target(issuerAPIUrl)
                .path("/api/certify/v2/issue")
                .request("application/pdf")
                .header("Authorization", "Bearer " + getToken())
                .post(entity);

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
