package health.ere.ps.service.erixa;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.Event;
import jakarta.inject.Inject;

import com.fasterxml.jackson.databind.ObjectMapper;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.util.EntityUtils;
import org.eclipse.microprofile.config.inject.ConfigProperty;

import health.ere.ps.model.erixa.api.mapping.UserDetails;


@ApplicationScoped
public class ErixaAPIInterface {

    @Inject
    ErixaHttpClient httpClient;

    @ConfigProperty(name = "erixa.api.url.user.details")
    String userUserDataURL;

    @ConfigProperty(name = "erixa.api.url.upload")
    String uploadToDrugstoreURL;

    @Inject
    Event<Exception> eventException;

    private final ObjectMapper objectMapper = new ObjectMapper();

    private final Logger log = Logger.getLogger(getClass().getName());


    public UserDetails getUserDetails() {
        try {
            HttpResponse response = httpClient.sendGetRequest(userUserDataURL);
            return parseUserDetails(response);
        } catch (Exception e) {
            e.printStackTrace();
            eventException.fireAsync(e);
            return null;
        }
    }

    public Object uploadToDrugstore(String json) {
        try {
            log.info("Post: "+uploadToDrugstoreURL+" "+json);
            HttpResponse response = httpClient.sendPostRequest(uploadToDrugstoreURL, json);
            if(response.getStatusLine().getStatusCode() != 200) {
                log.log(Level.WARNING, "Could not upload prescription to eRiXa: "+response.getStatusLine().getStatusCode()+" "+new String(response.getEntity().getContent().readAllBytes()));
            }
            return parseDrugstoreUploadResult(response);
        } catch (Exception e) {
            e.printStackTrace();
            eventException.fireAsync(e);
            return null;
        }
    }

    Object parseDrugstoreUploadResult(HttpResponse response) {
        // TODO implement method
        // throw new UnsupportedOperationException();
        return null;
    }

    UserDetails parseUserDetails(HttpResponse response) {
        HttpEntity entity = response.getEntity();
        String content;
        try {
            content = EntityUtils.toString(entity);
            return objectMapper.readValue(content, UserDetails.class);
        } catch (IOException e) {
            e.printStackTrace();
            eventException.fireAsync(e);
        }
        return null;
    }
}
