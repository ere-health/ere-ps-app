package health.ere.ps.service.erixa;

import health.ere.ps.model.erixa.api.mapping.UserDetails;
import health.ere.ps.service.erixa.ErixaAPIInterface;
import health.ere.ps.service.erixa.ErixaHttpClient;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.StatusLine;
import org.apache.http.entity.StringEntity;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.google.j2objc.annotations.ReflectionSupport.Level;

import javax.enterprise.event.Event;
import java.io.IOException;
import java.io.InputStream;
import java.util.logging.Logger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.*;

public class ErixaAPIInterfaceTest {

    @InjectMocks
    private ErixaAPIInterface erixaAPIInterface;

    @Mock
    private ErixaHttpClient httpClient;

    @Mock
    private Event<Exception> eventException;

    @Mock
    private Logger log;

    private HttpResponse response;

    @Test
    public void testGetUserDetails() throws IOException {
        // Prepare a mocked response from httpClient
        HttpResponse response = mock(HttpResponse.class);
        HttpEntity entity = mock(HttpEntity.class);
        String userDetailsJson = "{\"name\":\"John\",\"email\":\"john@example.com\"}";

        when(httpClient.sendGetRequest(anyString())).thenReturn(response);
        when(response.getEntity()).thenReturn(entity);
        when(entity.getContent()).thenReturn(mock(InputStream.class));
        when(entity.getContent().readAllBytes()).thenReturn(userDetailsJson.getBytes());

        UserDetails userDetails = erixaAPIInterface.getUserDetails();

        assertNotNull(userDetails);
        assertEquals("John", userDetails.getFirstName());
        assertEquals("john@example.com", userDetails.getEmailAddress());

        // Verify that the eventException was not fired
        verify(eventException, never()).fireAsync(any(Exception.class));
    }

    @Test
    public void testGetUserDetailsException() throws IOException {
        // Simulate an exception when making the HTTP request
        when(httpClient.sendGetRequest(anyString())).thenThrow(new IOException("Network error"));

        UserDetails userDetails = erixaAPIInterface.getUserDetails();

        assertNull(userDetails);

        // Verify that the eventException was fired with the expected exception
        verify(eventException).fireAsync(any(Exception.class));
    }

    @Test
    public void testUploadToDrugstore() throws IOException {
        // Prepare a mocked response from httpClient
        HttpResponse response = mock(HttpResponse.class);
        StatusLine statusLine = mock(StatusLine.class);

        when(httpClient.sendPostRequest(anyString(), anyString())).thenReturn(response);
        when(response.getStatusLine()).thenReturn(statusLine);
        when(statusLine.getStatusCode()).thenReturn(200);

        Object result = erixaAPIInterface.uploadToDrugstore("{}");

        assertNotNull(result);

        // Verify that the log was called with the expected message
        verify(log).info("Post: " + erixaAPIInterface.getUploadToDrugstoreURL() + " {}");

        // Verify that the eventException was not fired
        verify(eventException, never()).fireAsync(any(Exception.class));
    }

    /**
     * @throws IOException
     */
    @Test
    public void testUploadToDrugstoreNon200Status() throws IOException {
        // Prepare a mocked response from httpClient with a non-200 status code
        HttpResponse response = mock(HttpResponse.class);
        StatusLine statusLine = mock(StatusLine.class);

        when(httpClient.sendPostRequest(anyString(), anyString())).thenReturn(response);
        when(response.getStatusLine()).thenReturn(statusLine);
        when(statusLine.getStatusCode()).thenReturn(404);

        Object result = erixaAPIInterface.uploadToDrugstore("{}");

        assertNull(result);

        // Verify that the eventException was not fired
        verify(eventException, never()).fireAsync(any(Exception.class));
    }

    @Test
    public void testUploadToDrugstoreException() throws IOException {
        // Simulate an exception when making the HTTP request
        when(httpClient.sendPostRequest(anyString(), anyString())).thenThrow(new IOException("Network error"));

        Object result = erixaAPIInterface.uploadToDrugstore("{}");

        assertNull(result);

        // Verify that the eventException was fired with the expected exception
        verify(eventException).fireAsync(any(Exception.class));
    }

    @Test
    public void testParseUserDetails() throws IOException {
        // Prepare a mocked response entity with user details JSON
        HttpEntity entity = new StringEntity("{\"name\":\"Alice\",\"email\":\"alice@example.com\"}");

        HttpResponse response = mock(HttpResponse.class);
        when(response.getEntity()).thenReturn(entity);
    }

    @Test
    public void testParseUserDetailsException() throws IOException {
        // Simulate an exception when parsing the response entity
        HttpEntity entity = mock(HttpEntity.class);

        when(response.getEntity()).thenReturn(entity);
        when(entity.getContent()).thenThrow(new IOException("Parsing error"));

        // Verify that the eventException was fired with the expected exception
        verify(eventException).fireAsync(any(Exception.class));
    }
}
