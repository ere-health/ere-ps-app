package health.ere.ps.service.dgc;

import health.ere.ps.event.RequestBearerTokenFromIdpEvent;
import health.ere.ps.exception.dgc.DgcCertificateServiceAuthenticationException;
import health.ere.ps.exception.dgc.DgcCertificateServiceException;
import health.ere.ps.exception.dgc.DgcInternalAuthenticationException;
import health.ere.ps.exception.dgc.DgcInvalidParametersException;
import health.ere.ps.model.dgc.CertificateRequest;
import health.ere.ps.model.dgc.PersonName;
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
import java.io.IOException;
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

        switch (response.getStatus()) {
            case 200: {
                try {
                    return response.readEntity(InputStream.class).readAllBytes();
                } catch (IOException ioe) {
                    throw new DgcCertificateServiceException(100200, "Could not read response from certificate service");
                }
            }
            case 400:
            case 406: {
                throw new DgcInvalidParametersException(100000 + response.getStatus(), "Invalid parameters in request" +
                        " to certificate service");
            }
            case 401:
            case 403: {
                throw new DgcCertificateServiceAuthenticationException(100000 + response.getStatus(), "Credentials " +
                        "were not accepted by certificate service");
            }
            case 500: {
                throw new DgcCertificateServiceException(100500, "Internal server error in certificate service");
            }
            default: {
                throw new DgcCertificateServiceException(100000 + response.getStatus(), "Unspecified response from " +
                        "certificate service");
            }
        }
    }

    private String getToken() {
        RequestBearerTokenFromIdpEvent event = new RequestBearerTokenFromIdpEvent();

        requestBearerTokenFromIdp.fire(event);

        return Optional.ofNullable(event.getBearerToken()).orElseThrow(DgcInternalAuthenticationException::new);
    }
}
