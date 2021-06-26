package health.ere.ps.service.erixa;

import com.fasterxml.jackson.databind.ObjectMapper;
import health.ere.ps.event.erixa.SendToPharmacyEvent;
import health.ere.ps.model.erixa.PrescriptionTransferEntry;
import health.ere.ps.model.erixa.api.mapping.DoctorUploadToDrugstorePrescriptionModel;
import health.ere.ps.model.erixa.api.mapping.PrescriptionData;
import health.ere.ps.model.erixa.api.mapping.PrescriptionDoctorData;
import health.ere.ps.model.erixa.api.mapping.UserDetails;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.ObservesAsync;
import javax.inject.Inject;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

@ApplicationScoped
public class ErixaUploadService {

    @Inject
    ErixaAPIInterface apiInterface;

    private final ObjectMapper mapper;


    public ErixaUploadService() {
        mapper = new ObjectMapper();
    }


    public void uploadPrescriptionToDrugstore(@ObservesAsync SendToPharmacyEvent event) throws IOException {
        String document = event.getDocument();
        PrescriptionTransferEntry details = event.getDetails();

        DoctorUploadToDrugstorePrescriptionModel model = buildBody(document, details);
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
        String date = details.getCreationDateTime();
        String name = String.format("%s %s", details.getFirstName(), details.getLastName());
        String receiver = getDrugstoreEmail();

        return String.format("%s %s %s.pdf", date, name, receiver);
    }

    private int getFileSize(String base64Document) {
        // TODO implement method
        throw new UnsupportedOperationException();
    }

    private void interpolateDrugstoreDetails(DoctorUploadToDrugstorePrescriptionModel model) {
        model.setDrugstoreEmailAddress(getDrugstoreEmail());
        model.setDrugstoreSourceType(0);
    }

    private String getDrugstoreEmail() {
        // TODO implement method
        throw new UnsupportedOperationException();
    }

    private void interpolatePrescriptionDetails(DoctorUploadToDrugstorePrescriptionModel model, PrescriptionTransferEntry details) {
        model.setPrescriptionData(buildPrescriptionData(details));
    }

    private PrescriptionData buildPrescriptionData(PrescriptionTransferEntry entry) {
        PrescriptionData data = new PrescriptionData();
        // TODO set role
        data.setFirstName(entry.getFirstName());
        data.setLastName(entry.getLastName());
        data.setGender(entry.getGender());
        data.setSalutation(entry.getSalutation());
        data.setBirthday(parseBirthday(entry.getBirthday()));
        data.setPostcode(entry.getPostcode());
        data.setStreet(entry.getStreet());
        data.setCity(entry.getCity());
        data.setCountry("DE");
        // TODO fetch and set email address
        data.setInsuranceType(entry.getInsuranceType());
        data.setCreationDateTime(entry.getCreationDateTime());
        // TODO set delivery type
        data.setPrescriptionColor("Red");
        data.setPzn1(entry.getPzn());
        data.setAutIdem1(entry.isAutIdem());
        data.setDosage1(entry.getDosage());
        data.setMedicineDescription1(entry.getMedicineDescription());
        data.setExtraPaymentNecessary(entry.isExtraPaymentNecessary());
        return data;
    }

    private String parseBirthday(Date birthday) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'hh:mm:ss'Z'");
        return dateFormat.format(birthday);
    }

    private PrescriptionDoctorData getDoctorData() {
        UserDetails userDetails = apiInterface.getUserDetails();
        return new PrescriptionDoctorData(userDetails);
    }
}
