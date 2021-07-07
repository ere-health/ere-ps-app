package health.ere.ps.service.fhir.bundle;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.ListUtils;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.hl7.fhir.instance.model.api.IBaseResource;
import org.hl7.fhir.r4.model.Bundle;
import org.hl7.fhir.r4.model.Coverage;
import org.hl7.fhir.r4.model.Patient;
import org.jboss.logging.Logger;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringReader;
import java.nio.charset.Charset;
import java.text.ParseException;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.inject.Inject;
import javax.xml.stream.XMLStreamException;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.parser.IParser;
import ca.uhn.fhir.validation.ValidationResult;
import health.ere.ps.model.muster16.MedicationString;
import health.ere.ps.model.muster16.Muster16PrescriptionForm;
import health.ere.ps.service.extractor.SVGExtractor;
import health.ere.ps.service.extractor.TemplateProfile;
import health.ere.ps.service.muster16.Muster16FormDataExtractorService;
import health.ere.ps.service.muster16.parser.rgxer.Muster16SvgRegexParser;
import health.ere.ps.validation.fhir.bundle.PrescriptionBundleValidator;
import io.quarkus.test.junit.QuarkusTest;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

@QuarkusTest
public class PrescriptionBundlesBuilderTest {
    @Inject
    Logger logger;

    @Inject
    PrescriptionBundleValidator prescriptionBundleValidator;

    private static final String BAD_DENS_SIGN_REQUEST_KBV_JSON = " {\"resourceType\":\"Bundle\"," +
            "\"id\":\"e6baf9c0-5d88-4b28-b15d-1c3a2c3f3d19\",\"meta\":{\"lastUpdated\":\"2021-06-16T13:05:38.948-04:00\",\"profile\":[\"https://fhir.kbv.de/StructureDefinition/KBV_PR_ERP_Bundle|1.0.1\"]},\"type\":\"document\",\"timestamp\":\"2021-06-16T13:05:38.948-04:00\",\"entry\":[{\"fullUrl\":\"http://pvs.praxis.local/fhir/Medication/9d8c5ab9-73b8-4165-9f3a-9eb354ea1f88\",\"resource\":{\"resourceType\":\"Medication\",\"id\":\"9d8c5ab9-73b8-4165-9f3a-9eb354ea1f88\",\"meta\":{\"profile\":[\"https://fhir.kbv.de/StructureDefinition/KBV_PR_ERP_Medication_PZN|1.0.1\"]},\"extension\":[{\"url\":\"https://fhir.kbv.de/StructureDefinition/KBV_EX_ERP_Medication_Category\",\"valueCoding\":{\"system\":\"https://fhir.kbv.de/CodeSystem/KBV_CS_ERP_Medication_Category\",\"code\":\"00\"}},{\"url\":\"https://fhir.kbv.de/StructureDefinition/KBV_EX_ERP_Medication_Vaccine\",\"valueBoolean\":false},{\"url\":\"http://fhir.de/StructureDefinition/normgroesse\",\"valueCode\":\"N1\"}],\"code\":{\"coding\":[{\"system\":\"http://fhir.de/CodeSystem/ifa/pzn\",\"code\":\"00027950\"}],\"text\":\"Ibuprofen 600mg\"},\"form\":{\"coding\":[{\"system\":\"https://fhir.kbv.de/CodeSystem/KBV_CS_SFHIR_KBV_DARREICHUNGSFORM\",\"code\":\"FLE\"}]}}},{\"fullUrl\":\"http://pvs.praxis.local/fhir/MedicationRequest/028df042-2321-410c-9fa0-148af5d2b909\",\"resource\":{\"resourceType\":\"MedicationRequest\",\"id\":\"028df042-2321-410c-9fa0-148af5d2b909\",\"meta\":{\"lastUpdated\":\"2021-06-16T13:05:38.948-04:00\",\"profile\":[\"https://fhir.kbv.de/StructureDefinition/KBV_PR_ERP_Prescription|1.0.1\"]},\"extension\":[{\"url\":\"https://fhir.kbv.de/StructureDefinition/KBV_EX_ERP_StatusCoPayment\",\"valueCoding\":{\"system\":\"https://fhir.kbv.de/CodeSystem/KBV_CS_ERP_StatusCoPayment\",\"code\":\"1\"}},{\"url\":\"https://fhir.kbv.de/StructureDefinition/KBV_EX_ERP_EmergencyServicesFee\",\"valueBoolean\":false},{\"url\":\"https://fhir.kbv.de/StructureDefinition/KBV_EX_ERP_BVG\",\"valueBoolean\":false},{\"url\":\"https://fhir.kbv.de/StructureDefinition/KBV_EX_ERP_Multiple_Prescription\",\"extension\":[{\"url\":\"Kennzeichen\",\"valueBoolean\":false}]}],\"status\":\"active\",\"intent\":\"order\",\"medicationReference\":{\"reference\":\"Medication/9d8c5ab9-73b8-4165-9f3a-9eb354ea1f88\"},\"subject\":{\"reference\":\"Patient/\"},\"requester\":{\"reference\":\"Practitioner/30000000\"},\"insurance\":[{\"reference\":\"Coverage/\"}],\"dosageInstruction\":[{\"extension\":[{\"url\":\"https://fhir.kbv.de/StructureDefinition/KBV_EX_ERP_DosageFlag\",\"valueBoolean\":true}],\"text\":\"1-1-1\"}],\"dispenseRequest\":{\"quantity\":{\"value\":1,\"system\":\"http://unitsofmeasure.org\",\"code\":\"{Package}\"}},\"substitution\":{\"allowedBoolean\":true}}},{\"fullUrl\":\"http://pvs.praxis.local/fhir/Composition/ba4fc629-93ce-4670-b47a-b0596bc0aaa6\",\"resource\":{\"resourceType\":\"Composition\",\"id\":\"ba4fc629-93ce-4670-b47a-b0596bc0aaa6\",\"meta\":{\"profile\":[\"https://fhir.kbv.de/StructureDefinition/KBV_PR_ERP_Composition|1.0.1\"]},\"extension\":[{\"url\":\"https://fhir.kbv.de/StructureDefinition/KBV_EX_FOR_Legal_basis\",\"valueCoding\":{\"system\":\"https://fhir.kbv.de/CodeSystem/KBV_CS_SFHIR_KBV_STATUSKENNZEICHEN\",\"code\":\"04\"}}],\"status\":\"final\",\"type\":{\"coding\":[{\"system\":\"https://fhir.kbv.de/CodeSystem/KBV_CS_SFHIR_KBV_FORMULAR_ART\",\"code\":\"e16A\"}]},\"subject\":{\"reference\":\"Patient/\"},\"date\":\"2021-06-16T13:05:38-04:00\",\"author\":[{\"reference\":\"Practitioner/30000000\",\"type\":\"Practitioner\"},{\"type\":\"Device\",\"identifier\":{\"system\":\"https://fhir.kbv.de/NamingSystem/KBV_NS_FOR_Pruefnummer\",\"value\":\"123456\"}}],\"title\":\"elektronische Arzneimittelverordnung\",\"attester\":[{\"mode\":\"legal\",\"party\":{\"reference\":\"Practitioner/30000000\"}}],\"custodian\":{\"reference\":\"Organization/30000000\"},\"section\":[{\"code\":{\"coding\":[{\"system\":\"https://fhir.kbv.de/CodeSystem/KBV_CS_ERP_Section_Type\",\"code\":\"Prescription\"}]},\"entry\":[{\"reference\":\"MedicationRequest/028df042-2321-410c-9fa0-148af5d2b909\"}]},{\"code\":{\"coding\":[{\"system\":\"https://fhir.kbv.de/CodeSystem/KBV_CS_ERP_Section_Type\",\"code\":\"Coverage\"}]},\"entry\":[{\"reference\":\"Coverage/\"}]}]}},{\"fullUrl\":\"http://pvs.praxis.local/fhir/Patient/null\",\"resource\":{\"resourceType\":\"Patient\",\"meta\":{\"profile\":[\"https://fhir.kbv.de/StructureDefinition/KBV_PR_FOR_Patient|1.0.3\"]},\"identifier\":[{\"type\":{\"coding\":[{\"system\":\"http://fhir.de/CodeSystem/identifier-type-de-basis\",\"code\":\"GKV\"}]},\"system\":\"http://fhir.de/NamingSystem/gkv/kvid-10\"}],\"name\":[{\"use\":\"official\",\"family\":\"Heckner\",\"given\":[\"Markus\"],\"prefix\":[\"Dr.\"]}],\"address\":[{\"type\":\"both\",\"line\":[\"Berliner Str. 12\"],\"city\":\"Teltow\",\"postalCode\":\"14513\",\"country\":\"D\",\"_line\":[{\"extension\":null}]}]}},{\"fullUrl\":\"http://pvs.praxis.local/fhir/Practitioner/30000000\",\"resource\":{\"resourceType\":\"Practitioner\",\"id\":\"30000000\",\"meta\":{\"profile\":[\"https://fhir.kbv.de/StructureDefinition/KBV_PR_FOR_Practitioner|1.0.3\"]},\"identifier\":[{\"type\":{\"coding\":[{\"system\":\"http://terminology.hl7.org/CodeSystem/v2-0203\",\"code\":\"LANR\"}]},\"system\":\"https://fhir.kbv.de/NamingSystem/KBV_NS_Base_ANR\",\"value\":\"30000000\"}],\"name\":[{\"extension\":[{\"url\":\"http://hl7.org/fhir/StructureDefinition/iso21090-EN-qualifier\",\"valueString\":\"AC\"}],\"use\":\"official\",\"family\":\"Doctor Last Name\",\"given\":[\"Doctor First Name\"]}],\"qualification\":[{\"code\":{\"coding\":[{\"system\":\"https://fhir.kbv.de/CodeSystem/KBV_CS_FOR_Qualification_Type\",\"code\":\"00\",\"display\":\"Arzt-Hausarzt\"}]}},{\"code\":{\"text\":\"Arzt-Hausarzt\"}}]}},{\"fullUrl\":\"http://pvs.praxis.local/fhir/Organization/30000000\",\"resource\":{\"resourceType\":\"Organization\",\"id\":\"30000000\",\"meta\":{\"profile\":[\"https://fhir.kbv.de/StructureDefinition/KBV_PR_FOR_Organization|1.0.3\",\"https://fhir.kbv.de/StructureDefinition/KBV_PR_FOR_Coverage|1.0.3\"]},\"identifier\":[{\"type\":{\"coding\":[{\"system\":\"http://terminology.hl7.org/CodeSystem/v2-0203\",\"code\":\"BSNR\"}]},\"system\":\"https://fhir.kbv.de/NamingSystem/KBV_NS_Base_BSNR\",\"value\":\"30000000\"}],\"name\":\"null Doctor First Name Doctor Last Name\",\"telecom\":[{\"system\":\"phone\",\"value\":\"030/123456789\"}],\"address\":[{\"type\":\"both\",\"line\":[\"Doctor Street Name Doctor Street Number\"],\"city\":\"Doctor City\",\"postalCode\":\"012345\",\"country\":\"D\"}]}},{\"fullUrl\":\"http://pvs.praxis.local/fhir/Coverage/null\",\"resource\":{\"resourceType\":\"Coverage\",\"meta\":{\"profile\":[\"https://fhir.kbv.de/StructureDefinition/KBV_PR_FOR_Coverage|1.0.3\"]},\"extension\":[{\"url\":\"http://fhir.de/StructureDefinition/gkv/besondere-personengruppe\",\"valueCoding\":{\"system\":\"https://fhir.kbv.de/CodeSystem/KBV_CS_SFHIR_KBV_PERSONENGRUPPE\",\"code\":\"00\"}},{\"url\":\"http://fhir.de/StructureDefinition/gkv/dmp-kennzeichen\",\"valueCoding\":{\"system\":\"https://fhir.kbv.de/CodeSystem/KBV_CS_SFHIR_KBV_DMP\",\"code\":\"00\"}},{\"url\":\"http://fhir.de/StructureDefinition/gkv/wop\",\"valueCoding\":{\"system\":\"https://fhir.kbv.de/CodeSystem/KBV_CS_SFHIR_ITA_WOP\",\"code\":\"72\"}},{\"url\":\"https://fhir.kbv.de/CodeSystem/KBV_CS_SFHIR_KBV_VERSICHERTENSTATUS\",\"valueCoding\":{\"system\":\"https://fhir.kbv.de/CodeSystem/KBV_CS_SFHIR_ITA_WOP\",\"code\":\"3\"}}],\"status\":\"active\",\"type\":{\"coding\":[{\"system\":\"http://fhir.de/CodeSystem/versicherungsart-de-basis\",\"code\":\"GKV\"}]},\"beneficiary\":{\"reference\":\"Patient/\"},\"payor\":[{\"identifier\":{\"system\":\"http://fhir.de/NamingSystem/arge-ik/iknr\"},\"display\":\"DENS GmbH\"}]}}]}";
    private static final String GOOD_SIMPLIFIER_NET_SAMPLE_KBV_JSON = "{\"resourceType\":\"Bundle" +
            "\",\"id\":\"f70585e0-82f9-4d3d-b248-94504ccf6a66\",\"meta\":{\"lastUpdated\":\"2021-04-06T08:30:00Z\",\"profile\":[\"https://fhir.kbv.de/StructureDefinition/KBV_PR_ERP_Bundle|1.0.1\"]},\"identifier\":{\"system\":\"https://gematik.de/fhir/NamingSystem/PrescriptionID\",\"value\":\"160.100.000.000.016.91\"},\"type\":\"document\",\"timestamp\":\"2021-04-06T08:30:00Z\",\"entry\":[{\"fullUrl\":\"http://pvs.praxis.local/fhir/Composition/1868bb7c-c1a6-48a6-a327-05ff8d24c64a\",\"resource\":{\"resourceType\":\"Composition\",\"id\":\"1868bb7c-c1a6-48a6-a327-05ff8d24c64a\",\"meta\":{\"profile\":[\"https://fhir.kbv.de/StructureDefinition/KBV_PR_ERP_Composition|1.0.1\"]},\"extension\":[{\"url\":\"https://fhir.kbv.de/StructureDefinition/KBV_EX_FOR_Legal_basis\",\"valueCoding\":{\"system\":\"https://fhir.kbv.de/CodeSystem/KBV_CS_SFHIR_KBV_STATUSKENNZEICHEN\",\"code\":\"00\"}}],\"status\":\"final\",\"type\":{\"coding\":[{\"system\":\"https://fhir.kbv.de/CodeSystem/KBV_CS_SFHIR_KBV_FORMULAR_ART\",\"code\":\"e16A\"}]},\"subject\":{\"reference\":\"Patient/ce4104af-b86b-4664-afee-1b5fc3ac8acf\"},\"date\":\"2021-04-06T08:00:00Z\",\"author\":[{\"reference\":\"Practitioner/667ffd79-42a3-4002-b7ca-6b9098f20ccb\",\"type\":\"Practitioner\"},{\"type\":\"Device\",\"identifier\":{\"system\":\"https://fhir.kbv.de/NamingSystem/KBV_NS_FOR_Pruefnummer\",\"value\":\"Y/410/2107/36/999\"}}],\"title\":\"elektronische Arzneimittelverordnung\",\"custodian\":{\"reference\":\"Organization/5d3f4ac0-2b44-4d48-b363-e63efa72973b\"},\"section\":[{\"code\":{\"coding\":[{\"system\":\"https://fhir.kbv.de/CodeSystem/KBV_CS_ERP_Section_Type\",\"code\":\"Prescription\"}]},\"entry\":[{\"reference\":\"MedicationRequest/76b5767d-55a5-4233-8f85-e15a24a5193a\"}]},{\"code\":{\"coding\":[{\"system\":\"https://fhir.kbv.de/CodeSystem/KBV_CS_ERP_Section_Type\",\"code\":\"Coverage\"}]},\"entry\":[{\"reference\":\"Coverage/da80211e-61ee-458e-a651-87370b6ec30c\"}]}]}},{\"fullUrl\":\"http://pvs.praxis.local/fhir/MedicationRequest/76b5767d-55a5-4233-8f85-e15a24a5193a\",\"resource\":{\"resourceType\":\"MedicationRequest\",\"id\":\"76b5767d-55a5-4233-8f85-e15a24a5193a\",\"meta\":{\"profile\":[\"https://fhir.kbv.de/StructureDefinition/KBV_PR_ERP_Prescription|1.0.1\"]},\"extension\":[{\"url\":\"https://fhir.kbv.de/StructureDefinition/KBV_EX_ERP_StatusCoPayment\",\"valueCoding\":{\"system\":\"https://fhir.kbv.de/CodeSystem/KBV_CS_ERP_StatusCoPayment\",\"code\":\"0\"}},{\"url\":\"https://fhir.kbv.de/StructureDefinition/KBV_EX_ERP_EmergencyServicesFee\",\"valueBoolean\":false},{\"url\":\"https://fhir.kbv.de/StructureDefinition/KBV_EX_ERP_BVG\",\"valueBoolean\":false},{\"url\":\"https://fhir.kbv.de/StructureDefinition/KBV_EX_ERP_Multiple_Prescription\",\"extension\":[{\"url\":\"Kennzeichen\",\"valueBoolean\":true},{\"url\":\"Nummerierung\",\"valueRatio\":{\"numerator\":{\"value\":3},\"denominator\":{\"value\":4}}},{\"url\":\"Zeitraum\",\"valuePeriod\":{\"start\":\"2021-09-15\",\"end\":\"2021-12-31\"}}]}],\"status\":\"active\",\"intent\":\"order\",\"medicationReference\":{\"reference\":\"Medication/07c10a67-2ece-4d5d-9394-633e07c9656d\"},\"subject\":{\"reference\":\"Patient/ce4104af-b86b-4664-afee-1b5fc3ac8acf\"},\"authoredOn\":\"2021-04-01\",\"requester\":{\"reference\":\"Practitioner/667ffd79-42a3-4002-b7ca-6b9098f20ccb\"},\"insurance\":[{\"reference\":\"Coverage/da80211e-61ee-458e-a651-87370b6ec30c\"}],\"dosageInstruction\":[{\"extension\":[{\"url\":\"https://fhir.kbv.de/StructureDefinition/KBV_EX_ERP_DosageFlag\",\"valueBoolean\":false}]}],\"dispenseRequest\":{\"quantity\":{\"value\":1,\"system\":\"http://unitsofmeasure.org\",\"code\":\"{Package}\"}},\"substitution\":{\"allowedBoolean\":false}}},{\"fullUrl\":\"http://pvs.praxis.local/fhir/Medication/07c10a67-2ece-4d5d-9394-633e07c9656d\",\"resource\":{\"resourceType\":\"Medication\",\"id\":\"07c10a67-2ece-4d5d-9394-633e07c9656d\",\"meta\":{\"profile\":[\"https://fhir.kbv.de/StructureDefinition/KBV_PR_ERP_Medication_PZN|1.0.1\"]},\"extension\":[{\"url\":\"https://fhir.kbv.de/StructureDefinition/KBV_EX_ERP_Medication_Category\",\"valueCoding\":{\"system\":\"https://fhir.kbv.de/CodeSystem/KBV_CS_ERP_Medication_Category\",\"code\":\"00\"}},{\"url\":\"https://fhir.kbv.de/StructureDefinition/KBV_EX_ERP_Medication_Vaccine\",\"valueBoolean\":false},{\"url\":\"http://fhir.de/StructureDefinition/normgroesse\",\"valueCode\":\"N3\"}],\"code\":{\"coding\":[{\"system\":\"http://fhir.de/CodeSystem/ifa/pzn\",\"code\":\"02532741\"}],\"text\":\"L-Thyroxin Henning 75 100 Tbl. N3\"},\"form\":{\"coding\":[{\"system\":\"https://fhir.kbv.de/CodeSystem/KBV_CS_SFHIR_KBV_DARREICHUNGSFORM\",\"code\":\"TAB\"}]}}},{\"fullUrl\":\"http://pvs.praxis.local/fhir/Patient/ce4104af-b86b-4664-afee-1b5fc3ac8acf\",\"resource\":{\"resourceType\":\"Patient\",\"id\":\"ce4104af-b86b-4664-afee-1b5fc3ac8acf\",\"meta\":{\"profile\":[\"https://fhir.kbv.de/StructureDefinition/KBV_PR_FOR_Patient|1.0.3\"]},\"identifier\":[{\"type\":{\"coding\":[{\"system\":\"http://fhir.de/CodeSystem/identifier-type-de-basis\",\"code\":\"GKV\"}]},\"system\":\"http://fhir.de/sid/gkv/kvid-10\",\"value\":\"K030182229\"}],\"name\":[{\"use\":\"official\",\"family\":\"Kluge\",\"_family\":{\"extension\":[{\"url\":\"http://hl7.org/fhir/StructureDefinition/humanname-own-name\",\"valueString\":\"Kluge\"}]},\"given\":[\"Eva\"],\"prefix\":[\"Prof. Dr. Dr. med\"],\"_prefix\":[{\"extension\":[{\"url\":\"http://hl7.org/fhir/StructureDefinition/iso21090-EN-qualifier\",\"valueCode\":\"AC\"}]}]}],\"birthDate\":\"1982-01-03\",\"address\":[{\"type\":\"both\",\"line\":[\"Pflasterhofweg 111B\"],\"_line\":[{\"extension\":[{\"url\":\"http://hl7.org/fhir/StructureDefinition/iso21090-ADXP-houseNumber\",\"valueString\":\"111B\"},{\"url\":\"http://hl7.org/fhir/StructureDefinition/iso21090-ADXP-streetName\",\"valueString\":\"Pflasterhofweg\"}]}],\"city\":\"Köln\",\"postalCode\":\"50999\",\"country\":\"D\"}]}},{\"fullUrl\":\"http://pvs.praxis.local/fhir/Practitioner/667ffd79-42a3-4002-b7ca-6b9098f20ccb\",\"resource\":{\"resourceType\":\"Practitioner\",\"id\":\"667ffd79-42a3-4002-b7ca-6b9098f20ccb\",\"meta\":{\"profile\":[\"https://fhir.kbv.de/StructureDefinition/KBV_PR_FOR_Practitioner|1.0.3\"]},\"identifier\":[{\"type\":{\"coding\":[{\"system\":\"http://terminology.hl7.org/CodeSystem/v2-0203\",\"code\":\"LANR\"}]},\"system\":\"https://fhir.kbv.de/NamingSystem/KBV_NS_Base_ANR\",\"value\":\"987654423\"}],\"name\":[{\"use\":\"official\",\"family\":\"Schneider\",\"_family\":{\"extension\":[{\"url\":\"http://hl7.org/fhir/StructureDefinition/humanname-own-name\",\"valueString\":\"Schneider\"}]},\"given\":[\"Emma\"],\"prefix\":[\"Dr. med.\"],\"_prefix\":[{\"extension\":[{\"url\":\"http://hl7.org/fhir/StructureDefinition/iso21090-EN-qualifier\",\"valueCode\":\"AC\"}]}]}],\"qualification\":[{\"code\":{\"coding\":[{\"system\":\"https://fhir.kbv.de/CodeSystem/KBV_CS_FOR_Qualification_Type\",\"code\":\"00\"}]}},{\"code\":{\"text\":\"Fachärztin für Innere Medizin\"}}]}},{\"fullUrl\":\"http://pvs.praxis.local/fhir/Organization/5d3f4ac0-2b44-4d48-b363-e63efa72973b\",\"resource\":{\"resourceType\":\"Organization\",\"id\":\"5d3f4ac0-2b44-4d48-b363-e63efa72973b\",\"meta\":{\"profile\":[\"https://fhir.kbv.de/StructureDefinition/KBV_PR_FOR_Organization|1.0.3\"]},\"identifier\":[{\"type\":{\"coding\":[{\"system\":\"http://terminology.hl7.org/CodeSystem/v2-0203\",\"code\":\"BSNR\"}]},\"system\":\"https://fhir.kbv.de/NamingSystem/KBV_NS_Base_BSNR\",\"value\":\"721111100\"}],\"name\":\"MVZ\",\"telecom\":[{\"system\":\"phone\",\"value\":\"0301234567\"},{\"system\":\"fax\",\"value\":\"030123456789\"},{\"system\":\"email\",\"value\":\"mvz@e-mail.de\"}],\"address\":[{\"type\":\"both\",\"line\":[\"Herbert-Lewin-Platz 2\"],\"_line\":[{\"extension\":[{\"url\":\"http://hl7.org/fhir/StructureDefinition/iso21090-ADXP-houseNumber\",\"valueString\":\"2\"},{\"url\":\"http://hl7.org/fhir/StructureDefinition/iso21090-ADXP-streetName\",\"valueString\":\"Herbert-Lewin-Platz\"}]}],\"city\":\"Berlin\",\"postalCode\":\"10623\",\"country\":\"D\"}]}},{\"fullUrl\":\"http://pvs.praxis.local/fhir/Coverage/da80211e-61ee-458e-a651-87370b6ec30c\",\"resource\":{\"resourceType\":\"Coverage\",\"id\":\"da80211e-61ee-458e-a651-87370b6ec30c\",\"meta\":{\"profile\":[\"https://fhir.kbv.de/StructureDefinition/KBV_PR_FOR_Coverage|1.0.3\"]},\"extension\":[{\"url\":\"http://fhir.de/StructureDefinition/gkv/besondere-personengruppe\",\"valueCoding\":{\"system\":\"https://fhir.kbv.de/CodeSystem/KBV_CS_SFHIR_KBV_PERSONENGRUPPE\",\"code\":\"00\"}},{\"url\":\"http://fhir.de/StructureDefinition/gkv/dmp-kennzeichen\",\"valueCoding\":{\"system\":\"https://fhir.kbv.de/CodeSystem/KBV_CS_SFHIR_KBV_DMP\",\"code\":\"00\"}},{\"url\":\"http://fhir.de/StructureDefinition/gkv/wop\",\"valueCoding\":{\"system\":\"https://fhir.kbv.de/CodeSystem/KBV_CS_SFHIR_ITA_WOP\",\"code\":\"38\"}},{\"url\":\"http://fhir.de/StructureDefinition/gkv/versichertenart\",\"valueCoding\":{\"system\":\"https://fhir.kbv.de/CodeSystem/KBV_CS_SFHIR_KBV_VERSICHERTENSTATUS\",\"code\":\"3\"}}],\"status\":\"active\",\"type\":{\"coding\":[{\"system\":\"http://fhir.de/CodeSystem/versicherungsart-de-basis\",\"code\":\"GKV\"}]},\"beneficiary\":{\"reference\":\"Patient/ce4104af-b86b-4664-afee-1b5fc3ac8acf\"},\"payor\":[{\"identifier\":{\"system\":\"http://fhir.de/sid/arge-ik/iknr\",\"value\":\"109777509\"},\"display\":\"Techniker-Krankenkasse\"}]}}]}";
    private static final String GOOD_SIMPLIFIER_NET_SAMPLE_KBV_JSON_AS_A_TEMPLATE = "{\"resourceType\":\"Bundle\",\"id\":\"$BUNDLE_ID\"," +
            "\"meta\":{\"lastUpdated\":\"$LAST_UPDATED\",\"profile\":[\"https://fhir.kbv.de/StructureDefinition/KBV_PR_ERP_Bundle|1.0.1\"]},\"identifier\":{\"system\":\"https://gematik.de/fhir/NamingSystem/PrescriptionID\",\"value\":\"$PRESCRIPTION_ID\"},\"type\":\"document\",\"timestamp\":\"$TIMESTAMP\",\"entry\":[{\"fullUrl\":\"http://pvs.praxis.local/fhir/Composition/$COMPOSITION_ID\",\"resource\":{\"resourceType\":\"Composition\",\"id\":\"$COMPOSITION_ID\",\"meta\":{\"profile\":[\"https://fhir.kbv.de/StructureDefinition/KBV_PR_ERP_Composition|1.0.1\"]},\"extension\":[{\"url\":\"https://fhir.kbv.de/StructureDefinition/KBV_EX_FOR_Legal_basis\",\"valueCoding\":{\"system\":\"https://fhir.kbv.de/CodeSystem/KBV_CS_SFHIR_KBV_STATUSKENNZEICHEN\",\"code\":\"00\"}}],\"status\":\"final\",\"type\":{\"coding\":[{\"system\":\"https://fhir.kbv.de/CodeSystem/KBV_CS_SFHIR_KBV_FORMULAR_ART\",\"code\":\"e16A\"}]},\"subject\":{\"reference\":\"Patient/$PATIENT_ID\"},\"date\":\"$COMPOSITION_DATE\",\"author\":[{\"reference\":\"Practitioner/$PRACTITIONER_ID\",\"type\":\"Practitioner\"},{\"type\":\"Device\",\"identifier\":{\"system\":\"https://fhir.kbv.de/NamingSystem/KBV_NS_FOR_Pruefnummer\",\"value\":\"$DEVICE_ID\"}}],\"title\":\"elektronische Arzneimittelverordnung\",\"custodian\":{\"reference\":\"Organization/$ORGANIZATION_ID\"},\"section\":[{\"code\":{\"coding\":[{\"system\":\"https://fhir.kbv.de/CodeSystem/KBV_CS_ERP_Section_Type\",\"code\":\"Prescription\"}]},\"entry\":[{\"reference\":\"MedicationRequest/$MEDICATION_REQUEST_ID\"}]},{\"code\":{\"coding\":[{\"system\":\"https://fhir.kbv.de/CodeSystem/KBV_CS_ERP_Section_Type\",\"code\":\"Coverage\"}]},\"entry\":[{\"reference\":\"Coverage/$COVERAGE_ID\"}]}]}},{\"fullUrl\":\"http://pvs.praxis.local/fhir/MedicationRequest/$MEDICATION_REQUEST_ID\",\"resource\":{\"resourceType\":\"MedicationRequest\",\"id\":\"$MEDICATION_REQUEST_ID\",\"meta\":{\"profile\":[\"https://fhir.kbv.de/StructureDefinition/KBV_PR_ERP_Prescription|1.0.1\"]},\"extension\":[{\"url\":\"https://fhir.kbv.de/StructureDefinition/KBV_EX_ERP_StatusCoPayment\",\"valueCoding\":{\"system\":\"https://fhir.kbv.de/CodeSystem/KBV_CS_ERP_StatusCoPayment\",\"code\":\"$STATUS_CO_PAYMENT\"}},{\"url\":\"https://fhir.kbv.de/StructureDefinition/KBV_EX_ERP_EmergencyServicesFee\",\"valueBoolean\":false},{\"url\":\"https://fhir.kbv.de/StructureDefinition/KBV_EX_ERP_BVG\",\"valueBoolean\":false},{\"url\":\"https://fhir.kbv.de/StructureDefinition/KBV_EX_ERP_Multiple_Prescription\",\"extension\":[{\"url\":\"Kennzeichen\",\"valueBoolean\":false}]}],\"status\":\"active\",\"intent\":\"order\",\"medicationReference\":{\"reference\":\"Medication/$MEDICATION_ID\"},\"subject\":{\"reference\":\"Patient/$PATIENT_ID\"},\"authoredOn\":\"$AUTHORED_ON\",\"requester\":{\"reference\":\"Practitioner/$PRACTITIONER_ID\"},\"insurance\":[{\"reference\":\"Coverage/$COVERAGE_ID\"}],\"dosageInstruction\":[{\"extension\":[{\"url\":\"https://fhir.kbv.de/StructureDefinition/KBV_EX_ERP_DosageFlag\",\"valueBoolean\":true}],\"text\":\"$DOSAGE_QUANTITY\"}],\"dispenseRequest\":{\"quantity\":{\"value\":1,\"system\":\"http://unitsofmeasure.org\",\"code\":\"{Package}\"}},\"substitution\":{\"allowedBoolean\":true}}},{\"fullUrl\":\"http://pvs.praxis.local/fhir/Medication/$MEDICATION_ID\",\"resource\":{\"resourceType\":\"Medication\",\"id\":\"$MEDICATION_ID\",\"meta\":{\"profile\":[\"https://fhir.kbv.de/StructureDefinition/KBV_PR_ERP_Medication_PZN|1.0.1\"]},\"extension\":[{\"url\":\"https://fhir.kbv.de/StructureDefinition/KBV_EX_ERP_Medication_Category\",\"valueCoding\":{\"system\":\"https://fhir.kbv.de/CodeSystem/KBV_CS_ERP_Medication_Category\",\"code\":\"00\"}},{\"url\":\"https://fhir.kbv.de/StructureDefinition/KBV_EX_ERP_Medication_Vaccine\",\"valueBoolean\":false},{\"url\":\"http://fhir.de/StructureDefinition/normgroesse\",\"valueCode\":\"N1\"}],\"code\":{\"coding\":[{\"system\":\"http://fhir.de/CodeSystem/ifa/pzn\",\"code\":\"$PZN\"}],\"text\":\"$MEDICATION_NAME\"},\"form\":{\"coding\":[{\"system\":\"https://fhir.kbv.de/CodeSystem/KBV_CS_SFHIR_KBV_DARREICHUNGSFORM\",\"code\":\"FLE\"}]}}},{\"fullUrl\":\"http://pvs.praxis.local/fhir/Patient/$PATIENT_ID\",\"resource\":{\"resourceType\":\"Patient\",\"id\":\"$PATIENT_ID\",\"meta\":{\"profile\":[\"https://fhir.kbv.de/StructureDefinition/KBV_PR_FOR_Patient|1.0.3\"]},\"identifier\":[{\"type\":{\"coding\":[{\"system\":\"http://fhir.de/CodeSystem/identifier-type-de-basis\",\"code\":\"GKV\"}]},\"system\":\"http://fhir.de/NamingSystem/gkv/kvid-10\",\"value\":\"$KVID_10\"}],\"name\":[{\"use\":\"official\",\"family\":\"$PATIENT_NAME_FAMILY\",\"given\":[\"$PATIENT_NAME_FIRST\"],\"prefix\":[\"$PATIENT_NAME_PREFIX\"]}],\"birthDate\":\"$PATIENT_BIRTH_DATE\",\"address\":[{\"type\":\"both\",\"line\":[\"$PATIENT_ADDRESS_STREET_NUMBER $PATIENT_ADDRESS_STREET_NAME\"],\"_line\":[{\"extension\":[{\"url\":\"http://hl7.org/fhir/StructureDefinition/iso90-ADXP-houseNumber\",\"valueString\":\"$PATIENT_ADDRESS_STREET_NUMBER\"},{\"url\":\"http://hl7.org/fhir/StructureDefinition/iso90-ADXP-streetName\",\"valueString\":\"$PATIENT_ADDRESS_STREET_NAME\"}]}],\"city\":\"$PATIENT_ADDRESS_CITY\",\"postalCode\":\"$PATIENT_ADDRESS_POSTAL_CODE\",\"country\":\"D\"}]}},{\"fullUrl\":\"http://pvs.praxis.local/fhir/Practitioner/$PRACTITIONER_ID\",\"resource\":{\"resourceType\":\"Practitioner\",\"id\":\"$PRACTITIONER_ID\",\"meta\":{\"profile\":[\"https://fhir.kbv.de/StructureDefinition/KBV_PR_FOR_Practitioner|1.0.3\"]},\"identifier\":[{\"type\":{\"coding\":[{\"system\":\"http://terminology.hl7.org/CodeSystem/v2-3\",\"code\":\"LANR\"}]},\"system\":\"https://fhir.kbv.de/NamingSystem/KBV_NS_Base_ANR\",\"value\":\"\"}],\"name\":[{\"use\":\"official\",\"family\":\"$PRACTITIONER_NAME_FAMILY\",\"_family\":{\"extension\":[{\"url\":\"http://hl7.org/fhir/StructureDefinition/humanname-own-name\",\"valueString\":\"$PRACTITIONER_NAME_FAMILY\"}]},\"given\":[\"$PRACTITIONER_NAME_FIRST\"],\"prefix\":[\"$PRACTITIONER_NAME_PREFIX\"],\"_prefix\":[{\"extension\":[{\"url\":\"http://hl7.org/fhir/StructureDefinition/iso90-EN-qualifier\",\"valueCode\":\"AC\"}]}]}],\"qualification\":[{\"code\":{\"coding\":[{\"system\":\"https://fhir.kbv.de/CodeSystem/KBV_CS_FOR_Qualification_Type\",\"code\":\"00\"}]}},{\"code\":{\"text\":\"Arzt-Hausarzt\"}}]}},{\"fullUrl\":\"http://pvs.praxis.local/fhir/Organization/$ORGANIZATION_ID\",\"resource\":{\"resourceType\":\"Organization\",\"id\":\"$ORGANIZATION_ID\",\"meta\":{\"profile\":[\"https://fhir.kbv.de/StructureDefinition/KBV_PR_FOR_Organization|1.0.3\"]},\"identifier\":[{\"type\":{\"coding\":[{\"system\":\"http://terminology.hl7.org/CodeSystem/v2-3\",\"code\":\"BSNR\"}]},\"system\":\"https://fhir.kbv.de/NamingSystem/KBV_NS_Base_BSNR\",\"value\":\"$CLINIC_ID\"}],\"name\":\"Kinderarztpraxis\",\"telecom\":[{\"system\":\"phone\",\"value\":\"$PRACTITIONER_PHONE\"},{\"system\":\"fax\",\"value\":\"$PRACTITIONER_FAX\"}],\"address\":[{\"type\":\"both\",\"line\":[\"$PRACTITIONER_ADDRESS_STREET_NAME $PRACTITIONER_ADDRESS_STREET_NUMBER\"],\"_line\":[{\"extension\":[{\"url\":\"http://hl7.org/fhir/StructureDefinition/iso90-ADXP-houseNumber\",\"valueString\":\"$PRACTITIONER_ADDRESS_STREET_NUMBER\"},{\"url\":\"http://hl7.org/fhir/StructureDefinition/iso90-ADXP-streetName\",\"valueString\":\"$PRACTITIONER_ADDRESS_STREET_NAME\"}]}],\"city\":\"$PRACTITIONER_ADDRESS_CITY\",\"postalCode\":\"$PRACTITIONER_ADDRESS_POSTAL_CODE\",\"country\":\"D\"}]}},{\"fullUrl\":\"http://pvs.praxis.local/fhir/Coverage/$COVERAGE_ID\",\"resource\":{\"resourceType\":\"Coverage\",\"id\":\"$COVERAGE_ID\",\"meta\":{\"profile\":[\"https://fhir.kbv.de/StructureDefinition/KBV_PR_FOR_Coverage|1.0.3\"]},\"extension\":[{\"url\":\"http://fhir.de/StructureDefinition/gkv/besondere-personengruppe\",\"valueCoding\":{\"system\":\"https://fhir.kbv.de/CodeSystem/KBV_CS_SFHIR_KBV_PERSONENGRUPPE\",\"code\":\"00\"}},{\"url\":\"http://fhir.de/StructureDefinition/gkv/dmp-kennzeichen\",\"valueCoding\":{\"system\":\"https://fhir.kbv.de/CodeSystem/KBV_CS_SFHIR_KBV_DMP\",\"code\":\"00\"}},{\"url\":\"http://fhir.de/StructureDefinition/gkv/wop\",\"valueCoding\":{\"system\":\"https://fhir.kbv.de/CodeSystem/KBV_CS_SFHIR_ITA_WOP\",\"code\":\"72\"}},{\"url\":\"http://fhir.de/StructureDefinition/gkv/versichertenart\",\"valueCoding\":{\"system\":\"https://fhir.kbv.de/CodeSystem/KBV_CS_SFHIR_KBV_VERSICHERTENSTATUS\",\"code\":\"$PATIENT_STATUS\"}}],\"status\":\"active\",\"type\":{\"coding\":[{\"system\":\"http://fhir.de/CodeSystem/versicherungsart-de-basis\",\"code\":\"GKV\"}]},\"beneficiary\":{\"reference\":\"Patient/$PATIENT_ID\"},\"period\":{\"end\":\"$COVERAGE_PERIOD_END\"},\"payor\":[{\"identifier\":{\"system\":\"http://fhir.de/NamingSystem/arge-ik/iknr\",\"value\":\"$COVERAGE_ID\"},\"display\":\"$INSURANCE_NAME\"}]}}]}";

    private PrescriptionBundlesBuilder prescriptionBundlesBuilder;

    public static Muster16PrescriptionForm getMuster16PrescriptionFormForTests() {
        Muster16PrescriptionForm muster16PrescriptionForm;
        muster16PrescriptionForm = new Muster16PrescriptionForm();

        muster16PrescriptionForm.setClinicId("BS12345678");

        muster16PrescriptionForm.setPrescriptionDate("2021-04-05");
        MedicationString medicationString = new MedicationString("Amoxicillin 1000mg N2", null, null, "3x täglich alle 8 Std", null, "2394428");

        muster16PrescriptionForm.setPrescriptionList(Collections.singletonList(medicationString));

        muster16PrescriptionForm.setPractitionerId("LANR1234");

        muster16PrescriptionForm.setInsuranceCompany("Test Insurance Company, Gmbh");

        muster16PrescriptionForm.setPatientDateOfBirth("1986-07-16");
        muster16PrescriptionForm.setPatientNamePrefix(List.of("Dr."));
        muster16PrescriptionForm.setPatientFirstName("John");
        muster16PrescriptionForm.setPatientLastName("Doe");
        muster16PrescriptionForm.setPatientStreetName("Droysenstr.");
        muster16PrescriptionForm.setPatientStreetNumber("7");
        muster16PrescriptionForm.setPatientZipCode("10629");
        muster16PrescriptionForm.setPatientCity("Berlin");
        muster16PrescriptionForm.setPatientInsuranceId("M310119800");
        muster16PrescriptionForm.setPatientStatus("30000");

        muster16PrescriptionForm.setPractitionerNamePrefix("Dr.");
        muster16PrescriptionForm.setPractitionerFirstName("Testarzt");
        muster16PrescriptionForm.setPractitionerLastName("E-Rezept");
        muster16PrescriptionForm.setPractitionerPhone("123456789");

        muster16PrescriptionForm.setPractitionerStreetName("Doc Droysenstr.");
        muster16PrescriptionForm.setPractitionerStreetNumber("7a");
        muster16PrescriptionForm.setPractitionerZipCode("10630");
        muster16PrescriptionForm.setPractitionerCity("Berlinn");

        muster16PrescriptionForm.setPractitionerPhone("030/123456");

        muster16PrescriptionForm.setInsuranceCompanyId("100038825");
        muster16PrescriptionForm.setWithPayment(true);

        return muster16PrescriptionForm;
    }

    @BeforeEach
    public void initialize() throws IOException {
        prescriptionBundlesBuilder = new PrescriptionBundlesBuilder(getMuster16PrescriptionFormForTests());
    }

    @Test
    public void test_Successful_Creation_of_FHIR_EPrescription_Bundle_From_Muster16_Model_Object()
            throws ParseException {

        List<Bundle> fhirEPrescriptionBundles = prescriptionBundlesBuilder.createBundles();

        // Expecting the creation of 7 resources
        // 1. composition resource
        // 2. medication request resource
        // 3. medication resource.
        // 4. patient resource.
        // 5. practitioner resource.
        // 6. organization resource.
        // 7. coverage resource.
        fhirEPrescriptionBundles.forEach(bundle -> assertEquals(7, bundle.getEntry().size()));
        assertEquals(1, fhirEPrescriptionBundles.size());
    }

    @Test
    public void BundleBuilder_createsCorrectNumberOfBundles_givenThreeMedications() throws ParseException {
        // GIVEN
        Muster16PrescriptionForm muster16PrescriptionForm = getMuster16PrescriptionFormForTests();
        muster16PrescriptionForm.setPrescriptionList(List.of(
                new MedicationString("test", "test", "test", "test", "test", "test"),
                new MedicationString("test", "test", "test", "test", "test", "test"),
                new MedicationString("test", "test", "test", "test", "test", "test")));

        prescriptionBundlesBuilder = new PrescriptionBundlesBuilder(muster16PrescriptionForm);

        // WHEN
        List<Bundle> fhirEPrescriptionBundles = prescriptionBundlesBuilder.createBundles();

        // THEN
        assertEquals(3, fhirEPrescriptionBundles.size());
    }

    @Test
    public void test_Successful_XML_Serialization_Of_An_FHIR_EPrescription_Bundle_Object()
            throws ParseException {
        FhirContext ctx = FhirContext.forR4();

        IParser parser = ctx.newXmlParser();

        List<Bundle> fhirEPrescriptionBundles = prescriptionBundlesBuilder.createBundles();

        fhirEPrescriptionBundles.forEach(bundle -> {
            bundle.setId("sample-id-from-gematik-ti-123456");
            parser.setPrettyPrint(true);

            String serialized = parser.encodeResourceToString(bundle);

            logger.info(serialized);
        });
    }

    @Test
    public void test_Successful_JSON_Serialization_Of_An_FHIR_EPrescription_Bundle_Object()
            throws ParseException {
        FhirContext ctx = FhirContext.forR4();

        IParser parser = ctx.newJsonParser();

        List<Bundle> fhirEPrescriptionBundles = prescriptionBundlesBuilder.createBundles();

        fhirEPrescriptionBundles.forEach(bundle -> {
            bundle.setId("sample-id-from-gematik-ti-123456");
            parser.setPrettyPrint(true);

            String serialized = parser.encodeResourceToString(bundle);

            logger.info(serialized);
        });
    }

    @Test
    public void test_Successful_JSON_To_Bundle_Object_Conversion() {
        FhirContext ctx = FhirContext.forR4();
        IParser jsonParser = ctx.newJsonParser();

        Bundle bundle = jsonParser.parseResource(Bundle.class, GOOD_SIMPLIFIER_NET_SAMPLE_KBV_JSON);
        ValidationResult bundleValidationResult =
                prescriptionBundleValidator.validateResource(bundle, true);

        assertTrue(bundleValidationResult.isSuccessful());
    }

    @Test
    public void test_Successful_Validation_Of_Good_Simplifier_Net_Sample_Used_As_Base_For_Bundle_Creation_Template() throws IOException {
        FhirContext ctx = FhirContext.forR4();
        IParser jsonParser = ctx.newJsonParser();

        try(Reader reader =
                    new InputStreamReader(PrescriptionBundlesBuilderTest.this.getClass().getResourceAsStream(
                "/bundle-samples/bundleTemplatev2_filled-debug-3.json"))) {
            Bundle bundle = jsonParser.parseResource(Bundle.class, reader);
            ValidationResult bundleValidationResult =
                    prescriptionBundleValidator.validateResource(bundle, true);

            assertTrue(bundleValidationResult.isSuccessful());
        }
    }

    @Test
    public void test_Successful_Conversion_Of_The_Populated_Bundle_Json_Template_To_A_Bundle_Object()
            throws IOException, XMLStreamException, ParseException {
        SVGExtractor svgExtractor = new SVGExtractor(TemplateProfile.CGM_Z1.configuration);

        try(PDDocument pdDocument = PDDocument.load(getClass()
                .getResourceAsStream("/muster-16-print-samples/test1.pdf"))) {

            Map<String, String> map = svgExtractor.extract(pdDocument);
            Muster16SvgRegexParser parser = new Muster16SvgRegexParser(map);

            Muster16PrescriptionForm muster16PrescriptionForm =
                    Muster16FormDataExtractorService.fillForm(parser);
//            Muster16PrescriptionForm muster16PrescriptionForm =
//                    getMuster16PrescriptionFormForTests();

            muster16PrescriptionForm.setPatientDateOfBirth("02.01.1986");

            IBundlesBuilder bundleBuilder = new PrescriptionBundlesBuilderV2(
                    muster16PrescriptionForm);

            List<Bundle> bundles = bundleBuilder.createBundles();

            if(CollectionUtils.isNotEmpty(bundles)) {
                bundles.stream().forEach(bundle -> {
                    String bundleJsonString = ((EreBundle)bundle).encodeToJson();

                    logger.info("Filled bundle json template result shown below.");
                    logger.info("==============================================");
                    logger.info(bundleJsonString);
                });
            }

            Assertions.assertTrue(CollectionUtils.isNotEmpty(bundles));
        }
    }

    @Test
    public void test_Successful_Validation_Of_A_Compliant_FHIR_KBV_Bundle_Json_Sample_From_SimplifierNet_Site() {
        ValidationResult bundleValidationResult =
                prescriptionBundleValidator.validateResource(GOOD_SIMPLIFIER_NET_SAMPLE_KBV_JSON,
                        true);

        assertTrue(bundleValidationResult.isSuccessful());
    }

    @Test
    public void test_Expected_Validation_Failure_Of_Good_Unfilled_FHIR_KBV_Bundle_Json_Template_Having_Unresolved_Template_Key_Values_Present() {
        ValidationResult bundleValidationResult =
                prescriptionBundleValidator.validateResource(GOOD_SIMPLIFIER_NET_SAMPLE_KBV_JSON_AS_A_TEMPLATE,
                        true);

        Assertions.assertFalse(bundleValidationResult.isSuccessful());
    }

    @Test
    public void test_Expected_Validation_Failure_Of_A_Non_Compliant_FHIR_KBV_Bundle_Json_Having_Incorrect_Structure_AND_Data() {
        ValidationResult bundleValidationResult =
                prescriptionBundleValidator.validateResource(BAD_DENS_SIGN_REQUEST_KBV_JSON,
                        true);

        Assertions.assertFalse(bundleValidationResult.isSuccessful());
    }

    @Test
    public void test_Validation_Failure_Of_FHIR_Patient_Resource_With_Missing_Content() {
        Patient patient = new Patient();

        ValidationResult validationResult =
                prescriptionBundleValidator.validateResource(patient, true);
        assertFalse(validationResult.isSuccessful());
    }

    @Disabled
    @Test
    public void test_Successful_Validation_Of_An_FHIR_Coverage_Resource() {
        Coverage coverageResource = prescriptionBundlesBuilder.createCoverageResource("random_patient_id");

        ValidationResult validationResult =
                prescriptionBundleValidator.validateResource(coverageResource, true);
        logger.info("messages:" + validationResult.getMessages());
        assertTrue(validationResult.isSuccessful());
    }

    @Disabled
    @Test
    public void test_Successful_Validation_Of_XML_Serialization_Of_FHIR_EPrescription_Bundle_Object()
            throws ParseException {
        FhirContext ctx = FhirContext.forR4();
        IParser jsonParser = ctx.newJsonParser();

        jsonParser.setPrettyPrint(true);

        List<Bundle> prescriptionBundles = prescriptionBundlesBuilder.createBundles();

        prescriptionBundles.forEach(bundle -> {
            logger.infof("JSON serialised test bundle object created on back-end is:\n\n%s",
                    jsonParser.encodeResourceToString(bundle));
            ValidationResult validationResult =
                    prescriptionBundleValidator.validateResource(bundle, true);
            assertTrue(validationResult.isSuccessful());
        });
    }

    @Test
    @Disabled("Disabled until validator has a complete configuration")
    public void test_Successful_Validation_Of_XML_Prescription_Bundle() throws IOException {
        try(InputStream is = getClass().getResourceAsStream(
                            "/bundle-samples/bundle_July_2.xml")) {
            byte[] bundleXmlBytes = is.readAllBytes();
            String bundleXmlString = new String(bundleXmlBytes, Charset.forName("UTF-8"));

            ValidationResult bundleValidationResult =
                    prescriptionBundleValidator.validateResource(bundleXmlString, true);

            assertTrue(bundleValidationResult.isSuccessful());
        }
    }
}
