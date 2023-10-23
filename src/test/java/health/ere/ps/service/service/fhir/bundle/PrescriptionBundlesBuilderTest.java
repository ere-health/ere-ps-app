package health.ere.ps.service.fhir.bundle;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.text.ParseException;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;
import javax.xml.stream.XMLStreamException;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.hl7.fhir.r4.model.Bundle;
import org.hl7.fhir.r4.model.Coverage;
import org.hl7.fhir.r4.model.Patient;
import org.jboss.logging.Logger;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.parser.IParser;
import ca.uhn.fhir.validation.ValidationResult;
import health.ere.ps.model.muster16.MedicationString;
import health.ere.ps.model.muster16.Muster16PrescriptionForm;
import health.ere.ps.profile.TitusTestProfile;
import health.ere.ps.service.extractor.SVGExtractor;
import health.ere.ps.service.extractor.TemplateProfile;
import health.ere.ps.service.muster16.Muster16FormDataExtractorService;
import health.ere.ps.service.muster16.parser.rgxer.Muster16SvgRegexParser;
import health.ere.ps.validation.fhir.bundle.PrescriptionBundleValidator;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.junit.TestProfile;

@QuarkusTest
@TestProfile(TitusTestProfile.class)
public class PrescriptionBundlesBuilderTest {
    private static final String BAD_DENS_SIGN_REQUEST_KBV_JSON = " {\"resourceType\":\"Bundle\"," +
            "\"id\":\"e6baf9c0-5d88-4b28-b15d-1c3a2c3f3d19\",\"meta\":{\"lastUpdated\":\"2021-06-16T13:05:38.948-04:00\",\"profile\":[\"https://fhir.kbv.de/StructureDefinition/KBV_PR_ERP_Bundle|1.0.2\"]},\"type\":\"document\",\"timestamp\":\"2021-06-16T13:05:38.948-04:00\",\"entry\":[{\"fullUrl\":\"http://pvs.praxis.local/fhir/Medication/9d8c5ab9-73b8-4165-9f3a-9eb354ea1f88\",\"resource\":{\"resourceType\":\"Medication\",\"id\":\"9d8c5ab9-73b8-4165-9f3a-9eb354ea1f88\",\"meta\":{\"profile\":[\"https://fhir.kbv.de/StructureDefinition/KBV_PR_ERP_Medication_PZN|1.0.2\"]},\"extension\":[{\"url\":\"https://fhir.kbv.de/StructureDefinition/KBV_EX_ERP_Medication_Category\",\"valueCoding\":{\"system\":\"https://fhir.kbv.de/CodeSystem/KBV_CS_ERP_Medication_Category\",\"code\":\"00\"}},{\"url\":\"https://fhir.kbv.de/StructureDefinition/KBV_EX_ERP_Medication_Vaccine\",\"valueBoolean\":false},{\"url\":\"http://fhir.de/StructureDefinition/normgroesse\",\"valueCode\":\"N1\"}],\"code\":{\"coding\":[{\"system\":\"http://fhir.de/CodeSystem/ifa/pzn\",\"code\":\"00027950\"}],\"text\":\"Ibuprofen 600mg\"},\"form\":{\"coding\":[{\"system\":\"https://fhir.kbv.de/CodeSystem/KBV_CS_SFHIR_KBV_DARREICHUNGSFORM\",\"code\":\"FLE\"}]}}},{\"fullUrl\":\"http://pvs.praxis.local/fhir/MedicationRequest/028df042-2321-410c-9fa0-148af5d2b909\",\"resource\":{\"resourceType\":\"MedicationRequest\",\"id\":\"028df042-2321-410c-9fa0-148af5d2b909\",\"meta\":{\"lastUpdated\":\"2021-06-16T13:05:38.948-04:00\",\"profile\":[\"https://fhir.kbv.de/StructureDefinition/KBV_PR_ERP_Prescription|1.0.2\"]},\"extension\":[{\"url\":\"https://fhir.kbv.de/StructureDefinition/KBV_EX_ERP_StatusCoPayment\",\"valueCoding\":{\"system\":\"https://fhir.kbv.de/CodeSystem/KBV_CS_ERP_StatusCoPayment\",\"code\":\"1\"}},{\"url\":\"https://fhir.kbv.de/StructureDefinition/KBV_EX_ERP_EmergencyServicesFee\",\"valueBoolean\":false},{\"url\":\"https://fhir.kbv.de/StructureDefinition/KBV_EX_ERP_BVG\",\"valueBoolean\":false},{\"url\":\"https://fhir.kbv.de/StructureDefinition/KBV_EX_ERP_Multiple_Prescription\",\"extension\":[{\"url\":\"Kennzeichen\",\"valueBoolean\":false}]}],\"status\":\"active\",\"intent\":\"order\",\"medicationReference\":{\"reference\":\"Medication/9d8c5ab9-73b8-4165-9f3a-9eb354ea1f88\"},\"subject\":{\"reference\":\"Patient/\"},\"requester\":{\"reference\":\"Practitioner/30000000\"},\"insurance\":[{\"reference\":\"Coverage/\"}],\"dosageInstruction\":[{\"extension\":[{\"url\":\"https://fhir.kbv.de/StructureDefinition/KBV_EX_ERP_DosageFlag\",\"valueBoolean\":true}],\"text\":\"1-1-1\"}],\"dispenseRequest\":{\"quantity\":{\"value\":1,\"system\":\"http://unitsofmeasure.org\",\"code\":\"{Package}\"}},\"substitution\":{\"allowedBoolean\":true}}},{\"fullUrl\":\"http://pvs.praxis.local/fhir/Composition/ba4fc629-93ce-4670-b47a-b0596bc0aaa6\",\"resource\":{\"resourceType\":\"Composition\",\"id\":\"ba4fc629-93ce-4670-b47a-b0596bc0aaa6\",\"meta\":{\"profile\":[\"https://fhir.kbv.de/StructureDefinition/KBV_PR_ERP_Composition|1.0.2\"]},\"extension\":[{\"url\":\"https://fhir.kbv.de/StructureDefinition/KBV_EX_FOR_Legal_basis\",\"valueCoding\":{\"system\":\"https://fhir.kbv.de/CodeSystem/KBV_CS_SFHIR_KBV_STATUSKENNZEICHEN\",\"code\":\"04\"}}],\"status\":\"final\",\"type\":{\"coding\":[{\"system\":\"https://fhir.kbv.de/CodeSystem/KBV_CS_SFHIR_KBV_FORMULAR_ART\",\"code\":\"e16A\"}]},\"subject\":{\"reference\":\"Patient/\"},\"date\":\"2021-06-16T13:05:38-04:00\",\"author\":[{\"reference\":\"Practitioner/30000000\",\"type\":\"Practitioner\"},{\"type\":\"Device\",\"identifier\":{\"system\":\"https://fhir.kbv.de/NamingSystem/KBV_NS_FOR_Pruefnummer\",\"value\":\"123456\"}}],\"title\":\"elektronische Arzneimittelverordnung\",\"attester\":[{\"mode\":\"legal\",\"party\":{\"reference\":\"Practitioner/30000000\"}}],\"custodian\":{\"reference\":\"Organization/30000000\"},\"section\":[{\"code\":{\"coding\":[{\"system\":\"https://fhir.kbv.de/CodeSystem/KBV_CS_ERP_Section_Type\",\"code\":\"Prescription\"}]},\"entry\":[{\"reference\":\"MedicationRequest/028df042-2321-410c-9fa0-148af5d2b909\"}]},{\"code\":{\"coding\":[{\"system\":\"https://fhir.kbv.de/CodeSystem/KBV_CS_ERP_Section_Type\",\"code\":\"Coverage\"}]},\"entry\":[{\"reference\":\"Coverage/\"}]}]}},{\"fullUrl\":\"http://pvs.praxis.local/fhir/Patient/null\",\"resource\":{\"resourceType\":\"Patient\",\"meta\":{\"profile\":[\"https://fhir.kbv.de/StructureDefinition/KBV_PR_FOR_Patient|1.0.3\"]},\"identifier\":[{\"type\":{\"coding\":[{\"system\":\"http://fhir.de/CodeSystem/identifier-type-de-basis\",\"code\":\"GKV\"}]},\"system\":\"http://fhir.de/NamingSystem/gkv/kvid-10\"}],\"name\":[{\"use\":\"official\",\"family\":\"Heckner\",\"given\":[\"Markus\"],\"prefix\":[\"Dr.\"]}],\"address\":[{\"type\":\"both\",\"line\":[\"Berliner Str. 12\"],\"city\":\"Teltow\",\"postalCode\":\"14513\",\"country\":\"D\",\"_line\":[{\"extension\":null}]}]}},{\"fullUrl\":\"http://pvs.praxis.local/fhir/Practitioner/30000000\",\"resource\":{\"resourceType\":\"Practitioner\",\"id\":\"30000000\",\"meta\":{\"profile\":[\"https://fhir.kbv.de/StructureDefinition/KBV_PR_FOR_Practitioner|1.0.3\"]},\"identifier\":[{\"type\":{\"coding\":[{\"system\":\"http://terminology.hl7.org/CodeSystem/v2-0203\",\"code\":\"LANR\"}]},\"system\":\"https://fhir.kbv.de/NamingSystem/KBV_NS_Base_ANR\",\"value\":\"30000000\"}],\"name\":[{\"extension\":[{\"url\":\"http://hl7.org/fhir/StructureDefinition/iso21090-EN-qualifier\",\"valueString\":\"AC\"}],\"use\":\"official\",\"family\":\"Doctor Last Name\",\"given\":[\"Doctor First Name\"]}],\"qualification\":[{\"code\":{\"coding\":[{\"system\":\"https://fhir.kbv.de/CodeSystem/KBV_CS_FOR_Qualification_Type\",\"code\":\"00\",\"display\":\"Arzt-Hausarzt\"}]}},{\"code\":{\"text\":\"Arzt-Hausarzt\"}}]}},{\"fullUrl\":\"http://pvs.praxis.local/fhir/Organization/30000000\",\"resource\":{\"resourceType\":\"Organization\",\"id\":\"30000000\",\"meta\":{\"profile\":[\"https://fhir.kbv.de/StructureDefinition/KBV_PR_FOR_Organization|1.0.3\",\"https://fhir.kbv.de/StructureDefinition/KBV_PR_FOR_Coverage|1.0.3\"]},\"identifier\":[{\"type\":{\"coding\":[{\"system\":\"http://terminology.hl7.org/CodeSystem/v2-0203\",\"code\":\"BSNR\"}]},\"system\":\"https://fhir.kbv.de/NamingSystem/KBV_NS_Base_BSNR\",\"value\":\"30000000\"}],\"name\":\"null Doctor First Name Doctor Last Name\",\"telecom\":[{\"system\":\"phone\",\"value\":\"030/123456789\"}],\"address\":[{\"type\":\"both\",\"line\":[\"Doctor Street Name Doctor Street Number\"],\"city\":\"Doctor City\",\"postalCode\":\"012345\",\"country\":\"D\"}]}},{\"fullUrl\":\"http://pvs.praxis.local/fhir/Coverage/null\",\"resource\":{\"resourceType\":\"Coverage\",\"meta\":{\"profile\":[\"https://fhir.kbv.de/StructureDefinition/KBV_PR_FOR_Coverage|1.0.3\"]},\"extension\":[{\"url\":\"http://fhir.de/StructureDefinition/gkv/besondere-personengruppe\",\"valueCoding\":{\"system\":\"https://fhir.kbv.de/CodeSystem/KBV_CS_SFHIR_KBV_PERSONENGRUPPE\",\"code\":\"00\"}},{\"url\":\"http://fhir.de/StructureDefinition/gkv/dmp-kennzeichen\",\"valueCoding\":{\"system\":\"https://fhir.kbv.de/CodeSystem/KBV_CS_SFHIR_KBV_DMP\",\"code\":\"00\"}},{\"url\":\"http://fhir.de/StructureDefinition/gkv/wop\",\"valueCoding\":{\"system\":\"https://fhir.kbv.de/CodeSystem/KBV_CS_SFHIR_ITA_WOP\",\"code\":\"72\"}},{\"url\":\"https://fhir.kbv.de/CodeSystem/KBV_CS_SFHIR_KBV_VERSICHERTENSTATUS\",\"valueCoding\":{\"system\":\"https://fhir.kbv.de/CodeSystem/KBV_CS_SFHIR_ITA_WOP\",\"code\":\"3\"}}],\"status\":\"active\",\"type\":{\"coding\":[{\"system\":\"http://fhir.de/CodeSystem/versicherungsart-de-basis\",\"code\":\"GKV\"}]},\"beneficiary\":{\"reference\":\"Patient/\"},\"payor\":[{\"identifier\":{\"system\":\"http://fhir.de/NamingSystem/arge-ik/iknr\"},\"display\":\"DENS GmbH\"}]}}]}";
    private static final String GOOD_SIMPLIFIER_NET_SAMPLE_KBV_JSON = "{\n" + //
            "    \"resourceType\": \"Bundle\",\n" + //
            "    \"id\": \"0428d416-149e-48a4-977c-394887b3d85c\",\n" + //
            "    \"meta\": {\n" + //
            "        \"lastUpdated\": \"2022-05-20T08:30:00Z\",\n" + //
            "        \"profile\":  [\n" + //
            "            \"https://fhir.kbv.de/StructureDefinition/KBV_PR_ERP_Bundle|1.1.0\"\n" + //
            "        ]\n" + //
            "    },\n" + //
            "    \"identifier\": {\n" + //
            "        \"system\": \"https://gematik.de/fhir/erp/NamingSystem/GEM_ERP_NS_PrescriptionId\",\n" + //
            "        \"value\": \"160.100.000.000.002.36\"\n" + //
            "    },\n" + //
            "    \"type\": \"document\",\n" + //
            "    \"timestamp\": \"2022-05-20T08:30:00Z\",\n" + //
            "    \"entry\":  [\n" + //
            "        {\n" + //
            "            \"fullUrl\": \"http://pvs.praxis.local/fhir/Composition/a054c2f3-0123-4d33-a0b3-bedec2f7d1ea\",\n" + //
            "            \"resource\": {\n" + //
            "                \"resourceType\": \"Composition\",\n" + //
            "                \"id\": \"a054c2f3-0123-4d33-a0b3-bedec2f7d1ea\",\n" + //
            "                \"meta\": {\n" + //
            "                    \"profile\":  [\n" + //
            "                        \"https://fhir.kbv.de/StructureDefinition/KBV_PR_ERP_Composition|1.1.0\"\n" + //
            "                    ]\n" + //
            "                },\n" + //
            "                \"extension\":  [\n" + //
            "                    {\n" + //
            "                        \"url\": \"https://fhir.kbv.de/StructureDefinition/KBV_EX_FOR_Legal_basis\",\n" + //
            "                        \"valueCoding\": {\n" + //
            "                            \"system\": \"https://fhir.kbv.de/CodeSystem/KBV_CS_SFHIR_KBV_STATUSKENNZEICHEN\",\n" + //
            "                            \"code\": \"00\"\n" + //
            "                        }\n" + //
            "                    }\n" + //
            "                ],\n" + //
            "                \"status\": \"final\",\n" + //
            "                \"type\": {\n" + //
            "                    \"coding\":  [\n" + //
            "                        {\n" + //
            "                            \"system\": \"https://fhir.kbv.de/CodeSystem/KBV_CS_SFHIR_KBV_FORMULAR_ART\",\n" + //
            "                            \"code\": \"e16A\"\n" + //
            "                        }\n" + //
            "                    ]\n" + //
            "                },\n" + //
            "                \"subject\": {\n" + //
            "                    \"reference\": \"Patient/512ab5bc-a7ab-4fd7-81cc-16a594f747a6\"\n" + //
            "                },\n" + //
            "                \"date\": \"2022-05-20T08:00:00Z\",\n" + //
            "                \"author\":  [\n" + //
            "                    {\n" + //
            "                        \"reference\": \"Practitioner/e33d2afd-44c8-462b-80e5-52dbe5ebf359\",\n" + //
            "                        \"type\": \"Practitioner\"\n" + //
            "                    },\n" + //
            "                    {\n" + //
            "                        \"type\": \"Device\",\n" + //
            "                        \"identifier\": {\n" + //
            "                            \"system\": \"https://fhir.kbv.de/NamingSystem/KBV_NS_FOR_Pruefnummer\",\n" + //
            "                            \"value\": \"Y/400/2107/36/999\"\n" + //
            "                        }\n" + //
            "                    }\n" + //
            "                ],\n" + //
            "                \"title\": \"elektronische Arzneimittelverordnung\",\n" + //
            "                \"custodian\": {\n" + //
            "                    \"reference\": \"Organization/d2b30a70-9830-4968-ab97-688472b6f9a3\"\n" + //
            "                },\n" + //
            "                \"section\":  [\n" + //
            "                    {\n" + //
            "                        \"code\": {\n" + //
            "                            \"coding\":  [\n" + //
            "                                {\n" + //
            "                                    \"system\": \"https://fhir.kbv.de/CodeSystem/KBV_CS_ERP_Section_Type\",\n" + //
            "                                    \"code\": \"Prescription\"\n" + //
            "                                }\n" + //
            "                            ]\n" + //
            "                        },\n" + //
            "                        \"entry\":  [\n" + //
            "                            {\n" + //
            "                                \"reference\": \"MedicationRequest/06dc1594-509a-4f4c-ada7-dfd477a02d86\"\n" + //
            "                            }\n" + //
            "                        ]\n" + //
            "                    },\n" + //
            "                    {\n" + //
            "                        \"code\": {\n" + //
            "                            \"coding\":  [\n" + //
            "                                {\n" + //
            "                                    \"system\": \"https://fhir.kbv.de/CodeSystem/KBV_CS_ERP_Section_Type\",\n" + //
            "                                    \"code\": \"Coverage\"\n" + //
            "                                }\n" + //
            "                            ]\n" + //
            "                        },\n" + //
            "                        \"entry\":  [\n" + //
            "                            {\n" + //
            "                                \"reference\": \"Coverage/df0f2536-97b9-4bae-99cc-83ba2e8371e4\"\n" + //
            "                            }\n" + //
            "                        ]\n" + //
            "                    }\n" + //
            "                ]\n" + //
            "            }\n" + //
            "        },\n" + //
            "        {\n" + //
            "            \"fullUrl\": \"http://pvs.praxis.local/fhir/MedicationRequest/06dc1594-509a-4f4c-ada7-dfd477a02d86\",\n" + //
            "            \"resource\": {\n" + //
            "                \"resourceType\": \"MedicationRequest\",\n" + //
            "                \"id\": \"06dc1594-509a-4f4c-ada7-dfd477a02d86\",\n" + //
            "                \"meta\": {\n" + //
            "                    \"profile\":  [\n" + //
            "                        \"https://fhir.kbv.de/StructureDefinition/KBV_PR_ERP_Prescription|1.1.0\"\n" + //
            "                    ]\n" + //
            "                },\n" + //
            "                \"extension\":  [\n" + //
            "                    {\n" + //
            "                        \"url\": \"https://fhir.kbv.de/StructureDefinition/KBV_EX_FOR_StatusCoPayment\",\n" + //
            "                        \"valueCoding\": {\n" + //
            "                            \"system\": \"https://fhir.kbv.de/CodeSystem/KBV_CS_FOR_StatusCoPayment\",\n" + //
            "                            \"code\": \"1\"\n" + //
            "                        }\n" + //
            "                    },\n" + //
            "                    {\n" + //
            "                        \"url\": \"https://fhir.kbv.de/StructureDefinition/KBV_EX_ERP_EmergencyServicesFee\",\n" + //
            "                        \"valueBoolean\": false\n" + //
            "                    },\n" + //
            "                    {\n" + //
            "                        \"url\": \"https://fhir.kbv.de/StructureDefinition/KBV_EX_ERP_BVG\",\n" + //
            "                        \"valueBoolean\": false\n" + //
            "                    },\n" + //
            "                    {\n" + //
            "                        \"url\": \"https://fhir.kbv.de/StructureDefinition/KBV_EX_ERP_Multiple_Prescription\",\n" + //
            "                        \"extension\":  [\n" + //
            "                            {\n" + //
            "                                \"url\": \"Kennzeichen\",\n" + //
            "                                \"valueBoolean\": false\n" + //
            "                            }\n" + //
            "                        ]\n" + //
            "                    }\n" + //
            "                ],\n" + //
            "                \"status\": \"active\",\n" + //
            "                \"intent\": \"order\",\n" + //
            "                \"medicationReference\": {\n" + //
            "                    \"reference\": \"Medication/f568397d-7ba2-46ac-904b-02caec933b42\"\n" + //
            "                },\n" + //
            "                \"subject\": {\n" + //
            "                    \"reference\": \"Patient/512ab5bc-a7ab-4fd7-81cc-16a594f747a6\"\n" + //
            "                },\n" + //
            "                \"authoredOn\": \"2022-05-20\",\n" + //
            "                \"requester\": {\n" + //
            "                    \"reference\": \"Practitioner/e33d2afd-44c8-462b-80e5-52dbe5ebf359\"\n" + //
            "                },\n" + //
            "                \"insurance\":  [\n" + //
            "                    {\n" + //
            "                        \"reference\": \"Coverage/df0f2536-97b9-4bae-99cc-83ba2e8371e4\"\n" + //
            "                    }\n" + //
            "                ],\n" + //
            "                \"dosageInstruction\":  [\n" + //
            "                    {\n" + //
            "                        \"extension\":  [\n" + //
            "                            {\n" + //
            "                                \"url\": \"https://fhir.kbv.de/StructureDefinition/KBV_EX_ERP_DosageFlag\",\n" + //
            "                                \"valueBoolean\": true\n" + //
            "                            }\n" + //
            "                        ],\n" + //
            "                        \"text\": \"2mal tägl. 5ml\"\n" + //
            "                    }\n" + //
            "                ],\n" + //
            "                \"dispenseRequest\": {\n" + //
            "                    \"quantity\": {\n" + //
            "                        \"value\": 1,\n" + //
            "                        \"system\": \"http://unitsofmeasure.org\",\n" + //
            "                        \"code\": \"{Package}\"\n" + //
            "                    }\n" + //
            "                },\n" + //
            "                \"substitution\": {\n" + //
            "                    \"allowedBoolean\": true\n" + //
            "                }\n" + //
            "            }\n" + //
            "        },\n" + //
            "        {\n" + //
            "            \"fullUrl\": \"http://pvs.praxis.local/fhir/Medication/f568397d-7ba2-46ac-904b-02caec933b42\",\n" + //
            "            \"resource\": {\n" + //
            "                \"resourceType\": \"Medication\",\n" + //
            "                \"id\": \"f568397d-7ba2-46ac-904b-02caec933b42\",\n" + //
            "                \"meta\": {\n" + //
            "                    \"profile\":  [\n" + //
            "                        \"https://fhir.kbv.de/StructureDefinition/KBV_PR_ERP_Medication_PZN|1.1.0\"\n" + //
            "                    ]\n" + //
            "                },\n" + //
            "                \"extension\":  [\n" + //
            "                    {\n" + //
            "                        \"url\": \"https://fhir.kbv.de/StructureDefinition/KBV_EX_Base_Medication_Type\",\n" + //
            "                        \"valueCodeableConcept\": {\n" + //
            "                            \"coding\":  [\n" + //
            "                                {\n" + //
            "                                    \"system\": \"http://snomed.info/sct\",\n" + //
            "                                    \"version\": \"http://snomed.info/sct/900000000000207008/version/20220331\",\n" + //
            "                                    \"code\": \"763158003\",\n" + //
            "                                    \"display\": \"Medicinal product (product)\"\n" + //
            "                                }\n" + //
            "                            ]\n" + //
            "                        }\n" + //
            "                    },\n" + //
            "                    {\n" + //
            "                        \"url\": \"https://fhir.kbv.de/StructureDefinition/KBV_EX_ERP_Medication_Category\",\n" + //
            "                        \"valueCoding\": {\n" + //
            "                            \"system\": \"https://fhir.kbv.de/CodeSystem/KBV_CS_ERP_Medication_Category\",\n" + //
            "                            \"code\": \"00\"\n" + //
            "                        }\n" + //
            "                    },\n" + //
            "                    {\n" + //
            "                        \"url\": \"https://fhir.kbv.de/StructureDefinition/KBV_EX_ERP_Medication_Vaccine\",\n" + //
            "                        \"valueBoolean\": false\n" + //
            "                    },\n" + //
            "                    {\n" + //
            "                        \"url\": \"http://fhir.de/StructureDefinition/normgroesse\",\n" + //
            "                        \"valueCode\": \"N1\"\n" + //
            "                    }\n" + //
            "                ],\n" + //
            "                \"code\": {\n" + //
            "                    \"coding\":  [\n" + //
            "                        {\n" + //
            "                            \"system\": \"http://fhir.de/CodeSystem/ifa/pzn\",\n" + //
            "                            \"code\": \"08585997\"\n" + //
            "                        }\n" + //
            "                    ],\n" + //
            "                    \"text\": \"Prospan® Hustensaft 100ml N1\"\n" + //
            "                },\n" + //
            "                \"form\": {\n" + //
            "                    \"coding\":  [\n" + //
            "                        {\n" + //
            "                            \"system\": \"https://fhir.kbv.de/CodeSystem/KBV_CS_SFHIR_KBV_DARREICHUNGSFORM\",\n" + //
            "                            \"code\": \"FLE\"\n" + //
            "                        }\n" + //
            "                    ]\n" + //
            "                }\n" + //
            "            }\n" + //
            "        },\n" + //
            "        {\n" + //
            "            \"fullUrl\": \"http://pvs.praxis.local/fhir/Patient/512ab5bc-a7ab-4fd7-81cc-16a594f747a6\",\n" + //
            "            \"resource\": {\n" + //
            "                \"resourceType\": \"Patient\",\n" + //
            "                \"id\": \"512ab5bc-a7ab-4fd7-81cc-16a594f747a6\",\n" + //
            "                \"meta\": {\n" + //
            "                    \"profile\":  [\n" + //
            "                        \"https://fhir.kbv.de/StructureDefinition/KBV_PR_FOR_Patient|1.1.0\"\n" + //
            "                    ]\n" + //
            "                },\n" + //
            "                \"identifier\":  [\n" + //
            "                    {\n" + //
            "                        \"type\": {\n" + //
            "                            \"coding\":  [\n" + //
            "                                {\n" + //
            "                                    \"system\": \"http://fhir.de/CodeSystem/identifier-type-de-basis\",\n" + //
            "                                    \"code\": \"GKV\"\n" + //
            "                                }\n" + //
            "                            ]\n" + //
            "                        },\n" + //
            "                        \"system\": \"http://fhir.de/sid/gkv/kvid-10\",\n" + //
            "                        \"value\": \"M310119802\"\n" + //
            "                    }\n" + //
            "                ],\n" + //
            "                \"name\":  [\n" + //
            "                    {\n" + //
            "                        \"use\": \"official\",\n" + //
            "                        \"family\": \"Erbprinzessin von und zu der Schimmelpfennig-Hammerschmidt Federmannssohn\",\n" + //
            "                        \"_family\": {\n" + //
            "                            \"extension\":  [\n" + //
            "                                {\n" + //
            "                                    \"url\": \"http://fhir.de/StructureDefinition/humanname-namenszusatz\",\n" + //
            "                                    \"valueString\": \"Erbprinzessin\"\n" + //
            "                                },\n" + //
            "                                {\n" + //
            "                                    \"url\": \"http://hl7.org/fhir/StructureDefinition/humanname-own-prefix\",\n" + //
            "                                    \"valueString\": \"von und zu der\"\n" + //
            "                                },\n" + //
            "                                {\n" + //
            "                                    \"url\": \"http://hl7.org/fhir/StructureDefinition/humanname-own-name\",\n" + //
            "                                    \"valueString\": \"Schimmelpfennig-Hammerschmidt Federmannssohn\"\n" + //
            "                                }\n" + //
            "                            ]\n" + //
            "                        },\n" + //
            "                        \"given\":  [\n" + //
            "                            \"Ingrid\"\n" + //
            "                        ]\n" + //
            "                    }\n" + //
            "                ],\n" + //
            "                \"birthDate\": \"2010-01-31\",\n" + //
            "                \"address\":  [\n" + //
            "                    {\n" + //
            "                        \"type\": \"both\",\n" + //
            "                        \"line\":  [\n" + //
            "                            \"Anneliese- und Georg-von-Groscurth-Plaetzchen 149-C\",\n" + //
            "                            \"5. OG - Hinterhof\"\n" + //
            "                        ],\n" + //
            "                        \"_line\":  [\n" + //
            "                            {\n" + //
            "                                \"extension\":  [\n" + //
            "                                    {\n" + //
            "                                        \"url\": \"http://hl7.org/fhir/StructureDefinition/iso21090-ADXP-houseNumber\",\n" + //
            "                                        \"valueString\": \"149-C\"\n" + //
            "                                    },\n" + //
            "                                    {\n" + //
            "                                        \"url\": \"http://hl7.org/fhir/StructureDefinition/iso21090-ADXP-streetName\",\n" + //
            "                                        \"valueString\": \"Anneliese- und Georg-von-Groscurth-Plaetzchen\"\n" + //
            "                                    }\n" + //
            "                                ]\n" + //
            "                            },\n" + //
            "                            {\n" + //
            "                                \"extension\":  [\n" + //
            "                                    {\n" + //
            "                                        \"url\": \"http://hl7.org/fhir/StructureDefinition/iso21090-ADXP-additionalLocator\",\n" + //
            "                                        \"valueString\": \"5. OG - Hinterhof\"\n" + //
            "                                    }\n" + //
            "                                ]\n" + //
            "                            }\n" + //
            "                        ],\n" + //
            "                        \"city\": \"Bad Homburg\",\n" + //
            "                        \"postalCode\": \"60437\",\n" + //
            "                        \"country\": \"D\"\n" + //
            "                    }\n" + //
            "                ]\n" + //
            "            }\n" + //
            "        },\n" + //
            "        {\n" + //
            "            \"fullUrl\": \"http://pvs.praxis.local/fhir/Practitioner/e33d2afd-44c8-462b-80e5-52dbe5ebf359\",\n" + //
            "            \"resource\": {\n" + //
            "                \"resourceType\": \"Practitioner\",\n" + //
            "                \"id\": \"e33d2afd-44c8-462b-80e5-52dbe5ebf359\",\n" + //
            "                \"meta\": {\n" + //
            "                    \"profile\":  [\n" + //
            "                        \"https://fhir.kbv.de/StructureDefinition/KBV_PR_FOR_Practitioner|1.1.0\"\n" + //
            "                    ]\n" + //
            "                },\n" + //
            "                \"identifier\":  [\n" + //
            "                    {\n" + //
            "                        \"type\": {\n" + //
            "                            \"coding\":  [\n" + //
            "                                {\n" + //
            "                                    \"system\": \"http://terminology.hl7.org/CodeSystem/v2-0203\",\n" + //
            "                                    \"code\": \"LANR\"\n" + //
            "                                }\n" + //
            "                            ]\n" + //
            "                        },\n" + //
            "                        \"system\": \"https://fhir.kbv.de/NamingSystem/KBV_NS_Base_ANR\",\n" + //
            "                        \"value\": \"456456534\"\n" + //
            "                    }\n" + //
            "                ],\n" + //
            "                \"name\":  [\n" + //
            "                    {\n" + //
            "                        \"use\": \"official\",\n" + //
            "                        \"family\": \"Weber\",\n" + //
            "                        \"_family\": {\n" + //
            "                            \"extension\":  [\n" + //
            "                                {\n" + //
            "                                    \"url\": \"http://hl7.org/fhir/StructureDefinition/humanname-own-name\",\n" + //
            "                                    \"valueString\": \"Weber\"\n" + //
            "                                }\n" + //
            "                            ]\n" + //
            "                        },\n" + //
            "                        \"given\":  [\n" + //
            "                            \"Maximilian\"\n" + //
            "                        ],\n" + //
            "                        \"prefix\":  [\n" + //
            "                            \"Dr.\"\n" + //
            "                        ],\n" + //
            "                        \"_prefix\":  [\n" + //
            "                            {\n" + //
            "                                \"extension\":  [\n" + //
            "                                    {\n" + //
            "                                        \"url\": \"http://hl7.org/fhir/StructureDefinition/iso21090-EN-qualifier\",\n" + //
            "                                        \"valueCode\": \"AC\"\n" + //
            "                                    }\n" + //
            "                                ]\n" + //
            "                            }\n" + //
            "                        ]\n" + //
            "                    }\n" + //
            "                ],\n" + //
            "                \"qualification\":  [\n" + //
            "                    {\n" + //
            "                        \"code\": {\n" + //
            "                            \"coding\":  [\n" + //
            "                                {\n" + //
            "                                    \"system\": \"https://fhir.kbv.de/CodeSystem/KBV_CS_FOR_Qualification_Type\",\n" + //
            "                                    \"code\": \"00\"\n" + //
            "                                }\n" + //
            "                            ]\n" + //
            "                        }\n" + //
            "                    },\n" + //
            "                    {\n" + //
            "                        \"code\": {\n" + //
            "                            \"coding\":  [\n" + //
            "                                {\n" + //
            "                                    \"system\": \"https://fhir.kbv.de/CodeSystem/KBV_CS_FOR_Berufsbezeichnung\",\n" + //
            "                                    \"code\": \"Berufsbezeichnung\"\n" + //
            "                                }\n" + //
            "                            ],\n" + //
            "                            \"text\": \"Facharzt für Kinder- und Jugendmedizin\"\n" + //
            "                        }\n" + //
            "                    }\n" + //
            "                ]\n" + //
            "            }\n" + //
            "        },\n" + //
            "        {\n" + //
            "            \"fullUrl\": \"http://pvs.praxis.local/fhir/Organization/d2b30a70-9830-4968-ab97-688472b6f9a3\",\n" + //
            "            \"resource\": {\n" + //
            "                \"resourceType\": \"Organization\",\n" + //
            "                \"id\": \"d2b30a70-9830-4968-ab97-688472b6f9a3\",\n" + //
            "                \"meta\": {\n" + //
            "                    \"profile\":  [\n" + //
            "                        \"https://fhir.kbv.de/StructureDefinition/KBV_PR_FOR_Organization|1.1.0\"\n" + //
            "                    ]\n" + //
            "                },\n" + //
            "                \"identifier\":  [\n" + //
            "                    {\n" + //
            "                        \"type\": {\n" + //
            "                            \"coding\":  [\n" + //
            "                                {\n" + //
            "                                    \"system\": \"http://terminology.hl7.org/CodeSystem/v2-0203\",\n" + //
            "                                    \"code\": \"BSNR\"\n" + //
            "                                }\n" + //
            "                            ]\n" + //
            "                        },\n" + //
            "                        \"system\": \"https://fhir.kbv.de/NamingSystem/KBV_NS_Base_BSNR\",\n" + //
            "                        \"value\": \"687777700\"\n" + //
            "                    }\n" + //
            "                ],\n" + //
            "                \"name\": \"Kinderarztpraxis\",\n" + //
            "                \"telecom\":  [\n" + //
            "                    {\n" + //
            "                        \"system\": \"phone\",\n" + //
            "                        \"value\": \"09411234567\"\n" + //
            "                    }\n" + //
            "                ],\n" + //
            "                \"address\":  [\n" + //
            "                    {\n" + //
            "                        \"type\": \"both\",\n" + //
            "                        \"line\":  [\n" + //
            "                            \"Yorckstraße 15\",\n" + //
            "                            \"Hinterhaus\"\n" + //
            "                        ],\n" + //
            "                        \"_line\":  [\n" + //
            "                            {\n" + //
            "                                \"extension\":  [\n" + //
            "                                    {\n" + //
            "                                        \"url\": \"http://hl7.org/fhir/StructureDefinition/iso21090-ADXP-houseNumber\",\n" + //
            "                                        \"valueString\": \"15\"\n" + //
            "                                    },\n" + //
            "                                    {\n" + //
            "                                        \"url\": \"http://hl7.org/fhir/StructureDefinition/iso21090-ADXP-streetName\",\n" + //
            "                                        \"valueString\": \"Yorckstraße\"\n" + //
            "                                    }\n" + //
            "                                ]\n" + //
            "                            },\n" + //
            "                            {\n" + //
            "                                \"extension\":  [\n" + //
            "                                    {\n" + //
            "                                        \"url\": \"http://hl7.org/fhir/StructureDefinition/iso21090-ADXP-additionalLocator\",\n" + //
            "                                        \"valueString\": \"Hinterhaus\"\n" + //
            "                                    }\n" + //
            "                                ]\n" + //
            "                            }\n" + //
            "                        ],\n" + //
            "                        \"city\": \"Regensburg\",\n" + //
            "                        \"postalCode\": \"93049\",\n" + //
            "                        \"country\": \"D\"\n" + //
            "                    }\n" + //
            "                ]\n" + //
            "            }\n" + //
            "        },\n" + //
            "        {\n" + //
            "            \"fullUrl\": \"http://pvs.praxis.local/fhir/Coverage/df0f2536-97b9-4bae-99cc-83ba2e8371e4\",\n" + //
            "            \"resource\": {\n" + //
            "                \"resourceType\": \"Coverage\",\n" + //
            "                \"id\": \"df0f2536-97b9-4bae-99cc-83ba2e8371e4\",\n" + //
            "                \"meta\": {\n" + //
            "                    \"profile\":  [\n" + //
            "                        \"https://fhir.kbv.de/StructureDefinition/KBV_PR_FOR_Coverage|1.1.0\"\n" + //
            "                    ]\n" + //
            "                },\n" + //
            "                \"extension\":  [\n" + //
            "                    {\n" + //
            "                        \"url\": \"http://fhir.de/StructureDefinition/gkv/besondere-personengruppe\",\n" + //
            "                        \"valueCoding\": {\n" + //
            "                            \"system\": \"https://fhir.kbv.de/CodeSystem/KBV_CS_SFHIR_KBV_PERSONENGRUPPE\",\n" + //
            "                            \"code\": \"00\"\n" + //
            "                        }\n" + //
            "                    },\n" + //
            "                    {\n" + //
            "                        \"url\": \"http://fhir.de/StructureDefinition/gkv/dmp-kennzeichen\",\n" + //
            "                        \"valueCoding\": {\n" + //
            "                            \"system\": \"https://fhir.kbv.de/CodeSystem/KBV_CS_SFHIR_KBV_DMP\",\n" + //
            "                            \"code\": \"00\"\n" + //
            "                        }\n" + //
            "                    },\n" + //
            "                    {\n" + //
            "                        \"url\": \"http://fhir.de/StructureDefinition/gkv/wop\",\n" + //
            "                        \"valueCoding\": {\n" + //
            "                            \"system\": \"https://fhir.kbv.de/CodeSystem/KBV_CS_SFHIR_ITA_WOP\",\n" + //
            "                            \"code\": \"72\"\n" + //
            "                        }\n" + //
            "                    },\n" + //
            "                    {\n" + //
            "                        \"url\": \"http://fhir.de/StructureDefinition/gkv/versichertenart\",\n" + //
            "                        \"valueCoding\": {\n" + //
            "                            \"system\": \"https://fhir.kbv.de/CodeSystem/KBV_CS_SFHIR_KBV_VERSICHERTENSTATUS\",\n" + //
            "                            \"code\": \"3\"\n" + //
            "                        }\n" + //
            "                    }\n" + //
            "                ],\n" + //
            "                \"status\": \"active\",\n" + //
            "                \"type\": {\n" + //
            "                    \"coding\":  [\n" + //
            "                        {\n" + //
            "                            \"system\": \"http://fhir.de/CodeSystem/versicherungsart-de-basis\",\n" + //
            "                            \"code\": \"GKV\"\n" + //
            "                        }\n" + //
            "                    ]\n" + //
            "                },\n" + //
            "                \"beneficiary\": {\n" + //
            "                    \"reference\": \"Patient/512ab5bc-a7ab-4fd7-81cc-16a594f747a6\"\n" + //
            "                },\n" + //
            "                \"period\": {\n" + //
            "                    \"end\": \"2040-04-01\"\n" + //
            "                },\n" + //
            "                \"payor\":  [\n" + //
            "                    {\n" + //
            "                        \"identifier\": {\n" + //
            "                            \"system\": \"http://fhir.de/sid/arge-ik/iknr\",\n" + //
            "                            \"value\": \"108416214\"\n" + //
            "                        },\n" + //
            "                        \"display\": \"AOK Bayern Die Gesundh.\"\n" + //
            "                    }\n" + //
            "                ]\n" + //
            "            }\n" + //
            "        }\n" + //
            "    ]\n" + //
            "}";
    private static final String GOOD_SIMPLIFIER_NET_SAMPLE_KBV_JSON_AS_A_TEMPLATE = "{\"resourceType\":\"Bundle\",\"id\":\"$BUNDLE_ID\"," +
            "\"meta\":{\"lastUpdated\":\"$LAST_UPDATED\",\"profile\":[\"https://fhir.kbv.de/StructureDefinition/KBV_PR_ERP_Bundle|1.0.2\"]},\"identifier\":{\"system\":\"https://gematik.de/fhir/NamingSystem/PrescriptionID\",\"value\":\"$PRESCRIPTION_ID\"},\"type\":\"document\",\"timestamp\":\"$TIMESTAMP\",\"entry\":[{\"fullUrl\":\"http://pvs.praxis.local/fhir/Composition/$COMPOSITION_ID\",\"resource\":{\"resourceType\":\"Composition\",\"id\":\"$COMPOSITION_ID\",\"meta\":{\"profile\":[\"https://fhir.kbv.de/StructureDefinition/KBV_PR_ERP_Composition|1.0.2\"]},\"extension\":[{\"url\":\"https://fhir.kbv.de/StructureDefinition/KBV_EX_FOR_Legal_basis\",\"valueCoding\":{\"system\":\"https://fhir.kbv.de/CodeSystem/KBV_CS_SFHIR_KBV_STATUSKENNZEICHEN\",\"code\":\"00\"}}],\"status\":\"final\",\"type\":{\"coding\":[{\"system\":\"https://fhir.kbv.de/CodeSystem/KBV_CS_SFHIR_KBV_FORMULAR_ART\",\"code\":\"e16A\"}]},\"subject\":{\"reference\":\"Patient/$PATIENT_ID\"},\"date\":\"$COMPOSITION_DATE\",\"author\":[{\"reference\":\"Practitioner/$PRACTITIONER_ID\",\"type\":\"Practitioner\"},{\"type\":\"Device\",\"identifier\":{\"system\":\"https://fhir.kbv.de/NamingSystem/KBV_NS_FOR_Pruefnummer\",\"value\":\"$DEVICE_ID\"}}],\"title\":\"elektronische Arzneimittelverordnung\",\"custodian\":{\"reference\":\"Organization/$ORGANIZATION_ID\"},\"section\":[{\"code\":{\"coding\":[{\"system\":\"https://fhir.kbv.de/CodeSystem/KBV_CS_ERP_Section_Type\",\"code\":\"Prescription\"}]},\"entry\":[{\"reference\":\"MedicationRequest/$MEDICATION_REQUEST_ID\"}]},{\"code\":{\"coding\":[{\"system\":\"https://fhir.kbv.de/CodeSystem/KBV_CS_ERP_Section_Type\",\"code\":\"Coverage\"}]},\"entry\":[{\"reference\":\"Coverage/$COVERAGE_ID\"}]}]}},{\"fullUrl\":\"http://pvs.praxis.local/fhir/MedicationRequest/$MEDICATION_REQUEST_ID\",\"resource\":{\"resourceType\":\"MedicationRequest\",\"id\":\"$MEDICATION_REQUEST_ID\",\"meta\":{\"profile\":[\"https://fhir.kbv.de/StructureDefinition/KBV_PR_ERP_Prescription|1.0.2\"]},\"extension\":[{\"url\":\"https://fhir.kbv.de/StructureDefinition/KBV_EX_ERP_StatusCoPayment\",\"valueCoding\":{\"system\":\"https://fhir.kbv.de/CodeSystem/KBV_CS_ERP_StatusCoPayment\",\"code\":\"$STATUS_CO_PAYMENT\"}},{\"url\":\"https://fhir.kbv.de/StructureDefinition/KBV_EX_ERP_EmergencyServicesFee\",\"valueBoolean\":false},{\"url\":\"https://fhir.kbv.de/StructureDefinition/KBV_EX_ERP_BVG\",\"valueBoolean\":false},{\"url\":\"https://fhir.kbv.de/StructureDefinition/KBV_EX_ERP_Multiple_Prescription\",\"extension\":[{\"url\":\"Kennzeichen\",\"valueBoolean\":false}]}],\"status\":\"active\",\"intent\":\"order\",\"medicationReference\":{\"reference\":\"Medication/$MEDICATION_ID\"},\"subject\":{\"reference\":\"Patient/$PATIENT_ID\"},\"authoredOn\":\"$AUTHORED_ON\",\"requester\":{\"reference\":\"Practitioner/$PRACTITIONER_ID\"},\"insurance\":[{\"reference\":\"Coverage/$COVERAGE_ID\"}],\"dosageInstruction\":[{\"extension\":[{\"url\":\"https://fhir.kbv.de/StructureDefinition/KBV_EX_ERP_DosageFlag\",\"valueBoolean\":true}],\"text\":\"$DOSAGE_QUANTITY\"}],\"dispenseRequest\":{\"quantity\":{\"value\":1,\"system\":\"http://unitsofmeasure.org\",\"code\":\"{Package}\"}},\"substitution\":{\"allowedBoolean\":true}}},{\"fullUrl\":\"http://pvs.praxis.local/fhir/Medication/$MEDICATION_ID\",\"resource\":{\"resourceType\":\"Medication\",\"id\":\"$MEDICATION_ID\",\"meta\":{\"profile\":[\"https://fhir.kbv.de/StructureDefinition/KBV_PR_ERP_Medication_PZN|1.0.2\"]},\"extension\":[{\"url\":\"https://fhir.kbv.de/StructureDefinition/KBV_EX_ERP_Medication_Category\",\"valueCoding\":{\"system\":\"https://fhir.kbv.de/CodeSystem/KBV_CS_ERP_Medication_Category\",\"code\":\"00\"}},{\"url\":\"https://fhir.kbv.de/StructureDefinition/KBV_EX_ERP_Medication_Vaccine\",\"valueBoolean\":false},{\"url\":\"http://fhir.de/StructureDefinition/normgroesse\",\"valueCode\":\"N1\"}],\"code\":{\"coding\":[{\"system\":\"http://fhir.de/CodeSystem/ifa/pzn\",\"code\":\"$PZN\"}],\"text\":\"$MEDICATION_NAME\"},\"form\":{\"coding\":[{\"system\":\"https://fhir.kbv.de/CodeSystem/KBV_CS_SFHIR_KBV_DARREICHUNGSFORM\",\"code\":\"FLE\"}]}}},{\"fullUrl\":\"http://pvs.praxis.local/fhir/Patient/$PATIENT_ID\",\"resource\":{\"resourceType\":\"Patient\",\"id\":\"$PATIENT_ID\",\"meta\":{\"profile\":[\"https://fhir.kbv.de/StructureDefinition/KBV_PR_FOR_Patient|1.0.3\"]},\"identifier\":[{\"type\":{\"coding\":[{\"system\":\"http://fhir.de/CodeSystem/identifier-type-de-basis\",\"code\":\"GKV\"}]},\"system\":\"http://fhir.de/NamingSystem/gkv/kvid-10\",\"value\":\"$KVID_10\"}],\"name\":[{\"use\":\"official\",\"family\":\"$PATIENT_NAME_FAMILY\",\"given\":[\"$PATIENT_NAME_FIRST\"],\"prefix\":[\"$PATIENT_NAME_PREFIX\"]}],\"birthDate\":\"$PATIENT_BIRTH_DATE\",\"address\":[{\"type\":\"both\",\"line\":[\"$PATIENT_ADDRESS_STREET_NUMBER $PATIENT_ADDRESS_STREET_NAME\"],\"_line\":[{\"extension\":[{\"url\":\"http://hl7.org/fhir/StructureDefinition/iso90-ADXP-houseNumber\",\"valueString\":\"$PATIENT_ADDRESS_STREET_NUMBER\"},{\"url\":\"http://hl7.org/fhir/StructureDefinition/iso90-ADXP-streetName\",\"valueString\":\"$PATIENT_ADDRESS_STREET_NAME\"}]}],\"city\":\"$PATIENT_ADDRESS_CITY\",\"postalCode\":\"$PATIENT_ADDRESS_POSTAL_CODE\",\"country\":\"D\"}]}},{\"fullUrl\":\"http://pvs.praxis.local/fhir/Practitioner/$PRACTITIONER_ID\",\"resource\":{\"resourceType\":\"Practitioner\",\"id\":\"$PRACTITIONER_ID\",\"meta\":{\"profile\":[\"https://fhir.kbv.de/StructureDefinition/KBV_PR_FOR_Practitioner|1.0.3\"]},\"identifier\":[{\"type\":{\"coding\":[{\"system\":\"http://terminology.hl7.org/CodeSystem/v2-3\",\"code\":\"LANR\"}]},\"system\":\"https://fhir.kbv.de/NamingSystem/KBV_NS_Base_ANR\",\"value\":\"\"}],\"name\":[{\"use\":\"official\",\"family\":\"$PRACTITIONER_NAME_FAMILY\",\"_family\":{\"extension\":[{\"url\":\"http://hl7.org/fhir/StructureDefinition/humanname-own-name\",\"valueString\":\"$PRACTITIONER_NAME_FAMILY\"}]},\"given\":[\"$PRACTITIONER_NAME_FIRST\"],\"prefix\":[\"$PRACTITIONER_NAME_PREFIX\"],\"_prefix\":[{\"extension\":[{\"url\":\"http://hl7.org/fhir/StructureDefinition/iso90-EN-qualifier\",\"valueCode\":\"AC\"}]}]}],\"qualification\":[{\"code\":{\"coding\":[{\"system\":\"https://fhir.kbv.de/CodeSystem/KBV_CS_FOR_Qualification_Type\",\"code\":\"00\"}]}},{\"code\":{\"text\":\"Arzt-Hausarzt\"}}]}},{\"fullUrl\":\"http://pvs.praxis.local/fhir/Organization/$ORGANIZATION_ID\",\"resource\":{\"resourceType\":\"Organization\",\"id\":\"$ORGANIZATION_ID\",\"meta\":{\"profile\":[\"https://fhir.kbv.de/StructureDefinition/KBV_PR_FOR_Organization|1.0.3\"]},\"identifier\":[{\"type\":{\"coding\":[{\"system\":\"http://terminology.hl7.org/CodeSystem/v2-3\",\"code\":\"BSNR\"}]},\"system\":\"https://fhir.kbv.de/NamingSystem/KBV_NS_Base_BSNR\",\"value\":\"$CLINIC_ID\"}],\"name\":\"Kinderarztpraxis\",\"telecom\":[{\"system\":\"phone\",\"value\":\"$PRACTITIONER_PHONE\"},{\"system\":\"fax\",\"value\":\"$PRACTITIONER_FAX\"}],\"address\":[{\"type\":\"both\",\"line\":[\"$PRACTITIONER_ADDRESS_STREET_NAME $PRACTITIONER_ADDRESS_STREET_NUMBER\"],\"_line\":[{\"extension\":[{\"url\":\"http://hl7.org/fhir/StructureDefinition/iso90-ADXP-houseNumber\",\"valueString\":\"$PRACTITIONER_ADDRESS_STREET_NUMBER\"},{\"url\":\"http://hl7.org/fhir/StructureDefinition/iso90-ADXP-streetName\",\"valueString\":\"$PRACTITIONER_ADDRESS_STREET_NAME\"}]}],\"city\":\"$PRACTITIONER_ADDRESS_CITY\",\"postalCode\":\"$PRACTITIONER_ADDRESS_POSTAL_CODE\",\"country\":\"D\"}]}},{\"fullUrl\":\"http://pvs.praxis.local/fhir/Coverage/$COVERAGE_ID\",\"resource\":{\"resourceType\":\"Coverage\",\"id\":\"$COVERAGE_ID\",\"meta\":{\"profile\":[\"https://fhir.kbv.de/StructureDefinition/KBV_PR_FOR_Coverage|1.0.3\"]},\"extension\":[{\"url\":\"http://fhir.de/StructureDefinition/gkv/besondere-personengruppe\",\"valueCoding\":{\"system\":\"https://fhir.kbv.de/CodeSystem/KBV_CS_SFHIR_KBV_PERSONENGRUPPE\",\"code\":\"00\"}},{\"url\":\"http://fhir.de/StructureDefinition/gkv/dmp-kennzeichen\",\"valueCoding\":{\"system\":\"https://fhir.kbv.de/CodeSystem/KBV_CS_SFHIR_KBV_DMP\",\"code\":\"00\"}},{\"url\":\"http://fhir.de/StructureDefinition/gkv/wop\",\"valueCoding\":{\"system\":\"https://fhir.kbv.de/CodeSystem/KBV_CS_SFHIR_ITA_WOP\",\"code\":\"72\"}},{\"url\":\"http://fhir.de/StructureDefinition/gkv/versichertenart\",\"valueCoding\":{\"system\":\"https://fhir.kbv.de/CodeSystem/KBV_CS_SFHIR_KBV_VERSICHERTENSTATUS\",\"code\":\"$PATIENT_STATUS\"}}],\"status\":\"active\",\"type\":{\"coding\":[{\"system\":\"http://fhir.de/CodeSystem/versicherungsart-de-basis\",\"code\":\"GKV\"}]},\"beneficiary\":{\"reference\":\"Patient/$PATIENT_ID\"},\"period\":{\"end\":\"$COVERAGE_PERIOD_END\"},\"payor\":[{\"identifier\":{\"system\":\"http://fhir.de/NamingSystem/arge-ik/iknr\",\"value\":\"$COVERAGE_ID\"},\"display\":\"$INSURANCE_NAME\"}]}}]}";
    private final FhirContext ctx = FhirContext.forR4();

    @Inject
    Logger logger;

    @Inject
    PrescriptionBundleValidator prescriptionBundleValidator;

    private PrescriptionBundlesBuilder prescriptionBundlesBuilder;

    public static Muster16PrescriptionForm getMuster16PrescriptionFormForTests() {
        Muster16PrescriptionForm muster16PrescriptionForm;
        muster16PrescriptionForm = new Muster16PrescriptionForm();

        muster16PrescriptionForm.setClinicId("123456789");

        muster16PrescriptionForm.setPrescriptionDate("2021-04-05");
        MedicationString medicationString = new MedicationString("Amoxicillin 1000mg N2", null, null, "3x täglich alle 8 Std", null, "02394428");

        muster16PrescriptionForm.setPrescriptionList(Collections.singletonList(medicationString));

        muster16PrescriptionForm.setPractitionerId("123456789");

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
    public void test_Successful_XML_Serialization_Of_An_FHIR_EPrescription_Bundle_Object() {
        IParser parser = ctx.newXmlParser();

        List<Bundle> fhirEPrescriptionBundles = prescriptionBundlesBuilder.createBundles();

        fhirEPrescriptionBundles.forEach(bundle -> {
            parser.setPrettyPrint(true);

            String serialized = parser.encodeResourceToString(bundle);

            logger.info(serialized);
        });
    }

    @Test
    public void test_Name_null() {
        IParser parser = ctx.newXmlParser();

        prescriptionBundlesBuilder.muster16PrescriptionForm.setPractitionerFirstName(null);
        prescriptionBundlesBuilder.muster16PrescriptionForm.setPractitionerLastName(null);

        List<Bundle> fhirEPrescriptionBundles = prescriptionBundlesBuilder.createBundles();

        fhirEPrescriptionBundles.forEach(bundle -> {
            parser.setPrettyPrint(true);

            String serialized = parser.encodeResourceToString(bundle);

            logger.info(serialized);
        });
    }

    @Test
    public void test_Successful_JSON_Serialization_Of_An_FHIR_EPrescription_Bundle_Object() {
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
        IParser jsonParser = ctx.newJsonParser();

        Bundle bundle = jsonParser.parseResource(Bundle.class, GOOD_SIMPLIFIER_NET_SAMPLE_KBV_JSON);
        ValidationResult bundleValidationResult =
                prescriptionBundleValidator.validateResource(bundle, true);

        assertTrue(bundleValidationResult.isSuccessful());
    }

    @Test
    public void test_Successful_Validation_Of_Good_Simplifier_Net_Sample_Used_As_Base_For_Bundle_Creation_Template() throws IOException {
        IParser jsonParser = ctx.newJsonParser();

        try (Reader reader =
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
            throws IOException, XMLStreamException {
        SVGExtractor svgExtractor = new SVGExtractor(TemplateProfile.CGM_Z1.configuration);

        try (PDDocument pdDocument = PDDocument.load(getClass()
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

            if (CollectionUtils.isNotEmpty(bundles)) {
                bundles.stream().forEach(bundle -> {
                    String bundleJsonString = ((EreBundle) bundle).encodeToJson();

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
    public void test_Validation_Of_FHIR_Patient_Resource_With_Missing_Content() {
        Patient patient = new Patient();

        ValidationResult validationResult =
                prescriptionBundleValidator.validateResource(patient, true);
        assertTrue(validationResult.isSuccessful());
    }

    @Test
    public void test_Successful_Validation_Of_An_FHIR_Coverage_Resource() {
        Coverage coverageResource = prescriptionBundlesBuilder.createCoverageResource("random_patient_id");

        ValidationResult validationResult =
                prescriptionBundleValidator.validateResource(coverageResource, true);
        logger.info("messages:" + validationResult.getMessages());
        assertTrue(validationResult.isSuccessful());
    }

    @Test
    public void test_Expected_Validation_Successful_for_JSON_bundle() {
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
    public void test_Successful_Validation_Of_XML_Prescription_Bundle() throws IOException {
        try (InputStream is = getClass().getResourceAsStream(
                "/bundle-samples/bundle_July_2.xml")) {
            byte[] bundleXmlBytes = is.readAllBytes();
            String bundleXmlString = new String(bundleXmlBytes, StandardCharsets.UTF_8);

            ValidationResult bundleValidationResult =
                    prescriptionBundleValidator.validateResource(bundleXmlString, true);

            assertTrue(bundleValidationResult.isSuccessful());
        }
    }

}
