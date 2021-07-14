package health.ere.ps.service.erixa;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import health.ere.ps.model.erixa.api.mapping.UserDetails;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.util.EntityUtils;
import org.eclipse.microprofile.config.inject.ConfigProperty;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import java.io.IOException;
import java.util.logging.Logger;


@ApplicationScoped
public class ErixaAPIInterface {

    @Inject
    ErixaHttpClient httpClient;

    @ConfigProperty(name = "erixa.api.url.user.details")
    String userUserDataURL;

    @ConfigProperty(name = "erixa.api.url.upload")
    String uploadToDrugstoreURL;

    private final ObjectMapper objectMapper = new ObjectMapper();

    private final Logger log = Logger.getLogger(getClass().getName());


    public UserDetails getUserDetails() {
        try {
            HttpResponse response = httpClient.sendGetRequest(userUserDataURL);
            return parseUserDetails(response);
        } catch (IOException e) {
            return null;
        }
    }

    public Object uploadToDrugstore(String json) {
        try {
            HttpResponse response = httpClient.sendPostRequest(uploadToDrugstoreURL, json);
            return parseDrugstoreUploadResult(response);
        } catch (IOException e) {
            return null;
        }
    }

    Object parseDrugstoreUploadResult(HttpResponse response) {
        // TODO implement method
        throw new UnsupportedOperationException();
    }

    UserDetails parseUserDetails(HttpResponse response) {
        HttpEntity entity = response.getEntity();
        String content;
        try {
            content = EntityUtils.toString(entity);
            return objectMapper.readValue(content, UserDetails.class);
        } catch (JsonProcessingException e) {
            log.severe("Failed to parse json");
        } catch (IOException e) {
            log.severe("Error reading content");
        }
        return null;
    }
}
