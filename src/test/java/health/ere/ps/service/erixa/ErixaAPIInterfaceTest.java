package health.ere.ps.service.erixa;

import com.fasterxml.jackson.databind.ObjectMapper;
import health.ere.ps.model.erixa.api.mapping.UserDetails;
import jakarta.enterprise.event.Event;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.StatusLine;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ErixaAPIInterfaceTest {

    @Mock
    private ErixaHttpClient httpClient;

    @Mock
    private Event<Exception> eventException;

    @Mock
    private HttpResponse httpResponse;

    @Mock
    private StatusLine statusLine;

    @Mock
    private HttpEntity httpEntity;

    @InjectMocks
    private ErixaAPIInterface erixaAPIInterface;

    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
    }

    @Test
    void testGetUserDetails_Success() throws IOException {
        // Given
        UserDetails expectedUserDetails = createSampleUserDetails();
        String userDetailsJson = objectMapper.writeValueAsString(expectedUserDetails);

        when(httpResponse.getEntity()).thenReturn(httpEntity);
        when(httpEntity.getContent()).thenReturn(
                new java.io.ByteArrayInputStream(userDetailsJson.getBytes(StandardCharsets.UTF_8))
        );
        when(httpClient.sendGetRequest(any())).thenReturn(httpResponse);

        // When
        UserDetails result = erixaAPIInterface.getUserDetails();

        // Then
        assertNotNull(result);
        assertEquals(expectedUserDetails.getId(), result.getId());
        assertEquals(expectedUserDetails.getFirstName(), result.getFirstName());
        assertEquals(expectedUserDetails.getLastName(), result.getLastName());
        assertEquals(expectedUserDetails.getEmailAddress(), result.getEmailAddress());

        verify(httpClient).sendGetRequest(any());
        verifyNoInteractions(eventException);
    }

    @Test
    void testGetUserDetails_HttpClientException() throws IOException {
        // Given
        when(httpClient.sendGetRequest(any())).thenThrow(new IOException("Network error"));

        // When
        UserDetails result = erixaAPIInterface.getUserDetails();

        // Then
        assertNull(result);
        verify(eventException).fireAsync(any(IOException.class));
    }

    @Test
    void testGetUserDetails_ParsingException() throws IOException {
        // Given
        String invalidJson = "{ invalid json }";

        when(httpResponse.getEntity()).thenReturn(httpEntity);
        when(httpEntity.getContent()).thenReturn(
                new java.io.ByteArrayInputStream(invalidJson.getBytes(StandardCharsets.UTF_8))
        );
        when(httpClient.sendGetRequest(any())).thenReturn(httpResponse);

        // When
        UserDetails result = erixaAPIInterface.getUserDetails();

        // Then
        assertNull(result);
        verify(eventException).fireAsync(any(Exception.class));
    }

    @Test
    void testUploadToDrugstore_Success() throws IOException {
        // Given
        String prescriptionJson = "{\"prescription\": \"test data\"}";

        when(httpResponse.getStatusLine()).thenReturn(statusLine);
        when(statusLine.getStatusCode()).thenReturn(200);
        when(httpClient.sendPostRequest(any(), any())).thenReturn(httpResponse);

        // When
        Object result = erixaAPIInterface.uploadToDrugstore(prescriptionJson);

        // Then
        assertNull(result); // Currently returns null as parseDrugstoreUploadResult is not implemented
        verify(httpClient).sendPostRequest(any(), eq(prescriptionJson));
        verifyNoInteractions(eventException);
    }

    @Test
    void testUploadToDrugstore_Non200Response() throws IOException {
        // Given
        String prescriptionJson = "{\"prescription\": \"test data\"}";
        String errorResponse = "{\"error\": \"Upload failed\"}";

        when(httpResponse.getStatusLine()).thenReturn(statusLine);
        when(statusLine.getStatusCode()).thenReturn(400);
        when(httpResponse.getEntity()).thenReturn(httpEntity);
        when(httpEntity.getContent()).thenReturn(
                new java.io.ByteArrayInputStream(errorResponse.getBytes(StandardCharsets.UTF_8))
        );
        when(httpClient.sendPostRequest(any(), any())).thenReturn(httpResponse);

        // When
        Object result = erixaAPIInterface.uploadToDrugstore(prescriptionJson);

        // Then
        assertNull(result);
        verify(httpClient).sendPostRequest(any(), eq(prescriptionJson));
        verifyNoInteractions(eventException);
    }

    @Test
    void testUploadToDrugstore_HttpClientException() throws IOException {
        // Given
        String prescriptionJson = "{\"prescription\": \"test data\"}";
        when(httpClient.sendPostRequest(any(), any())).thenThrow(new IOException("Network error"));

        // When
        Object result = erixaAPIInterface.uploadToDrugstore(prescriptionJson);

        // Then
        assertNull(result);
        verify(eventException).fireAsync(any(IOException.class));
    }

    @Test
    void testParseUserDetails_Success() throws IOException {
        // Given
        UserDetails expectedUserDetails = createSampleUserDetails();
        String userDetailsJson = objectMapper.writeValueAsString(expectedUserDetails);

        when(httpResponse.getEntity()).thenReturn(httpEntity);
        when(httpEntity.getContent()).thenReturn(
                new java.io.ByteArrayInputStream(userDetailsJson.getBytes(StandardCharsets.UTF_8))
        );

        // When
        UserDetails result = erixaAPIInterface.parseUserDetails(httpResponse);

        // Then
        assertNotNull(result);
        assertEquals(expectedUserDetails.getId(), result.getId());
        assertEquals(expectedUserDetails.getFirstName(), result.getFirstName());
        assertEquals(expectedUserDetails.getLastName(), result.getLastName());
        assertEquals(expectedUserDetails.getEmailAddress(), result.getEmailAddress());
    }

    @Test
    void testParseUserDetails_IOException() throws IOException {
        // Given
        when(httpResponse.getEntity()).thenReturn(httpEntity);
        when(httpEntity.getContent()).thenThrow(new IOException("Read error"));

        // When
        UserDetails result = erixaAPIInterface.parseUserDetails(httpResponse);

        // Then
        assertNull(result);
        verify(eventException).fireAsync(any(IOException.class));
    }

    @Test
    void testParseDrugstoreUploadResult_NotImplemented() {
        // Given
        // This method is currently not implemented and returns null

        // When
        Object result = erixaAPIInterface.parseDrugstoreUploadResult(httpResponse);

        // Then
        assertNull(result);
    }

    private UserDetails createSampleUserDetails() {
        UserDetails userDetails = new UserDetails();
        userDetails.setId(12345);
        userDetails.setRole(1);
        userDetails.setGender("M");
        userDetails.setSalutation("Herr");
        userDetails.setFirstName("Max");
        userDetails.setLastName("Mustermann");
        userDetails.setBirthday("1980-01-01");
        userDetails.setStreet("Musterstraße 123");
        userDetails.setPostcode("12345");
        userDetails.setCity("Musterstadt");
        userDetails.setCountry("Deutschland");
        userDetails.setEmailAddress("max.mustermann@example.com");
        userDetails.setTelephoneNumber("+49 123 456789");
        userDetails.setInsuranceType("GKV");
        userDetails.setHealthInsurance("AOK");
        userDetails.setHealthInsuranceNumber("A123456789");
        userDetails.setOwnInsuredNumber("123456789");
        userDetails.setInsuranceState("NRW");
        userDetails.setBusinessPlaceNumber("123456789");
        userDetails.setDoctorNumber("123456789");
        userDetails.setDrugstoreNumber("123456789");
        userDetails.setPharmacistPtaNumber("123456789");
        userDetails.setFavoredDrugstoreId("123456789");
        userDetails.setFavoredDoctorId("123456789");
        userDetails.setSignMeUsername("max.mustermann");
        return userDetails;
    }
}
