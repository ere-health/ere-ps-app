package health.ere.ps.service.dgc;

import java.io.IOException;
import java.io.InputStream;
import java.util.logging.Logger;

import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Event;
import javax.inject.Inject;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.Response;

import org.eclipse.microprofile.config.inject.ConfigProperty;

import health.ere.ps.event.RequestBearerTokenFromIdpEvent;
import health.ere.ps.model.dgc.VaccinationCertificateRequest;

@ApplicationScoped
public class DigitalGreenCertificateService {
    private static Logger log = Logger.getLogger(DigitalGreenCertificateService.class.getName());

    @ConfigProperty(name = "digital-green-certificate-service.issuerAPIUrl", defaultValue = "")
    String issuerAPIUrl;

    Client client;

    String bearerToken;

    @Inject
    Event<RequestBearerTokenFromIdpEvent> requestBearerTokenFromIdp;

    @PostConstruct
    public void init() {
        ClientBuilder clientBuilder = ClientBuilder.newBuilder();
        client = clientBuilder.build();
        
        RequestBearerTokenFromIdpEvent event = new RequestBearerTokenFromIdpEvent();
        requestBearerTokenFromIdp.fire(event);
        bearerToken = event.getBearerToken();
    }
    
    public byte[] issue(VaccinationCertificateRequest vaccinationCertificateRequest) {
        Response response = client.target(issuerAPIUrl).request().header("Authorization", "Bearer " + bearerToken).post(Entity.json(vaccinationCertificateRequest));

        byte[] pdf;
        try {
            pdf = response.readEntity(InputStream.class).readAllBytes();
            if (Response.Status.Family.familyOf(response.getStatus()) != Response.Status.Family.SUCCESSFUL) {
                throw new RuntimeException(new String(pdf));
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return pdf;
    }
}
