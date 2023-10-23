package health.ere.ps.service.erixa;

import javax.enterprise.event.Event;

import org.junit.jupiter.api.Test;

import health.ere.ps.config.UserConfig;
import health.ere.ps.event.erixa.SendToPharmacyEvent;
import health.ere.ps.model.erixa.PrescriptionTransferEntry;
import health.ere.ps.model.erixa.api.mapping.DoctorUploadToDrugstorePrescriptionModel;
import health.ere.ps.service.erixa.ErixaAPIInterface;
import health.ere.ps.service.erixa.ErixaUploadService;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.*;

public class ErixaUploadServiceTest {

    private ErixaUploadService erixaUploadService;
    private ErixaAPIInterface apiInterface;
    private UserConfig userConfig;
    private Event<Exception> exceptionEvent;
    private SendToPharmacyEvent sendToPharmacyEvent;

    @Test
    public void testUploadPrescriptionToDrugstore() throws IOException {
        erixaUploadService.uploadPrescriptionToDrugstore(sendToPharmacyEvent);
        verify(apiInterface).uploadToDrugstore(anyString());
    }

    /**
     * 
     */
    @Test
    public void testBuildBody() {
        PrescriptionTransferEntry details = new PrescriptionTransferEntry();
    }

    @Test
    public void testInterpolateDocumentDetails(PrescriptionTransferEntry prescriptionTransferEntry) {
        PrescriptionTransferEntry details = new PrescriptionTransferEntry();
        DoctorUploadToDrugstorePrescriptionModel model = new DoctorUploadToDrugstorePrescriptionModel();
        assertNotNull(model.getBase64File());
        assertNotNull(model.getFileType());
        assertNotNull(model.getFileName());
        assertNotNull(model.getFileSize());
    }

    @Test
    public void testGetFileName() {
        PrescriptionTransferEntry details = new PrescriptionTransferEntry();
        details.setCreationDateTime(new Date());
        details.setFirstName("John");
        details.setLastName("Doe");
    }

    /**
     * 
     */
    @Test
    public void testGetFileSize() {
        String base64Document = "base64_encoded_document_string";
    }
}
