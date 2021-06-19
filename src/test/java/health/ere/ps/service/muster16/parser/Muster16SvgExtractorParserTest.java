package health.ere.ps.service.muster16.parser;

import org.codehaus.plexus.util.StringUtils;
import org.jboss.logging.Logger;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;

import health.ere.ps.model.muster16.MedicationString;
import health.ere.ps.model.muster16.Muster16PrescriptionForm;
import io.quarkus.test.junit.QuarkusTest;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@Disabled
@QuarkusTest
class Muster16SvgExtractorParserTest {
    @Inject
    Logger logger;

    private Muster16PrescriptionForm muster16PrescriptionForm;

    @BeforeEach
    void setUp() {
        logger.info("Setting up parser using SVGExtractor");

        Map<String, String> mappedFields = new HashMap<>();
        Muster16SvgExtractorParser parser = new Muster16SvgExtractorParser(mappedFields);
        muster16PrescriptionForm = new Muster16PrescriptionForm(
                parser.parseInsuranceCompany(),
                parser.parseInsuranceCompanyId(),
                parser.parsePatientNamePrefix(),
                parser.parsePatientFirstName(),
                parser.parsePatientLastName(),
                parser.parsePatientStreetName(),
                parser.parsePatientStreetNumber(),
                parser.parsePatientCity(),
                parser.parsePatientZipCode(),
                parser.parsePatientDateOfBirth(),
                parser.parsePatientInsuranceId(),
                parser.parseClinicId(),
                parser.parseDoctorId(),
                parser.parsePrescriptionDate(),
                parser.parseIsWithPayment(),
                parser.parsePrescriptionList()
        );
    }

    @Test
    void parseInsuranceCompanyId() {
        assertTrue(StringUtils.isNotBlank(muster16PrescriptionForm.getInsuranceCompanyId()));
    }

    @Test
    void parsePatientFirstName() {
        assertTrue(StringUtils.isNotBlank(muster16PrescriptionForm.getPatientFirstName()));
    }

    @Test
    void parsePatientLastName() {
        assertTrue(StringUtils.isNotBlank(muster16PrescriptionForm.getPatientLastName()));
    }

    @Test
    void parsePatientStreetName() {
        assertTrue(StringUtils.isNotBlank(muster16PrescriptionForm.getPatientStreetName()));
    }

    @Test
    void parsePatientStreetNumber() {
        assertTrue(StringUtils.isNotBlank(muster16PrescriptionForm.getPatientStreetNumber()));
    }

    @Test
    void parsePatientCity() {
        assertTrue(StringUtils.isNotBlank(muster16PrescriptionForm.getPatientCity()));
    }

    @Test
    void parsePatientZipCode() {
        assertTrue(StringUtils.isNotBlank(muster16PrescriptionForm.getPatientZipCode()));
    }

    @Test
    void parsePatientDateOfBirth() {
        assertTrue(StringUtils.isNotBlank(muster16PrescriptionForm.getPatientDateOfBirth()));
    }

    @Test
    void parseClinicId() {
        assertTrue(StringUtils.isNotBlank(muster16PrescriptionForm.getClinicId()));
    }

    @Test
    void parseDoctorId() {
        assertTrue(StringUtils.isNotBlank(muster16PrescriptionForm.getDoctorId()));
    }

    @Test
    void parsePrescriptionDate() {
        assertTrue(StringUtils.isNotBlank(muster16PrescriptionForm.getPrescriptionDate()));
    }

    @Test
    void parsePrescriptionList() {
        List<MedicationString> meds = muster16PrescriptionForm.getPrescriptionList();

        assertNotNull(meds);
        assertTrue(meds.size() > 0);
    }

    @Test
    void parsePatientInsuranceId() {
        assertTrue(StringUtils.isNotBlank(muster16PrescriptionForm.getPatientInsuranceId()));
    }

    void getMappedFields(Muster16SvgExtractorParser parser) {
        parser.getMappedFields().entrySet().stream().forEach((entry) -> {
            logger.info(entry.getKey() + " = " + entry.getValue());
        });
    }
}