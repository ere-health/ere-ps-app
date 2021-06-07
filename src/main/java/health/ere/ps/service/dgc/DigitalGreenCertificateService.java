package health.ere.ps.service.dgc;

import java.io.InputStream;
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

import health.ere.ps.model.dgc.CertificateRequest;
import org.eclipse.microprofile.config.inject.ConfigProperty;

import health.ere.ps.event.RequestBearerTokenFromIdpEvent;

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
    
    public byte[] issue(CertificateRequest request) {
        Response response = client.target(issuerAPIUrl)
                .request("application/pdf")
                .header("Authorization", "Bearer " + getToken())
                .post(Entity.json(request));

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

    private String getToken() {
        RequestBearerTokenFromIdpEvent event = new RequestBearerTokenFromIdpEvent();

        requestBearerTokenFromIdp.fire(event);

        return Optional.ofNullable(event.getBearerToken()).orElseThrow(IllegalArgumentException::new);
    }
}
