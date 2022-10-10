package health.ere.ps.service.erixa;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

import javax.inject.Inject;
import javax.json.Json;
import javax.json.JsonArrayBuilder;
import javax.json.JsonObject;

import org.apache.http.HttpResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import health.ere.ps.config.AppConfig;
import health.ere.ps.event.erixa.ErixaEvent;
import health.ere.ps.event.erixa.SendToPharmacyEvent;
import health.ere.ps.exception.idp.IdpClientException;
import health.ere.ps.exception.idp.IdpException;
import health.ere.ps.exception.idp.IdpJoseException;
import health.ere.ps.service.idp.client.IdpClient;
import health.ere.ps.service.idp.client.IdpHttpClientService;
import io.quarkus.test.junit.QuarkusTest;

@QuarkusTest
public class ErixaUploadServiceTest {

	@Inject
	ErixaUploadService service;

	@Inject
	IdpClient idpClient;

	@Inject
	AppConfig appConfig;
	
	String userURL;

	@BeforeEach
	public void init() throws IdpClientException, IdpException, IdpJoseException {
		 userURL = appConfig.getIdpBaseURL() + IdpHttpClientService.USER_AGENT;
		
		idpClient.init(appConfig.getIdpClientId(), appConfig.getIdpAuthRequestRedirectURL(), userURL,
				true);
		idpClient.initializeClient();

	}

	@Test
	public void testUploadPrescriptionToDrugStore() throws IOException {

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

		JsonObject erixaObject = Json.createObjectBuilder().add("ProcessType", "SendToPharmacy")
				.add("payload", payloadObj).build();

		ErixaEvent eEvent = new ErixaEvent(erixaObject);
		HttpResponse response = service.generatePrescriptionBungidle(eEvent);
		System.out.println(response);

	}

	private JsonObject buildJsonObject(String key, String value) {
		return Json.createObjectBuilder().add(key, value).build();

	}

}
