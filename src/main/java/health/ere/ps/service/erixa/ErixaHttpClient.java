package health.ere.ps.service.erixa;


import health.ere.ps.model.erixa.api.credentials.BasicAuthCredentials;
import org.apache.commons.codec.binary.Base64;
import org.apache.http.HttpHeaders;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.HttpClientBuilder;

import javax.enterprise.context.ApplicationScoped;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.logging.Logger;

@ApplicationScoped
public class ErixaHttpClient {

    private final Logger log = Logger.getLogger(getClass().getName());

    private final HttpClient httpClient = HttpClientBuilder.create().build();

    public HttpResponse sendPostRequest(String url, String json) throws IOException {

        StringEntity entity = new StringEntity(json);

        HttpPost request = new HttpPost(url.toString());

        request.setHeader(HttpHeaders.AUTHORIZATION, getBasicAuthenticationHeader());
        request.setHeader(HttpHeaders.CONTENT_TYPE, "application/json");
        request.setHeader("ApiKey", getApiKey());
        request.setEntity(entity);

        return httpClient.execute(request);
    }

    public HttpResponse sendGetRequest(String url) throws IOException {

        HttpGet request = new HttpGet(url.toString());

        request.setHeader(HttpHeaders.AUTHORIZATION, getBasicAuthenticationHeader());
        request.setHeader(HttpHeaders.CONTENT_TYPE, "application/json");
        request.setHeader("ApiKey", getApiKey());

        return httpClient.execute(request);
    }

    private String getBasicAuthenticationHeader() {
        BasicAuthCredentials credentials = getErixaCredentials();
        String auth = credentials.getEmail() + ":" + credentials.getPassword();
        byte[] encodedAuth = Base64.encodeBase64(auth.getBytes(StandardCharsets.ISO_8859_1));
        return "Basic " + new String(encodedAuth);
    }

    private BasicAuthCredentials getErixaCredentials() {
        // TODO get user's credentials
        return new BasicAuthCredentials("<User-Email-Address>", "<User-Password>");
    }

    private String getApiKey() {
        // TODO fetch from secret file
        return "<ERE-API-Key>";
    }
}
