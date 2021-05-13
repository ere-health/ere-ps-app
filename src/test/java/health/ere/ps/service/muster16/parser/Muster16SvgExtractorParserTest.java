package health.ere.ps.service.muster16.parser;

import org.codehaus.plexus.util.StringUtils;
import org.jboss.logging.Logger;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.util.List;

import javax.inject.Inject;

import health.ere.ps.model.muster16.Muster16PrescriptionForm;
import io.quarkus.test.junit.QuarkusTest;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@Disabled
@QuarkusTest
class Muster16SvgExtractorParserTest {
    @Inject
    Logger logger;

    @Inject
    Muster16SvgExtractorParser parser;

    private Muster16PrescriptionForm muster16PrescriptionForm;

    @BeforeEach
    void setUp() throws URISyntaxException, IOException {
        logger.info("Setting up parser using SVGExtractor");

        try(InputStream muster16PdfFileStream = Muster16SvgExtractorParserTest.class.getResourceAsStream(
                "/muster-16-print-samples/test1.pdf")) {
            parser.init(muster16PdfFileStream);

            muster16PrescriptionForm = new Muster16PrescriptionForm(
                    parser.parseInsuranceCompany(),
                    parser.parseInsuranceCompanyId(),
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
                    parser.parsePrescriptionList()
            );
        }
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
        List<String> meds = muster16PrescriptionForm.getPrescriptionList();

        assertNotNull(meds);
        assertTrue(meds.size() > 0);
    }

    @Test
    void parsePatientInsuranceId() {
        assertTrue(StringUtils.isNotBlank(muster16PrescriptionForm.getPatientInsuranceId()));
    }

    @Test
    void getMappedFields() {
        parser.getMappedFields().entrySet().stream().forEach((entry) -> {
            logger.info(entry.getKey() + " = " + entry.getValue());
        });
    }
}