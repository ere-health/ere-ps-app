package health.ere.ps.service.erixa;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.util.Date;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.fasterxml.jackson.databind.ObjectMapper;

import health.ere.ps.config.UserConfig;
import health.ere.ps.event.erixa.SendToPharmacyEvent;
import health.ere.ps.model.erixa.PrescriptionTransferEntry;
import health.ere.ps.model.erixa.api.mapping.DoctorUploadToDrugstorePrescriptionModel;
import health.ere.ps.model.erixa.api.mapping.PrescriptionData;

class ErixaUploadServiceTest {

    @InjectMocks
    private ErixaUploadService erixaUploadService;

    @Mock
    private ErixaAPIInterface apiInterface;

    @Mock
    private UserConfig userConfig;

    private ObjectMapper objectMapper = new ObjectMapper();

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        when(userConfig.getErixaReceiverEmail()).thenReturn("pharmacy@example.com");
    }

    @Test
    void testUploadPrescriptionToDrugstore() throws IOException {
        String documentBase64 = "RG9taSdzIFByZXNjcmlwdGlvbg=="; // "Domi's Prescription" in base64
        PrescriptionTransferEntry prescriptionDetails = createPrescriptionDetails();
        
        SendToPharmacyEvent event = mock(SendToPharmacyEvent.class);
        when(event.getDocument()).thenReturn(documentBase64);
        when(event.getDetails()).thenReturn(prescriptionDetails);
        
        erixaUploadService.uploadPrescriptionToDrugstore(event);
        
        ArgumentCaptor<String> jsonCaptor = ArgumentCaptor.forClass(String.class);
        verify(apiInterface).uploadToDrugstore(jsonCaptor.capture());
        
        DoctorUploadToDrugstorePrescriptionModel model = 
            objectMapper.readValue(jsonCaptor.getValue(), DoctorUploadToDrugstorePrescriptionModel.class);
        
        assertEquals(documentBase64, model.getBase64File());
        assertEquals("PDF", model.getFileType());
        assertEquals("pharmacy@example.com", model.getDrugstoreEmailAddress());
        
        PrescriptionData prescriptionData = model.getPrescriptionData();
        assertEquals("John", prescriptionData.getFirstName());
        assertEquals("Doe", prescriptionData.getLastName());
        assertEquals("12345", prescriptionData.getPostcode());
        assertEquals("123456789", prescriptionData.getPzn1());
    }
    
    // Helper methods
    private PrescriptionTransferEntry createPrescriptionDetails() {
        PrescriptionTransferEntry entry = new PrescriptionTransferEntry();
        entry.setFirstName("John");
        entry.setLastName("Doe");
        entry.setBirthday(new Date());
        entry.setPostcode("12345");
        entry.setStreet("Test Street");
        entry.setCity("Test City");
        entry.setEmailAddress("patient@example.com");
        entry.setInsuranceType("public");
        entry.setHealthInsuranceNumber("A123456789");
        entry.setCreationDateTime(new Date());
        entry.setPzn("123456789");
        entry.setAutIdem(true);
        entry.setDosage("1-0-1");
        entry.setMedicineDescription("Test Medicine");
        return entry;
    }
}