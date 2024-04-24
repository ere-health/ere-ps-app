package health.ere.ps.model.erixa;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.text.ParseException;
import java.text.SimpleDateFormat;

import jakarta.json.Json;
import jakarta.json.JsonObject;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.Date;

public class PrescriptionTransferEntryTest {
	
	static PrescriptionTransferEntry prescriptionTransferEntry = new PrescriptionTransferEntry();
	
	@BeforeAll
	public static void init() throws FileNotFoundException, ParseException
	{
		JsonObject messageWithPrescriptionTransferEntry = Json.createReader(new FileInputStream("src/test/resources/erixa/PrescriptionTest.json")).readObject();
		prescriptionTransferEntry.setFirstName(messageWithPrescriptionTransferEntry.getString("firstName"));
		prescriptionTransferEntry.setLastName(messageWithPrescriptionTransferEntry.getString("lastName"));
		prescriptionTransferEntry.setSalutation(messageWithPrescriptionTransferEntry.getString("salutation"));
		String birthDateStr = messageWithPrescriptionTransferEntry.getString("birthday");
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
		Date birthDate = sdf.parse(birthDateStr);
		prescriptionTransferEntry.setBirthday(birthDate);
		prescriptionTransferEntry.setStreet(messageWithPrescriptionTransferEntry.getString("street"));
		prescriptionTransferEntry.setPostcode(messageWithPrescriptionTransferEntry.getString("postcode"));
		prescriptionTransferEntry.setCity(messageWithPrescriptionTransferEntry.getString("city"));
		prescriptionTransferEntry.setEmailAddress(messageWithPrescriptionTransferEntry.getString("emailAdress"));
		prescriptionTransferEntry.setInsuranceType(messageWithPrescriptionTransferEntry.getString("insurenceType"));
		prescriptionTransferEntry.setHealthInsurance(messageWithPrescriptionTransferEntry.getString("healthInsurance"));
		prescriptionTransferEntry.setHealthInsuranceNumber(messageWithPrescriptionTransferEntry.getString("healthInsuranceNumber"));
		prescriptionTransferEntry.setPzn(messageWithPrescriptionTransferEntry.getString("pzn"));
		prescriptionTransferEntry.setAutIdem(messageWithPrescriptionTransferEntry.getBoolean("autIdem"));
		prescriptionTransferEntry.setDosage(messageWithPrescriptionTransferEntry.getString("dosage"));
		prescriptionTransferEntry.setMedicineDescription(messageWithPrescriptionTransferEntry.getString("medicineDescription"));
		prescriptionTransferEntry.setExtraPaymentNecessary(messageWithPrescriptionTransferEntry.getBoolean("extraPaymentNecessary"));
		String creationDateStr = messageWithPrescriptionTransferEntry.getString("creationDateTime");
		SimpleDateFormat sdf_time = new SimpleDateFormat("yyyy-MM-dd'T'hh:mm:ss'Z'");
		Date creationDate = sdf_time.parse(creationDateStr);
		prescriptionTransferEntry.setCreationDateTime(creationDate);
		String surgeryDateStr = messageWithPrescriptionTransferEntry.getString("surgeryDate");
		Date surgeryDate = sdf.parse(surgeryDateStr);
		prescriptionTransferEntry.setSurgeryDate(surgeryDate);
		
	}
	
	@Test
	public void testPrescriptionTransferEntry(){
		assertEquals("Mario", prescriptionTransferEntry.getFirstName());
		assertEquals("Müller", prescriptionTransferEntry.getLastName());
		assertEquals("Herr", prescriptionTransferEntry.getSalutation());
		// assertEquals("Thu Sep 19 00:00:00 CET 1963", prescriptionTransferEntry.getBirthday().toString());
		assertEquals("Musterstraße",prescriptionTransferEntry.getStreet());
		assertEquals("12345", prescriptionTransferEntry.getPostcode());
		assertEquals("Musterstadt", prescriptionTransferEntry.getCity());
		assertEquals("müller.mario@outlook.de", prescriptionTransferEntry.getEmailAddress());
		assertEquals("gesetzlich", prescriptionTransferEntry.getInsuranceType());
		assertEquals("bkk Pfalz", prescriptionTransferEntry.getHealthInsurance());
		assertEquals("LFX9F5GC", prescriptionTransferEntry.getHealthInsuranceNumber());
		assertEquals("01016090", prescriptionTransferEntry.getPzn());
		assertEquals(true, prescriptionTransferEntry.isAutIdem());
		assertEquals("1/1/1", prescriptionTransferEntry.getDosage());
		assertEquals("Ibuprofen 600mg", prescriptionTransferEntry.getMedicineDescription());
		assertEquals(true, prescriptionTransferEntry.isExtraPaymentNecessary());
		// assertEquals("Mon Nov 28 09:05:23 CET 2022", prescriptionTransferEntry.getCreationDateTime().toString());
		// assertEquals("Fri Nov 25 00:00:00 CET 2022", prescriptionTransferEntry.getSurgeryDate().toString());
	}
}
