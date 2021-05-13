package health.ere.ps.model.idp.client;

import org.apache.http.HttpStatus;
import java.util.List;

import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;

import health.ere.ps.model.idp.client.data.BiometrieData;
import health.ere.ps.model.idp.client.token.JsonWebToken;
import kong.unirest.GenericType;
import kong.unirest.HttpResponse;
import kong.unirest.Unirest;

public class BiometrieClient {
    private static final String USER_AGENT = "IdP-Client";
    private static final String BEARER = "Bearer ";
    private String serverUrl;
    private JsonWebToken accessToken;

    public boolean insertPairing(final BiometrieData biometrieData) {
        final HttpResponse<String> response = Unirest.put(getServerUrl())
            .field("encrypted_registration_data", biometrieData)
            .header(HttpHeaders.AUTHORIZATION, BEARER + getAccessToken().getRawString())
            .header(HttpHeaders.USER_AGENT, USER_AGENT)
            .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_FORM_URLENCODED)
            .asString();
        return response.getStatus() == HttpStatus.SC_OK;
    }

    public List<BiometrieData> getAllPairingsForKvnr(final String kvnr) {
        final HttpResponse<List<BiometrieData>> response = Unirest
            .get(getServerUrl() + "/" + kvnr)
            .header(HttpHeaders.AUTHORIZATION, BEARER + getAccessToken().getRawString())
            .header(HttpHeaders.USER_AGENT, USER_AGENT)
            .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON)
            .asObject(new GenericType<>() {
            });

        if (response.getStatus() != HttpStatus.SC_OK) {
            throw new IdpClientRuntimeException(
                "Unexpected Server-Response " + response.getStatus());
        }

        return response.getBody();
    }

    public boolean deleteAllPairingsForKvnr(final String kvnr) {
        final HttpResponse<String> response = Unirest.delete(getServerUrl() + "/" + kvnr)
            .header(HttpHeaders.AUTHORIZATION, BEARER + getAccessToken().getRawString())
            .header(HttpHeaders.USER_AGENT, USER_AGENT)
            .header(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON)
            .asString();
        return response.getStatus() == HttpStatus.SC_OK;
    }

    public String getServerUrl() {
        return serverUrl;
    }

    public void setServerUrl(String serverUrl) {
        this.serverUrl = serverUrl;
    }

    public JsonWebToken getAccessToken() {
        return accessToken;
    }

    public void setAccessToken(JsonWebToken accessToken) {
        this.accessToken = accessToken;
    }

    public static class BiometrieClientBuilder {
        private BiometrieClient biometrieClient;

        public BiometrieClientBuilder() {
            biometrieClient = new BiometrieClient();
        }

        public BiometrieClientBuilder serverUrl(String serverUrl) {
            biometrieClient.setServerUrl(serverUrl);

            return this;
        }

        public BiometrieClientBuilder accessToken(JsonWebToken accessToken) {
            biometrieClient.setAccessToken(accessToken);

            return this;
        }

        public BiometrieClient build() {
            return biometrieClient;
        }
    }
}

