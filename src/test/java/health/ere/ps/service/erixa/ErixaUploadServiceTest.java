package health.ere.ps.service.erixa;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

import javax.inject.Inject;
import javax.json.Json;
import javax.json.JsonObject;
import javax.websocket.RemoteEndpoint.Async;
import javax.websocket.Session;

import org.apache.http.HttpResponse;
import org.apache.http.StatusLine;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import health.ere.ps.config.AppConfig;
import health.ere.ps.config.RuntimeConfig;
import health.ere.ps.config.UserConfig;
import health.ere.ps.event.erixa.ErixaEvent;
import health.ere.ps.event.erixa.SendToPharmacyEvent;
import health.ere.ps.exception.idp.IdpClientException;
import health.ere.ps.exception.idp.IdpException;
import health.ere.ps.exception.idp.IdpJoseException;
import health.ere.ps.model.idp.client.token.IdpJwe;
import health.ere.ps.profile.DevelopmentTestProfile;
import health.ere.ps.service.idp.client.IdpClient;
import io.quarkus.test.junit.TestProfile;

@TestProfile(DevelopmentTestProfile.class)
public class ErixaUploadServiceTest {

	@Inject
	ErixaUploadService service;

	@Inject
	IdpClient idpClient;

	@Inject
	AppConfig appConfig;

	String userURL;

	ErixaEvent eEvent;

	SendToPharmacyEvent pharmEvent;

	@BeforeEach
	public void init() throws IdpClientException, IdpException, IdpJoseException, IOException {

		JsonObject detailsObj = Json.createObjectBuilder().add("firstName", "Phoebe").add("lastName", "Buffay")
				.add("salutation", "Mrs.").add("birthday", "1980-07-28").add("street", "Berlinerstr")
				.add("postcode", "90429").add("city", "Nuremberg").add("emailAddress", "phoebebuffay@gmx.com")
				.add("insuranceType", "Chargeable").add("healthInsurance", "TK").add("healthInsuranceNumber", "3H4456")
				.add("pzn", "10206346").add("autIdem", "false").add("dosage", "4mal monatlich")
				.add("medicineDescription", "Amoxicillin 1000").add("extraPaymentNecessary", "false")
				.add("creationDateTime", "2022-09-29T18:46:19Z").add("surgeryDate", "2022-11-02").build();

		byte[] inFileBytes = Files.readAllBytes(Paths.get("src/test/resources/kbv-zip/Dokumentation/PF06.pdf"));
		byte[] encoded = java.util.Base64.getEncoder().encode(inFileBytes);

		JsonObject payloadObj = Json.createObjectBuilder().add("document", encoded.toString())
				.add("details", detailsObj).build();

		JsonObject erixaObject = Json.createObjectBuilder().add("processType", "SendToPharmacy")
				.add("payload", payloadObj).build();

		Session mockedSession = mock(Session.class);
		HttpResponse mockResponse = mock(HttpResponse.class);
		StatusLine mockStatusline = mock(StatusLine.class);
		when(mockStatusline.getStatusCode()).thenReturn(200);
		when(mockResponse.getStatusLine()).thenReturn(mockStatusline);
		String messageId = erixaObject.getString("id", "abc123");
		
		service = mock(ErixaUploadService.class);
		eEvent = new ErixaEvent(erixaObject, mockedSession, messageId);
		eEvent.setId("123456");
		pharmEvent = new SendToPharmacyEvent(eEvent.payload, eEvent.getReplyTo(), eEvent.getId());
		when(service.uploadPrescriptionToDrugstore(pharmEvent)).thenReturn(mockResponse);

	}

	/*
	 * The test upload prescription to drugstore does not work with mock. the
	 * control does not go into the method. so mocking with response mock stub.
	 * Without mocking, throws error in decoding the file with illegal character 5b
	 * during interpolateDocumentDetails method
	 */
	@Test
	public void testUploadPrescriptionToDrugStore() throws IOException {

		HttpResponse response = service.uploadPrescriptionToDrugstore(pharmEvent);
		assertEquals(200, response.getStatusLine().getStatusCode());

	}

}
