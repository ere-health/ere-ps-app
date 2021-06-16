package health.ere.ps.service.fhir.bundle;

import org.hl7.fhir.r4.model.Bundle;
import org.hl7.fhir.r4.model.CanonicalType;
import org.hl7.fhir.r4.model.Coverage;
import org.hl7.fhir.r4.model.Patient;
import org.jboss.logging.Logger;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import java.net.URISyntaxException;
import java.text.ParseException;
import java.util.Arrays;
import java.util.stream.Collectors;

import javax.inject.Inject;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.parser.IParser;
import ca.uhn.fhir.validation.ValidationResult;
import health.ere.ps.model.muster16.MedicationString;
import health.ere.ps.model.muster16.Muster16PrescriptionForm;
import health.ere.ps.validation.fhir.bundle.PrescriptionBundleValidator;
import io.quarkus.test.junit.QuarkusTest;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

@QuarkusTest
public class PrescriptionBundleBuilderTest {
    @Inject
    Logger logger;

    private PrescriptionBundleValidator prescriptionBundleValidator;
    private PrescriptionBundleBuilder prescriptionBundleBuilder;

    public static PrescriptionBundleBuilder getPrescriptionBundleBuilder() {
        Muster16PrescriptionForm muster16PrescriptionForm;
        muster16PrescriptionForm = new Muster16PrescriptionForm();

        muster16PrescriptionForm.setClinicId("BS12345678");

        muster16PrescriptionForm.setPrescriptionDate("05.04.2021");
        MedicationString medicationString = new MedicationString("Amoxicillin 1000mg N2", null, null, "3x täglich alle 8 Std", null, "2394428");

        muster16PrescriptionForm.setPrescriptionList(Arrays.asList(medicationString));

        muster16PrescriptionForm.setDoctorId("LANR1234");

        muster16PrescriptionForm.setInsuranceCompany("Test Insurance Company, Gmbh");

        muster16PrescriptionForm.setPatientDateOfBirth("16.07.1986");
        muster16PrescriptionForm.setPatientFirstName("John");
        muster16PrescriptionForm.setPatientLastName("Doe");
        muster16PrescriptionForm.setPatientStreetName("Droysenstr.");
        muster16PrescriptionForm.setPatientStreetNumber("7");
        muster16PrescriptionForm.setPatientZipCode("10629");
        muster16PrescriptionForm.setPatientCity("Berlin");
        muster16PrescriptionForm.setPatientInsuranceId("M310119800");

        muster16PrescriptionForm.setDoctorNamePrefix("Dr.");
        muster16PrescriptionForm.setDoctorFirstName("Testarzt");
        muster16PrescriptionForm.setDoctorLastName("E-Rezept");
        muster16PrescriptionForm.setDoctorPhone("123456789");

        muster16PrescriptionForm.setDoctorStreetName("Doc Droysenstr.");
        muster16PrescriptionForm.setDoctorStreetNumber("7a");
        muster16PrescriptionForm.setDoctorZipCode("10630");
        muster16PrescriptionForm.setDoctorCity("Berlinn");

        muster16PrescriptionForm.setDoctorPhone("030/123456");

        muster16PrescriptionForm.setInsuranceCompanyId("100038825");

        return
                new PrescriptionBundleBuilder(muster16PrescriptionForm);
    }

    @BeforeEach
    public void initialize() throws URISyntaxException {
        prescriptionBundleBuilder = getPrescriptionBundleBuilder();
        prescriptionBundleValidator = new PrescriptionBundleValidator();
    }

    @Test
    public void test_Successful_Creation_of_FHIR_EPrescription_Bundle_From_Muster16_Model_Object()
            throws ParseException {

        Bundle fhirEPrescriptionBundle = prescriptionBundleBuilder.createBundle();

        // Expecting the creation of 7 resources
        // 1. composition resource
        // 2. medication request resource
        // 3. medication resource.
        // 4. patient resource.
        // 5. practitioner resource.
        // 6. organization resource.
        // 7. coverage resource.
        assertEquals(7, fhirEPrescriptionBundle.getEntry().size());
    }

    @Test
    public void test_Successful_XML_Serialization_Of_An_FHIR_EPrescription_Bundle_Object()
            throws ParseException {
        FhirContext ctx = FhirContext.forR4();

        IParser parser = ctx.newXmlParser();

        Bundle fhirEPrescriptionBundle = prescriptionBundleBuilder.createBundle();

        fhirEPrescriptionBundle.setId("sample-id-from-gematik-ti-123456");

        parser.setPrettyPrint(true);

        String serialized = parser.encodeResourceToString(fhirEPrescriptionBundle);

        logger.info(serialized);
    }

    @Test
    public void test_Successful_JSON_Serialization_Of_An_FHIR_EPrescription_Bundle_Object()
            throws ParseException {
        FhirContext ctx = FhirContext.forR4();

        IParser parser = ctx.newJsonParser();

        Bundle fhirEPrescriptionBundle = prescriptionBundleBuilder.createBundle();

        fhirEPrescriptionBundle.setId("sample-id-from-gematik-ti-123456");

        parser.setPrettyPrint(true);

        String serialized = parser.encodeResourceToString(fhirEPrescriptionBundle);

        logger.info(serialized);
    }

    @Test
    public void test_Successful_JSON_To_Bundle_Object_Conversion() {
        FhirContext ctx = FhirContext.forR4();
        IParser jsonParser = ctx.newJsonParser();
        String json = "{\n" +
                "  \"resourceType\": \"Bundle\",\n" +
                "  \"id\": \"4c0171cd-1e7e-4466-887d-f19b6370dc6e\",\n" +
                "  \"meta\": {\n" +
                "    \"lastUpdated\": \"2021-06-15T17:01:16.223-04:00\",\n" +
                "    \"profile\": [\n" +
                "      \"https://fhir.kbv.de/StructureDefinition/KBV_PR_ERP_Bundle|1.0.1\"\n" +
                "    ]\n" +
                "  },\n" +
                "  \"type\": \"document\",\n" +
                "  \"timestamp\": \"2021-06-15T17:01:16.223-04:00\",\n" +
                "  \"entry\": [\n" +
                "    {\n" +
                "      \"fullUrl\": \"http://pvs.praxis.local/fhir/Composition/9a36ec63-ffe4-4a2a-928c-74fbe263f317\",\n" +
                "      \"resource\": {\n" +
                "        \"resourceType\": \"Composition\",\n" +
                "        \"id\": \"9a36ec63-ffe4-4a2a-928c-74fbe263f317\",\n" +
                "        \"meta\": {\n" +
                "          \"profile\": [\n" +
                "            \"https://fhir.kbv.de/StructureDefinition/KBV_PR_ERP_Composition|1.0.1\"\n" +
                "          ]\n" +
                "        },\n" +
                "        \"extension\": [\n" +
                "          {\n" +
                "            \"url\": \"https://fhir.kbv.de/StructureDefinition/KBV_EX_FOR_Legal_basis\",\n" +
                "            \"valueCoding\": {\n" +
                "              \"system\": \"https://fhir.kbv.de/CodeSystem/KBV_CS_SFHIR_KBV_STATUSKENNZEICHEN\",\n" +
                "              \"code\": \"04\"\n" +
                "            }\n" +
                "          }\n" +
                "        ],\n" +
                "        \"status\": \"final\",\n" +
                "        \"type\": {\n" +
                "          \"coding\": [\n" +
                "            {\n" +
                "              \"system\": \"https://fhir.kbv.de/CodeSystem/KBV_CS_SFHIR_KBV_FORMULAR_ART\",\n" +
                "              \"code\": \"e16A\"\n" +
                "            }\n" +
                "          ]\n" +
                "        },\n" +
                "        \"subject\": {\n" +
                "          \"reference\": \"Patient/\"\n" +
                "        },\n" +
                "        \"date\": \"2021-06-15T17:01:16-04:00\",\n" +
                "        \"author\": [\n" +
                "          {\n" +
                "            \"reference\": \"Practitioner/30000000\",\n" +
                "            \"type\": \"Practitioner\"\n" +
                "          },\n" +
                "          {\n" +
                "            \"type\": \"Device\",\n" +
                "            \"identifier\": {\n" +
                "              \"system\": \"https://fhir.kbv.de/NamingSystem/KBV_NS_FOR_Pruefnummer\",\n" +
                "              \"value\": \"123456\"\n" +
                "            }\n" +
                "          }\n" +
                "        ],\n" +
                "        \"title\": \"elektronische Arzneimittelverordnung\",\n" +
                "        \"attester\": [\n" +
                "          {\n" +
                "            \"mode\": \"legal\",\n" +
                "            \"party\": {\n" +
                "              \"reference\": \"Practitioner/30000000\"\n" +
                "            }\n" +
                "          }\n" +
                "        ],\n" +
                "        \"custodian\": {\n" +
                "          \"reference\": \"Organization/30000000\"\n" +
                "        },\n" +
                "        \"section\": [\n" +
                "          {\n" +
                "            \"code\": {\n" +
                "              \"coding\": [\n" +
                "                {\n" +
                "                  \"system\": \"https://fhir.kbv.de/CodeSystem/KBV_CS_ERP_Section_Type\",\n" +
                "                  \"code\": \"Prescription\"\n" +
                "                }\n" +
                "              ]\n" +
                "            },\n" +
                "            \"entry\": [\n" +
                "              {\n" +
                "                \"reference\": \"MedicationRequest/0386659d-7feb-4b44-b616-58a0db6588b6\"\n" +
                "              }\n" +
                "            ]\n" +
                "          },\n" +
                "          {\n" +
                "            \"code\": {\n" +
                "              \"coding\": [\n" +
                "                {\n" +
                "                  \"system\": \"https://fhir.kbv.de/CodeSystem/KBV_CS_ERP_Section_Type\",\n" +
                "                  \"code\": \"Coverage\"\n" +
                "                }\n" +
                "              ]\n" +
                "            },\n" +
                "            \"entry\": [\n" +
                "              {\n" +
                "                \"reference\": \"Coverage/\"\n" +
                "              }\n" +
                "            ]\n" +
                "          }\n" +
                "        ]\n" +
                "      }\n" +
                "    },\n" +
                "    {\n" +
                "      \"fullUrl\": \"http://pvs.praxis.local/fhir/MedicationRequest/0386659d-7feb-4b44-b616-58a0db6588b6\",\n" +
                "      \"resource\": {\n" +
                "        \"resourceType\": \"MedicationRequest\",\n" +
                "        \"id\": \"0386659d-7feb-4b44-b616-58a0db6588b6\",\n" +
                "        \"meta\": {\n" +
                "          \"lastUpdated\": \"2021-06-15T17:01:16.223-04:00\",\n" +
                "          \"profile\": [\n" +
                "            \"https://fhir.kbv.de/StructureDefinition/KBV_PR_ERP_Prescription|1.0.1\"\n" +
                "          ]\n" +
                "        },\n" +
                "        \"extension\": [\n" +
                "          {\n" +
                "            \"url\": \"https://fhir.kbv.de/StructureDefinition/KBV_EX_ERP_StatusCoPayment\",\n" +
                "            \"valueCoding\": {\n" +
                "              \"system\": \"https://fhir.kbv.de/CodeSystem/KBV_CS_ERP_StatusCoPayment\",\n" +
                "              \"code\": \"1\"\n" +
                "            }\n" +
                "          },\n" +
                "          {\n" +
                "            \"url\": \"https://fhir.kbv.de/StructureDefinition/KBV_EX_ERP_EmergencyServicesFee\",\n" +
                "            \"valueBoolean\": false\n" +
                "          },\n" +
                "          {\n" +
                "            \"url\": \"https://fhir.kbv.de/StructureDefinition/KBV_EX_ERP_BVG\",\n" +
                "            \"valueBoolean\": false\n" +
                "          },\n" +
                "          {\n" +
                "            \"url\": \"https://fhir.kbv.de/StructureDefinition/KBV_EX_ERP_Multiple_Prescription\",\n" +
                "            \"extension\": [\n" +
                "              {\n" +
                "                \"url\": \"Kennzeichen\",\n" +
                "                \"valueBoolean\": false\n" +
                "              }\n" +
                "            ]\n" +
                "          }\n" +
                "        ],\n" +
                "        \"status\": \"active\",\n" +
                "        \"intent\": \"order\",\n" +
                "        \"medicationReference\": {\n" +
                "          \"reference\": \"Medication/f0e74458-d721-4776-889b-bc57618be8b4\"\n" +
                "        },\n" +
                "        \"subject\": {\n" +
                "          \"reference\": \"Patient/\"\n" +
                "        },\n" +
                "        \"requester\": {\n" +
                "          \"reference\": \"Practitioner/30000000\"\n" +
                "        },\n" +
                "        \"insurance\": [\n" +
                "          {\n" +
                "            \"reference\": \"Coverage/\"\n" +
                "          }\n" +
                "        ],\n" +
                "        \"dosageInstruction\": [\n" +
                "          {\n" +
                "            \"extension\": [\n" +
                "              {\n" +
                "                \"url\": \"https://fhir.kbv.de/StructureDefinition/KBV_EX_ERP_DosageFlag\",\n" +
                "                \"valueBoolean\": true\n" +
                "              }\n" +
                "            ],\n" +
                "            \"text\": \"1-1-1\"\n" +
                "          }\n" +
                "        ],\n" +
                "        \"dispenseRequest\": {\n" +
                "          \"quantity\": {\n" +
                "            \"value\": 1,\n" +
                "            \"system\": \"http://unitsofmeasure.org\",\n" +
                "            \"code\": \"{Package}\"\n" +
                "          }\n" +
                "        },\n" +
                "        \"substitution\": {\n" +
                "          \"allowedBoolean\": true\n" +
                "        }\n" +
                "      }\n" +
                "    },\n" +
                "    {\n" +
                "      \"fullUrl\": \"http://pvs.praxis.local/fhir/Medication/f0e74458-d721-4776-889b-bc57618be8b4\",\n" +
                "      \"resource\": {\n" +
                "        \"resourceType\": \"Medication\",\n" +
                "        \"id\": \"f0e74458-d721-4776-889b-bc57618be8b4\",\n" +
                "        \"meta\": {\n" +
                "          \"profile\": [\n" +
                "            \"https://fhir.kbv.de/StructureDefinition/KBV_PR_ERP_Medication_PZN|1.0.1\"\n" +
                "          ]\n" +
                "        },\n" +
                "        \"extension\": [\n" +
                "          {\n" +
                "            \"url\": \"https://fhir.kbv.de/StructureDefinition/KBV_EX_ERP_Medication_Category\",\n" +
                "            \"valueCoding\": {\n" +
                "              \"system\": \"https://fhir.kbv.de/CodeSystem/KBV_CS_ERP_Medication_Category\",\n" +
                "              \"code\": \"00\"\n" +
                "            }\n" +
                "          },\n" +
                "          {\n" +
                "            \"url\": \"https://fhir.kbv.de/StructureDefinition/KBV_EX_ERP_Medication_Vaccine\",\n" +
                "            \"valueBoolean\": false\n" +
                "          },\n" +
                "          {\n" +
                "            \"url\": \"http://fhir.de/StructureDefinition/normgroesse\",\n" +
                "            \"valueCode\": \"N1\"\n" +
                "          }\n" +
                "        ],\n" +
                "        \"code\": {\n" +
                "          \"coding\": [\n" +
                "            {\n" +
                "              \"system\": \"http://fhir.de/CodeSystem/ifa/pzn\",\n" +
                "              \"code\": \"00027950\"\n" +
                "            },\n" +
                "            {\n" +
                "              \"system\": \"http://fhir.de/CodeSystem/ifa/pzn\",\n" +
                "              \"code\": \"09667510\"\n" +
                "            },\n" +
                "            {\n" +
                "              \"system\": \"http://fhir.de/CodeSystem/ifa/pzn\",\n" +
                "              \"code\": \"04751588\"\n" +
                "            }\n" +
                "          ],\n" +
                "          \"text\": \"Amoxicillin 1.000 mg\"\n" +
                "        },\n" +
                "        \"form\": {\n" +
                "          \"coding\": [\n" +
                "            {\n" +
                "              \"system\": \"https://fhir.kbv.de/CodeSystem/KBV_CS_SFHIR_KBV_DARREICHUNGSFORM\",\n" +
                "              \"code\": \"FLE\"\n" +
                "            }\n" +
                "          ]\n" +
                "        }\n" +
                "      }\n" +
                "    },\n" +
                "    {\n" +
                "      \"fullUrl\": \"http://pvs.praxis.local/fhir/Patient/null\",\n" +
                "      \"resource\": {\n" +
                "        \"resourceType\": \"Patient\",\n" +
                "        \"meta\": {\n" +
                "          \"profile\": [\n" +
                "            \"https://fhir.kbv.de/StructureDefinition/KBV_PR_FOR_Patient|1.0.3\"\n" +
                "          ]\n" +
                "        },\n" +
                "        \"identifier\": [\n" +
                "          {\n" +
                "            \"type\": {\n" +
                "              \"coding\": [\n" +
                "                {\n" +
                "                  \"system\": \"http://fhir.de/CodeSystem/identifier-type-de-basis\",\n" +
                "                  \"code\": \"GKV\"\n" +
                "                }\n" +
                "              ]\n" +
                "            },\n" +
                "            \"system\": \"http://fhir.de/NamingSystem/gkv/kvid-10\"\n" +
                "          }\n" +
                "        ],\n" +
                "        \"name\": [\n" +
                "          {\n" +
                "            \"use\": \"official\",\n" +
                "            \"family\": \"Heckner\",\n" +
                "            \"given\": [\n" +
                "              \"Markus\"\n" +
                "            ]\n" +
                "          }\n" +
                "        ],\n" +
                "        \"address\": [\n" +
                "          {\n" +
                "            \"type\": \"both\",\n" +
                "            \"line\": [\n" +
                "              \"Berliner Str. 12\"\n" +
                "            ],\n" +
                "            \"city\": \"Teltow\",\n" +
                "            \"postalCode\": \"14513\",\n" +
                "            \"_line\": [\n" +
                "              {\n" +
                "                \"extension\": null\n" +
                "              }\n" +
                "            ]\n" +
                "          }\n" +
                "        ],\n" +
                "        \"birthDate\": \"14/02/1976\"\n" +
                "      }\n" +
                "    },\n" +
                "    {\n" +
                "      \"fullUrl\": \"http://pvs.praxis.local/fhir/Practitioner/30000000\",\n" +
                "      \"resource\": {\n" +
                "        \"resourceType\": \"Practitioner\",\n" +
                "        \"id\": \"30000000\",\n" +
                "        \"meta\": {\n" +
                "          \"profile\": [\n" +
                "            \"https://fhir.kbv.de/StructureDefinition/KBV_PR_FOR_Practitioner|1.0.3\"\n" +
                "          ]\n" +
                "        },\n" +
                "        \"identifier\": [\n" +
                "          {\n" +
                "            \"type\": {\n" +
                "              \"coding\": [\n" +
                "                {\n" +
                "                  \"system\": \"http://terminology.hl7.org/CodeSystem/v2-0203\",\n" +
                "                  \"code\": \"LANR\"\n" +
                "                }\n" +
                "              ]\n" +
                "            },\n" +
                "            \"system\": \"https://fhir.kbv.de/NamingSystem/KBV_NS_Base_ANR\",\n" +
                "            \"value\": \"30000000\"\n" +
                "          }\n" +
                "        ],\n" +
                "        \"name\": [\n" +
                "          {\n" +
                "            \"extension\": [\n" +
                "              {\n" +
                "                \"url\": \"http://hl7.org/fhir/StructureDefinition/iso21090-EN-qualifier\",\n" +
                "                \"valueString\": \"AC\"\n" +
                "              }\n" +
                "            ],\n" +
                "            \"use\": \"official\",\n" +
                "            \"family\": \"Doctor Last Name\",\n" +
                "            \"given\": [\n" +
                "              \"Doctor First Name\"\n" +
                "            ]\n" +
                "          }\n" +
                "        ],\n" +
                "        \"qualification\": [\n" +
                "          {\n" +
                "            \"code\": {\n" +
                "              \"coding\": [\n" +
                "                {\n" +
                "                  \"system\": \"https://fhir.kbv.de/CodeSystem/KBV_CS_FOR_Qualification_Type\",\n" +
                "                  \"code\": \"00\",\n" +
                "                  \"display\": \"Arzt-Hausarzt\"\n" +
                "                }\n" +
                "              ]\n" +
                "            }\n" +
                "          },\n" +
                "          {\n" +
                "            \"code\": {\n" +
                "              \"text\": \"Arzt-Hausarzt\"\n" +
                "            }\n" +
                "          }\n" +
                "        ]\n" +
                "      }\n" +
                "    },\n" +
                "    {\n" +
                "      \"fullUrl\": \"http://pvs.praxis.local/fhir/Organization/30000000\",\n" +
                "      \"resource\": {\n" +
                "        \"resourceType\": \"Organization\",\n" +
                "        \"id\": \"30000000\",\n" +
                "        \"meta\": {\n" +
                "          \"profile\": [\n" +
                "            \"https://fhir.kbv.de/StructureDefinition/KBV_PR_FOR_Organization|1.0.3\",\n" +
                "            \"https://fhir.kbv.de/StructureDefinition/KBV_PR_FOR_Coverage|1.0.3\"\n" +
                "          ]\n" +
                "        },\n" +
                "        \"identifier\": [\n" +
                "          {\n" +
                "            \"type\": {\n" +
                "              \"coding\": [\n" +
                "                {\n" +
                "                  \"system\": \"http://terminology.hl7.org/CodeSystem/v2-0203\",\n" +
                "                  \"code\": \"BSNR\"\n" +
                "                }\n" +
                "              ]\n" +
                "            },\n" +
                "            \"system\": \"https://fhir.kbv.de/NamingSystem/KBV_NS_Base_BSNR\",\n" +
                "            \"value\": \"30000000\"\n" +
                "          }\n" +
                "        ],\n" +
                "        \"name\": \"null Doctor First Name Doctor Last Name\",\n" +
                "        \"telecom\": [\n" +
                "          {\n" +
                "            \"system\": \"phone\",\n" +
                "            \"value\": \"030/123456789\"\n" +
                "          }\n" +
                "        ],\n" +
                "        \"address\": [\n" +
                "          {\n" +
                "            \"type\": \"both\",\n" +
                "            \"line\": [\n" +
                "              \"Doctor Street Name Doctor Street Number\"\n" +
                "            ],\n" +
                "            \"city\": \"Doctor City\",\n" +
                "            \"postalCode\": \"012345\",\n" +
                "            \"country\": \"D\"\n" +
                "          }\n" +
                "        ]\n" +
                "      }\n" +
                "    },\n" +
                "    {\n" +
                "      \"fullUrl\": \"http://pvs.praxis.local/fhir/Coverage/null\",\n" +
                "      \"resource\": {\n" +
                "        \"resourceType\": \"Coverage\",\n" +
                "        \"meta\": {\n" +
                "          \"profile\": [\n" +
                "            \"https://fhir.kbv.de/StructureDefinition/KBV_PR_FOR_Coverage|1.0.3\"\n" +
                "          ]\n" +
                "        },\n" +
                "        \"extension\": [\n" +
                "          {\n" +
                "            \"url\": \"http://fhir.de/StructureDefinition/gkv/besondere-personengruppe\",\n" +
                "            \"valueCoding\": {\n" +
                "              \"system\": \"https://fhir.kbv.de/CodeSystem/KBV_CS_SFHIR_KBV_PERSONENGRUPPE\",\n" +
                "              \"code\": \"00\"\n" +
                "            }\n" +
                "          },\n" +
                "          {\n" +
                "            \"url\": \"http://fhir.de/StructureDefinition/gkv/dmp-kennzeichen\",\n" +
                "            \"valueCoding\": {\n" +
                "              \"system\": \"https://fhir.kbv.de/CodeSystem/KBV_CS_SFHIR_KBV_DMP\",\n" +
                "              \"code\": \"00\"\n" +
                "            }\n" +
                "          },\n" +
                "          {\n" +
                "            \"url\": \"http://fhir.de/StructureDefinition/gkv/wop\",\n" +
                "            \"valueCoding\": {\n" +
                "              \"system\": \"https://fhir.kbv.de/CodeSystem/KBV_CS_SFHIR_ITA_WOP\",\n" +
                "              \"code\": \"72\"\n" +
                "            }\n" +
                "          },\n" +
                "          {\n" +
                "            \"url\": \"https://fhir.kbv.de/CodeSystem/KBV_CS_SFHIR_KBV_VERSICHERTENSTATUS\",\n" +
                "            \"valueCoding\": {\n" +
                "              \"system\": \"https://fhir.kbv.de/CodeSystem/KBV_CS_SFHIR_ITA_WOP\",\n" +
                "              \"code\": \"3\"\n" +
                "            }\n" +
                "          }\n" +
                "        ],\n" +
                "        \"status\": \"active\",\n" +
                "        \"type\": {\n" +
                "          \"coding\": [\n" +
                "            {\n" +
                "              \"system\": \"http://fhir.de/CodeSystem/versicherungsart-de-basis\",\n" +
                "              \"code\": \"GKV\"\n" +
                "            }\n" +
                "          ]\n" +
                "        },\n" +
                "        \"beneficiary\": {\n" +
                "          \"reference\": \"Patient/\"\n" +
                "        },\n" +
                "        \"payor\": [\n" +
                "          {\n" +
                "            \"identifier\": {\n" +
                "              \"system\": \"http://fhir.de/NamingSystem/arge-ik/iknr\"\n" +
                "            },\n" +
                "            \"display\": \"DENS GmbH\"\n" +
                "          }\n" +
                "        ]\n" +
                "      }\n" +
                "    }\n" +
                "  ]\n" +
                "}";

        Bundle bundle = jsonParser.parseResource(Bundle.class, json);
    }

    @Test
    public void test_Successful_Validation_Of_An_FHIR_Patient_Resource()
            throws ParseException {
        Patient patientResource = prescriptionBundleBuilder.createPatientResource();

        prescriptionBundleValidator.setProfile(
                "https://fhir.kbv.de/StructureDefinition/KBV_PR_FOR_Patient");

        patientResource.getMeta().setProfile(Arrays.asList(
                new CanonicalType(
                        "https://fhir.kbv.de/StructureDefinition/KBV_PR_FOR_Patient")));

        ValidationResult validationResult =
                prescriptionBundleValidator.validateResource(patientResource, true);
        logger.info(validationResult.getMessages().stream().map(m -> m.getMessage()).collect(Collectors.joining("\n")));

        // Solutions for configuring HAPI validator can be found in a gematik presentation
        // https://gematik.atlassian.net/plugins/servlet/servicedesk/customer/confluence/shim/download/attachments/620855297/20210517%20-%20Sprechstunde%20eRP.pptx?version=1&modificationDate=1621431687594&cacheVersion=1&api=v2
        /*
        https://hapifhir.io/hapi-fhir/docs/tools/hapi_fhir_cli.html

        internalValidator = new FhirInstanceValidator(fhirContext);
        ValidationSupportChain support = new ValidationSupportChain(
                new DefaultProfileValidationSupport(fhirContext),
                new InMemoryTerminologyServerValidationSupport(fhirContext),
                new SnapshotGeneratingValidationSupport(fhirContext),
                new FhirSupport()
        );
        internalValidator.setValidationSupport(support);
        internalValidator.setNoTerminologyChecks(false);
        internalValidator.setAssumeValidRestReferences(false);
        internalValidator.setBestPracticeWarningLevel(IResourceValidator.BestPracticeWarningLevel.Hint);
        validator.registerValidatorModule(internalValidator);
        */
        // TODO: Next issue WARNING - Patient.identifier[0].type - None of the codes provided are in the value set http://hl7.org/fhir/ValueSet/identifier-type (http://hl7.org/fhir/ValueSet/identifier-type), and a code should come from this value set unless it has no suitable code and the validator cannot judge what is suitable) (codes = http://fhir.de/CodeSystem/identifier-type-de-basis#GKV)
        // TODO: Next issue ERROR - Patient.meta.profile[0] - Profile reference 'https://fhir.kbv.de/StructureDefinition/KBV_PR_FOR_Patient|1.0.3' has not been checked because it is unknown
        // TODO: None of the codes provided are in the value set http://hl7.org/fhir/ValueSet/identifier-type (http://hl7.org/fhir/ValueSet/identifier-type), and a code should come from this value set unless it has no suitable code and the validator cannot judge what is suitable) (codes = http://fhir.de/CodeSystem/identifier-type-de-basis#GKV)
        // TODO: Profile reference 'https://fhir.kbv.de/StructureDefinition/KBV_PR_FOR_Patient|1.0.3' has not been checked because it is unknown


         assertTrue(validationResult.isSuccessful());
    }

    @Disabled
    @Test
    public void test_Validation_Failure_Of_FHIR_Patient_Resource_With_Missing_Content() {
        Patient patient = new Patient();

        ValidationResult validationResult =
                prescriptionBundleValidator.validateResource(patient, true);
        assertFalse(validationResult.isSuccessful());
    }

    @Disabled
    @Test
    public void test_Successful_Validation_Of_An_FHIR_Coverage_Resource()
            throws ParseException {
        Coverage coverageResource = prescriptionBundleBuilder.createCoverageResource();

        ValidationResult validationResult =
                prescriptionBundleValidator.validateResource(coverageResource, true);
        assertTrue(validationResult.isSuccessful());
    }

    @Disabled
    @Test
    public void test_Successful_Validation_Of_XML_Serialization_Of_FHIR_EPrescription_Bundle_Object()
            throws ParseException {
        Bundle prescriptionBundle = prescriptionBundleBuilder.createBundle();

        ValidationResult validationResult =
                prescriptionBundleValidator.validateResource(prescriptionBundle, true);
        assertTrue(validationResult.isSuccessful());
    }
}
