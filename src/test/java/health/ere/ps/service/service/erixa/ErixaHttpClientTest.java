package health.ere.ps.service.erixa;

import org.apache.http.HttpHeaders;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.entity.ContentType;
import org.apache.http.impl.client.CloseableHttpClient;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import health.ere.ps.config.UserConfig;
import health.ere.ps.service.erixa.ErixaHttpClient;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.*;

public class ErixaHttpClientTest {

    private ErixaHttpClient erixaHttpClient;
    private CloseableHttpClient httpClient;

    @Test
    public void testSendPostRequest() throws IOException {
        String url = "https://example.com/api";
        String json = "{\"key\":\"value\"}";

        CloseableHttpResponse mockResponse = (CloseableHttpResponse) mock(HttpResponse.class);
        when(httpClient.execute(any())).thenReturn(mockResponse);

        HttpResponse response = erixaHttpClient.sendPostRequest(url, json);

        assertNotNull(response);
        assertEquals(mockResponse, response);

        verify(httpClient).execute(any());
    }

    @Test
    public void testSendGetRequest() throws IOException {
        String url = "https://example.com/api";

        HttpResponse mockResponse = mock(HttpResponse.class);
        when(httpClient.execute(any())).thenReturn((CloseableHttpResponse) mockResponse);

        HttpResponse response = erixaHttpClient.sendGetRequest(url);

        assertNotNull(response);
        assertEquals(mockResponse, response);

        verify(httpClient).execute(any());
    }

    @Test
    public void testGetBasicAuthenticationHeader() {
        erixaHttpClient.setUserConfig(new UserConfig());
        UserConfig userConfig = erixaHttpClient.getUserConfig();
        userConfig.setErixaUserEmail("test@example.com");
        userConfig.setErixaUserEmail("password");
    }

    @Test
    public void testGetApiKey() {
        erixaHttpClient.setUserConfig(new UserConfig());
        UserConfig userConfig = erixaHttpClient.getUserConfig();
        userConfig.setErixaApiKey("api-key");
    }
}
