package health.ere.ps.service.erixa;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.parser.IParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import health.ere.ps.event.erixa.ErixaUploadEvent;
import health.ere.ps.model.erixa.api.credentials.BasicAuthCredentials;
import health.ere.ps.model.erixa.ErixaUploadMessagePayload;
import health.ere.ps.model.erixa.api.mapping.DoctorUploadToDrugstorePrescriptionModel;
import health.ere.ps.model.erixa.api.mapping.PrescriptionData;
import health.ere.ps.model.erixa.api.mapping.PrescriptionDoctorData;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.hl7.fhir.r4.model.Bundle;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.ObservesAsync;
import javax.inject.Inject;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;

@ApplicationScoped
public class ErixaUploadService {

    @ConfigProperty(name = "erixa.api.url.upload")
    String uploadURL;

    @Inject
    ErixaHttpClient httpClient;

    private final IParser bundleParser;
    private final ObjectMapper mapper;


    public ErixaUploadService() {
        bundleParser = FhirContext.forR4().newJsonParser();
        mapper = new ObjectMapper();
    }


    public void uploadPrescriptionToDrugstore(@ObservesAsync ErixaUploadEvent event) throws IOException {

        ErixaUploadMessagePayload load = event.getLoad();

        DoctorUploadToDrugstorePrescriptionModel model = buildBody(load.getDocument(), load.getBundle());
        String json = mapper.writeValueAsString(model);

        httpClient.sendPostRequest(getUploadURI(), json);
    }


    private URL getUploadURI() {
        try {
            return new URL(uploadURL);
        } catch (MalformedURLException e) {
            return null;
        }
    }

    private String toBasicAuth(BasicAuthCredentials credentials) {
        return String.join(":", credentials.getEmail(), credentials.getPassword());
    }

    private DoctorUploadToDrugstorePrescriptionModel buildBody(String document, String bundleString) {

        PrescriptionData prescriptionData = parseBundle(bundleString);

        DoctorUploadToDrugstorePrescriptionModel model = new DoctorUploadToDrugstorePrescriptionModel();
        model.setBase64File(document);
        model.setDrugstoreEmailAddress("mc30.apo@mail-mc.wps.de");
        model.setDrugstoreSourceType(1);
        model.setFileName("[2021-06-24] [Neithart] [mc30.apo@mail-mc.wps.de].pdf");
        model.setFileSize(114783);
        model.setFileType("application/pdf");
        model.setPrescriptionData(prescriptionData);
//        model.setDrugstoreId("13");

        return model;
    }

    private PrescriptionData parseBundle(String bundleString) {

        PrescriptionData prescriptionData = new PrescriptionData();

        PrescriptionDoctorData doctorData = getDoctorData();

        // TODO parse the bundle and interpolate prescription data

        prescriptionData.setCreationDateTime("2021-06-23T15:30:00.000Z");
        prescriptionData.setDeliveryType("SelfCollect");
        prescriptionData.setEmailAddress("donotreply@am.gmbh");
        prescriptionData.setExtraPaymentNecessary(false);
        prescriptionData.setSalutation("Mr");
        prescriptionData.setFirstName("John");
        prescriptionData.setLastName("Doe");
        prescriptionData.setLastName("1970-01-01T00:00:00.000Z");
        prescriptionData.setStreet("Blumen Straße");
        prescriptionData.setPostcode("12345");
        prescriptionData.setCity("München");
        prescriptionData.setCity("Deutschland");
        prescriptionData.setInsuranceType("PKV");
        prescriptionData.setPrescriptionColor("Blue");
        prescriptionData.setRole("Patient");
        prescriptionData.setDescription("New Prescription");
        prescriptionData.setGender("W");
        prescriptionData.setTelephoneNumber("030/12345678");
//        prescriptionData.setHealthInsurance("030/12345678");
//        prescriptionData.setHealthInsuranceNumber("030/12345678");
//        prescriptionData.setOwnInsuredNumber("030/12345678");
//        prescriptionData.setInsuranceState("030/12345678");
        prescriptionData.setPzn1("04527098");
        prescriptionData.setAutIdem1(false);
        prescriptionData.setDosage1("2mal tägl. 5ml");
        prescriptionData.setMedicineDescription1("1x Novalgin AMP N1 5X2 ml Tabletten / 10 St");
//        prescriptionData.setIcdCode("A65_A69");
        prescriptionData.setDoctorData(doctorData);

        return prescriptionData;
    }

    private PrescriptionDoctorData getDoctorData() {
        PrescriptionDoctorData doctorData = new PrescriptionDoctorData();
        doctorData.setUserDataId(77);
        doctorData.setDoctorNumber("987654321");
        doctorData.setBusinessPlaceNumber("999888777");
        return doctorData;
    }
}
