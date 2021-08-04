package health.ere.ps.service.erixa;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Base64;
import java.util.Date;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.ObservesAsync;
import javax.inject.Inject;

import com.fasterxml.jackson.databind.ObjectMapper;

import health.ere.ps.config.UserConfig;
import health.ere.ps.event.erixa.SendToPharmacyEvent;
import health.ere.ps.model.erixa.PrescriptionTransferEntry;
import health.ere.ps.model.erixa.api.mapping.DeliveryType;
import health.ere.ps.model.erixa.api.mapping.DoctorUploadToDrugstorePrescriptionModel;
import health.ere.ps.model.erixa.api.mapping.PrescriptionColor;
import health.ere.ps.model.erixa.api.mapping.PrescriptionData;
import health.ere.ps.model.erixa.api.mapping.PrescriptionDoctorData;
import health.ere.ps.model.erixa.api.mapping.Role;
import health.ere.ps.model.erixa.api.mapping.UserDetails;

@ApplicationScoped
public class ErixaUploadService {

    @Inject
    ErixaAPIInterface apiInterface;

    @Inject
    UserConfig userConfig;

    private final ObjectMapper mapper;
    private final SimpleDateFormat simpleDateFormat, timestampFormat;


    public ErixaUploadService() {
        mapper = new ObjectMapper();
        simpleDateFormat = new SimpleDateFormat("dd-MM-yyyy");
        timestampFormat = new SimpleDateFormat("yyyy-MM-dd'T'hh:mm:ss'Z'");
    }


    public void uploadPrescriptionToDrugstore(@ObservesAsync SendToPharmacyEvent event) throws IOException {
        PrescriptionTransferEntry details = event.getDetails();

        DoctorUploadToDrugstorePrescriptionModel model = buildBody(event.getDocument(), details);
        String json = mapper.writeValueAsString(model);
        apiInterface.uploadToDrugstore(json);
    }

    private DoctorUploadToDrugstorePrescriptionModel buildBody(String document, PrescriptionTransferEntry details) {

        DoctorUploadToDrugstorePrescriptionModel model = new DoctorUploadToDrugstorePrescriptionModel();
        interpolateDrugstoreDetails(model);
        interpolateDocumentDetails(model, details, document);
        interpolatePrescriptionDetails(model, details);

        return model;
    }

    private void interpolateDocumentDetails(DoctorUploadToDrugstorePrescriptionModel model, PrescriptionTransferEntry details, String document) {
        model.setBase64File(document);
        model.setFileType("PDF");
        model.setFileName(getFileName(details));
        model.setFileSize(getFileSize(document));
    }

    private String getFileName(PrescriptionTransferEntry details) {
        String date = simpleDateFormat.format(details.getCreationDateTime());
        String name = String.format("%s %s", details.getFirstName(), details.getLastName());
        String receiver = getDrugstoreEmail();

        return String.format("%s %s %s.pdf", date, name, receiver);
    }

    private int getFileSize(String base64Document) {
        return Base64.getDecoder().decode(base64Document).length;
    }

    private void interpolateDrugstoreDetails(DoctorUploadToDrugstorePrescriptionModel model) {
        model.setDrugstoreEmailAddress(getDrugstoreEmail());
        model.setDrugstoreSourceType(0);
    }

    private String getDrugstoreEmail() {
        return userConfig.getErixaReceiverEmail();
    }

    private void interpolatePrescriptionDetails(DoctorUploadToDrugstorePrescriptionModel model, PrescriptionTransferEntry details) {
        model.setPrescriptionData(buildPrescriptionData(details));
    }

    private PrescriptionData buildPrescriptionData(PrescriptionTransferEntry entry) {
        PrescriptionData data = new PrescriptionData();
        data.setRole(Role.PATIENT);
        data.setFirstName(entry.getFirstName());
        data.setLastName(entry.getLastName());
        data.setSalutation(null);// entry.getSalutation());
        data.setBirthday(parseBirthday(entry.getBirthday()));
        data.setPostcode(entry.getPostcode());
        data.setStreet(entry.getStreet());
        data.setCity(entry.getCity());
        data.setCountry("DE");
        data.setEmailAddress(entry.getEmailAddress());
        data.setInsuranceType(entry.getInsuranceType());
        data.setHealthInsuranceNumber(entry.getHealthInsuranceNumber());
        data.setCreationDateTime(toTimestamp(entry.getCreationDateTime()));
        data.setDeliveryType(DeliveryType.HOME_DELIVERY);
        data.setPrescriptionColor(PrescriptionColor.RED);
        data.setPzn1(entry.getPzn());
        data.setAutIdem1(entry.isAutIdem());
        data.setDosage1(entry.getDosage());
        data.setMedicineDescription1(entry.getMedicineDescription());
        data.setExtraPaymentNecessary(entry.isExtraPaymentNecessary());
        if(entry.getSurgeryDate()!=null)
            interpolateSurgeryData(data, entry);
        return data;
    }


    private void interpolateSurgeryData(PrescriptionData data, PrescriptionTransferEntry entry) {
         // Invoked when entry.surgeryDate != null
        // Set pzn3 = pzn1
        // Set medicineDescription3 = surgeryDate
        data.setPzn3(entry.getPzn());
        data.setMedicineDescription3(toTimestamp(entry.getSurgeryDate()));
    }

    private String parseBirthday(Date birthday) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'hh:mm:ss'Z'");
        return dateFormat.format(birthday);
    }

    private PrescriptionDoctorData getDoctorData() {
        UserDetails userDetails = apiInterface.getUserDetails();
        return new PrescriptionDoctorData(userDetails);
    }

    private String toTimestamp(Date date){
        return timestampFormat.format(date);
    }
}
