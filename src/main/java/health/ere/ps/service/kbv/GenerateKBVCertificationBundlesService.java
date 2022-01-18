package health.ere.ps.service.kbv;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.enterprise.context.ApplicationScoped;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;

import org.hl7.fhir.r4.model.Annotation;
import org.hl7.fhir.r4.model.BooleanType;
import org.hl7.fhir.r4.model.Bundle;
import org.hl7.fhir.r4.model.CodeType;
import org.hl7.fhir.r4.model.CodeableConcept;
import org.hl7.fhir.r4.model.Coding;
import org.hl7.fhir.r4.model.Coverage;
import org.hl7.fhir.r4.model.DateType;
import org.hl7.fhir.r4.model.Extension;
import org.hl7.fhir.r4.model.Medication;
import org.hl7.fhir.r4.model.Medication.MedicationIngredientComponent;
import org.hl7.fhir.r4.model.MedicationRequest;
import org.hl7.fhir.r4.model.MedicationRequest.MedicationRequestDispenseRequestComponent;
import org.hl7.fhir.r4.model.MedicationRequest.MedicationRequestSubstitutionComponent;
import org.hl7.fhir.r4.model.Organization;
import org.hl7.fhir.r4.model.Patient;
import org.hl7.fhir.r4.model.Period;
import org.hl7.fhir.r4.model.Practitioner;
import org.hl7.fhir.r4.model.PractitionerRole;
import org.hl7.fhir.r4.model.Quantity;
import org.hl7.fhir.r4.model.Ratio;
import org.hl7.fhir.r4.model.Resource;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.model.api.TemporalPrecisionEnum;
import ca.uhn.fhir.parser.DataFormatException;
import de.gematik.ws.fa.vsdm.vsd.v5.UCAllgemeineVersicherungsdatenXML;
import de.gematik.ws.fa.vsdm.vsd.v5.UCGeschuetzteVersichertendatenXML;
import de.gematik.ws.fa.vsdm.vsd.v5.UCPersoenlicheVersichertendatenXML;

@ApplicationScoped
public class GenerateKBVCertificationBundlesService {

    private static Logger log = Logger.getLogger(GenerateKBVCertificationBundlesService.class.getName());

    private final FhirContext fhirContext = FhirContext.forR4();

    static JAXBContext jaxbContext; 

    static {
        try {
            jaxbContext = JAXBContext.newInstance(UCPersoenlicheVersichertendatenXML.class, UCAllgemeineVersicherungsdatenXML.class, UCGeschuetzteVersichertendatenXML.class);
        } catch (JAXBException e) {
            log.log(Level.SEVERE, "Could not init jaxb context", e);
        }
    }

    public Bundle PF01() {
        int quantity = 2;
        String note  = null;
        return PF01_PF02("11536100", "Entresto® 49 mg/51 mg 20 Filmtbl. N1", quantity, note);
    }

    public Bundle PF02() {
        int quantity = 1;
        String note  = "bitte Dosierung deutlich mitgeben";
        return PF01_PF02("11126514", "Entresto® 49 mg/51 mg 56 Filmtbl. N2", quantity, note);
    }

    public Bundle PF03() {

        try {
            String doctorFileName = "src/test/resources/kbv-certification-samples/doctors/838382201.xml";
            List<Resource> resources = getDoctor(doctorFileName);
            Practitioner practitioner = (Practitioner)resources.get(0);
            Organization organization = (Organization) resources.get(1);
        
            String patientFile = "XML_09";
            List<Object> list = getVersichertenDaten(patientFile);

            Patient patientMueller = (Patient)list.get(0);
            Coverage coverage = (Coverage)list.get(1);

            String freitext = "Triamcinolonacetonid 0,1% in Basiscreme DAC 75g 1x tgl. dünn auf die betroffene Stelle auftragen";

            Medication medication = createFreeTextMedicationResource(freitext, "Creme");
        
            MedicationRequest medicationRequest = createMedicationRequest(medication.getIdElement().getIdPart(), patientMueller.getIdElement().getIdPart(), practitioner.getIdElement().getIdPart(), coverage.getIdElement().getIdPart(), "", new BigDecimal(1), "");

            Bundle bundle = assembleBundle(practitioner, organization, patientMueller, coverage, medication,
                medicationRequest);

            return bundle;
        } catch (Exception e) {
            log.log(Level.SEVERE, "Could not generate bundles", e);
            return null;
        }
    }

    public Bundle PF04() {
        int quantity = 1;
        return PF01_PF02_PF04_PF05("00102999", "Twinrix® Erwachsene Eurim, Injektionssuspension", quantity, "", "XML_09", true, false, true, null);
    }

    public Bundle PF05() {
        int quantity = 1;
        return PF01_PF02_PF04_PF05("03716124", "Janumet® 50 mg/850 mg 196 Filmtabletten N3", quantity, "", "XML_09", false, true, false, "1 Tablette, 2x täglich, morgens und abends, zu der Mahlzeit");
    }

    // PF06 needs only additional screenshots from frontend

    public Bundle PF07() {
        try {
            String doctorFileName = "src/test/resources/kbv-certification-samples/doctors/838382201.xml";
            List<Resource> resources = getDoctor(doctorFileName);
            Practitioner practitionerAnnie = (Practitioner)resources.get(0);
            Organization organization = (Organization) resources.get(1);
            // PractitionerRole practitionerRole = (PractitionerRole) resources.get(2);

            doctorFileName = "src/test/resources/kbv-certification-samples/doctors/728382503.xml";
            resources = getDoctor(doctorFileName);
            Practitioner practitionerKlaus = (Practitioner)resources.get(0);
            
            List<Object> list = getVersichertenDaten("XML_05");
    
            Patient patientAlthaus = (Patient)list.get(0);
            Coverage coverage = (Coverage)list.get(1);
    
            Medication medication = createMedicationResource("01016144", "Ibuprofen AbZ 800mg 50 Filmtbl. N2", false);
           
            MedicationRequest medicationRequest = createMedicationRequest(medication.getIdElement().getIdPart(), patientAlthaus.getIdElement().getIdPart(), practitionerKlaus.getIdElement().getIdPart(), coverage.getIdElement().getIdPart(), "", new BigDecimal(1), "", false, true, true, true);
    
            Bundle bundle = KBVFHIRUtil.assembleBundle(practitionerKlaus, organization, patientAlthaus, coverage, medication,
                medicationRequest, null, practitionerAnnie);
    
            return bundle;
        } catch (Exception e) {
            log.log(Level.SEVERE, "Could not generate bundles", e);
            return null;
        }
    }

    public List<Bundle> PF08() {
        try {
            String doctorFileName = "src/test/resources/kbv-certification-samples/doctors/838382201.xml";
            List<Resource> resources = getDoctor(doctorFileName);
            Practitioner practitionerAnnie = (Practitioner)resources.get(0);
            Organization organization = (Organization) resources.get(1);
            
            List<Object> list = getVersichertenDaten("XML_34");
    
            Patient patientWerner = (Patient)list.get(0);
            Coverage coverage = (Coverage)list.get(1);
    
            Medication medication = createMedicationResource("04562798", "HerzASS-ratiopharm® 50mg 100 Tbl. N3", false);

            List<Bundle> bundles = new ArrayList<>();
            LocalDateTime today =  LocalDateTime.now();     //Today
            for(int i =1;i<=3;i++) {
                Map<String,Object> mehrfachverordnung = new HashMap<>();
                mehrfachverordnung.put("numerator", i);
                mehrfachverordnung.put("denominator", 3);

                if(i == 1) {
                    mehrfachverordnung.put("start", Date.from(today.atZone(ZoneId.systemDefault()).toInstant()));
                    mehrfachverordnung.put("end", Date.from(today.plusDays(90).atZone(ZoneId.systemDefault()).toInstant()));

                } else if(i == 2) {
                    mehrfachverordnung.put("start", Date.from(today.plusDays(75).atZone(ZoneId.systemDefault()).toInstant()));
                    mehrfachverordnung.put("end", Date.from(today.plusDays(180).atZone(ZoneId.systemDefault()).toInstant()));
                } else if(i == 3) {
                    mehrfachverordnung.put("start", Date.from(today.plusDays(135).atZone(ZoneId.systemDefault()).toInstant()));
                }

                MedicationRequest medicationRequest = createMedicationRequest(medication.getIdElement().getIdPart(), patientWerner.getIdElement().getIdPart(), practitionerAnnie.getIdElement().getIdPart(), coverage.getIdElement().getIdPart(), "", new BigDecimal(1), "", false, true, false, false, mehrfachverordnung, true);
        
                Bundle bundle = assembleBundle(practitionerAnnie, organization, patientWerner, coverage, medication,
                        medicationRequest);
                KBVFHIRUtil.generateIdentifier(bundle);
                bundles.add(bundle);
            }
    
            return bundles;
        } catch (Exception e) {
            log.log(Level.SEVERE, "Could not generate bundles", e);
            return null;
        }
    }

    public Bundle PF09() {
        try {
            String doctorFileName = "src/test/resources/kbv-certification-samples/doctors/838382201.xml";
            List<Resource> resources = getDoctor(doctorFileName);
            Practitioner practitionerAnnie = (Practitioner)resources.get(0);
            Organization organization = (Organization) resources.get(1);
            PractitionerRole practitionerRole = (PractitionerRole) resources.get(2);
            
            List<Object> list = getVersichertenDaten("XML_37");
    
            Patient patientWerner = (Patient)list.get(0);
            Coverage coverage = (Coverage)list.get(1);
    
            Medication medication = createMedicationResource("01672693", "CAPVAL® Saft 25 mg/5 g, 100ml Suspension N1", false);

            MedicationRequest medicationRequest = createMedicationRequest(medication.getIdElement().getIdPart(), patientWerner.getIdElement().getIdPart(), practitionerAnnie.getIdElement().getIdPart(), coverage.getIdElement().getIdPart(), "3x täglich 5ml", new BigDecimal(1), "", true, true, false, false, null, false, "1");
        
            Bundle bundle = assembleBundle(practitionerAnnie, organization, patientWerner, coverage, medication,
                    medicationRequest, practitionerRole);
    
            return bundle;
        } catch (Exception e) {
            log.log(Level.SEVERE, "Could not generate bundles", e);
            return null;
        }
    }

    public Bundle PF10() {
        try {
            String doctorFileName = "src/test/resources/kbv-certification-samples/doctors/838382201.xml";
            List<Resource> resources = getDoctor(doctorFileName);
            Practitioner practitionerAnnie = (Practitioner)resources.get(0);
            Organization organization = (Organization) resources.get(1);
            
            List<Object> list = getVersichertenDaten("XML_37");
    
            Patient patientIngrid = (Patient)list.get(0);
            Coverage coverage = (Coverage)list.get(1);
    
            Medication medication = createMedicationIngredientResource("5682", "Ibuprofen", 600, "mg", "Tabletten", 20, "Stück");

            MedicationRequest medicationRequest = createMedicationRequest(medication.getIdElement().getIdPart(), patientIngrid.getIdElement().getIdPart(), practitionerAnnie.getIdElement().getIdPart(), coverage.getIdElement().getIdPart(), "1-0-1-0", new BigDecimal(1), "", true, true, false, false, null, false, "1");
        
            Bundle bundle = assembleBundle(practitionerAnnie, organization, patientIngrid, coverage, medication,
                    medicationRequest);
    
            return bundle;
        } catch (Exception e) {
            log.log(Level.SEVERE, "Could not generate bundles", e);
            return null;
        }
    }

    private Medication createMedicationIngredientResource(String wirkstoffnummer, String wirkstoffname, int wirkstaerke, String wirkstaerkeEinheit,
            String darreichungsform, int packunggroesse, String packunggroesseEinheit) {
        Medication medication = new Medication();

        medication.setId(UUID.randomUUID().toString())
                .getMeta()
                .addProfile("https://fhir.kbv.de/StructureDefinition/KBV_PR_ERP_Medication_Ingredient|1.0.2");


        Coding medicationCategory = new Coding("https://fhir.kbv.de/CodeSystem/KBV_CS_ERP_Medication_Category", "00", null);
        Extension medicationCategoryEx = new Extension("https://fhir.kbv.de/StructureDefinition/KBV_EX_ERP_Medication_Category", medicationCategory);
        medication.addExtension(medicationCategoryEx);

        Extension medicationVaccine = new Extension("https://fhir.kbv.de/StructureDefinition/KBV_EX_ERP_Medication_Vaccine", new BooleanType(false));
        medication.addExtension(medicationVaccine);

        medication.getCode().addCoding().setSystem("https://fhir.kbv.de/CodeSystem/KBV_CS_ERP_Medication_Type").setCode("wirkstoff");

        medication.getForm().setText(darreichungsform);

        medication.getAmount().setNumerator(new Quantity(packunggroesse));
        medication.getAmount().getNumerator().setUnit(packunggroesseEinheit);
        medication.getAmount().setDenominator(new Quantity(1));

        MedicationIngredientComponent theIngredient = new MedicationIngredientComponent();

        Coding formCoding = new Coding("http://fhir.de/CodeSystem/ask", wirkstoffnummer, "");
        CodeableConcept item = new CodeableConcept().addCoding(formCoding).setText(wirkstoffname);

        theIngredient.setItem(item);

        theIngredient.getStrength().setNumerator(new Quantity(wirkstaerke));
        theIngredient.getStrength().getNumerator().setUnit(wirkstaerkeEinheit);

        theIngredient.getStrength().setDenominator(new Quantity(1));
        medication.setIngredient(Arrays.asList(theIngredient));


        return medication;
    }

    private Bundle PF01_PF02(String pzn, String medicationText, int quantity, String note) {
        return PF01_PF02_PF04_PF05(pzn, medicationText, quantity, note, "XML_01", false, true, true, "0-0-1-0");
    }

    private Bundle PF01_PF02_PF04_PF05(String pzn, String medicationText, int quantity, String note, String patientFile, boolean medicationVaccineFlag, boolean dosageFlag, boolean substitutable, String dosageInstruction) {
        try {
            String doctorFileName = "src/test/resources/kbv-certification-samples/doctors/838382201.xml";
            List<Resource> resources = getDoctor(doctorFileName);
            Practitioner practitioner = (Practitioner)resources.get(0);
            Organization organization = (Organization) resources.get(1);
            
            List<Object> list = getVersichertenDaten(patientFile);

            Patient patientSchaumberg = (Patient)list.get(0);
            Coverage coverage = (Coverage)list.get(1);

            Medication medication = createMedicationResource(pzn, medicationText, medicationVaccineFlag);
        
            MedicationRequest medicationRequest = createMedicationRequest(medication.getIdElement().getIdPart(), patientSchaumberg.getIdElement().getIdPart(), practitioner.getIdElement().getIdPart(), coverage.getIdElement().getIdPart(), dosageInstruction, new BigDecimal(quantity), note, dosageFlag, substitutable);

            Bundle bundle = assembleBundle(practitioner, organization, patientSchaumberg, coverage, medication,
                medicationRequest);

            return bundle;
        } catch (Exception e) {
            log.log(Level.SEVERE, "Could not generate bundles", e);
            return null;
        }
    }

    private List<Object> getVersichertenDaten(String patientFile) throws UnsupportedEncodingException, FileNotFoundException, JAXBException {
        UCPersoenlicheVersichertendatenXML schaumberg = (UCPersoenlicheVersichertendatenXML) jaxbContext.createUnmarshaller().unmarshal(new InputStreamReader(new FileInputStream("src/test/resources/kbv-certification-samples/patients/"+patientFile+"/"+patientFile+"_pd.xml"), "ISO-8859-1"));
        Patient patient = KBVFHIRUtil.UCPersoenlicheVersichertendatenXML2Patient(schaumberg);

        UCAllgemeineVersicherungsdatenXML versicherung = (UCAllgemeineVersicherungsdatenXML) jaxbContext.createUnmarshaller().unmarshal(new InputStreamReader(new FileInputStream("src/test/resources/kbv-certification-samples/patients/"+patientFile+"/"+patientFile+"_vd.xml"), "ISO-8859-1"));
        UCGeschuetzteVersichertendatenXML versichungKennzeichen = (UCGeschuetzteVersichertendatenXML) jaxbContext.createUnmarshaller().unmarshal(new InputStreamReader(new FileInputStream("src/test/resources/kbv-certification-samples/patients/"+patientFile+"/"+patientFile+"_gvd.xml"), "ISO-8859-1"));
        Coverage coverage = KBVFHIRUtil.UCAllgemeineVersicherungsdatenXML2Coverage(versicherung, patient.getIdElement().getIdPart(), versichungKennzeichen);
        return Arrays.asList(patient, coverage);

    }

    public List<Resource> getDoctor(String doctorFileName) throws DataFormatException, FileNotFoundException {
        Bundle doctor = fhirContext.newXmlParser().parseResource(Bundle.class, new FileInputStream(doctorFileName));
        Resource practitioner = doctor.getEntry().stream().filter(d -> d.getResource() instanceof Practitioner).findAny().get().getResource();
        Optional<Bundle.BundleEntryComponent> organization = doctor.getEntry().stream().filter(d -> d.getResource() instanceof Organization).findAny();
        Optional<Bundle.BundleEntryComponent> practitionerRole = doctor.getEntry().stream().filter(d -> d.getResource() instanceof PractitionerRole).findAny();
        return Arrays.asList(practitioner, organization.isPresent() ? organization.get().getResource() : null, practitionerRole.isPresent() ? practitionerRole.get().getResource() : null); 
    }

    private Bundle assembleBundle(Practitioner practitioner, Organization organization, Patient patientSchaumberg,
            Coverage coverage, Medication medication, MedicationRequest medicationRequest) {
        return KBVFHIRUtil.assembleBundle(practitioner, organization, patientSchaumberg,
                coverage, medication, medicationRequest, null, null);
    }

    private Bundle assembleBundle(Practitioner practitioner, Organization organization, Patient patientSchaumberg,
            Coverage coverage, Medication medication, MedicationRequest medicationRequest, PractitionerRole practitionerRole) {
        return KBVFHIRUtil.assembleBundle(practitioner, organization, patientSchaumberg,
                coverage, medication, medicationRequest, practitionerRole, null);
    }
    
    private Medication createMedicationResource(String pzn, String name, boolean medicationVaccineFlag) {
        Medication medication = new Medication();

        medication.setId(UUID.randomUUID().toString())
                .getMeta()
                .addProfile("https://fhir.kbv.de/StructureDefinition/KBV_PR_ERP_Medication_PZN|1.0.2");


        Coding medicationCategory = new Coding("https://fhir.kbv.de/CodeSystem/KBV_CS_ERP_Medication_Category", "00", null);
        Extension medicationCategoryEx = new Extension("https://fhir.kbv.de/StructureDefinition/KBV_EX_ERP_Medication_Category", medicationCategory);
        medication.addExtension(medicationCategoryEx);

        Extension medicationVaccine = new Extension("https://fhir.kbv.de/StructureDefinition/KBV_EX_ERP_Medication_Vaccine", new BooleanType(medicationVaccineFlag));
        medication.addExtension(medicationVaccine);

        String normgroesseString= "N1";
        Pattern p = Pattern.compile(".* (N\\d).*");
        Matcher m = p.matcher(name);
        if(m.matches()) {
            normgroesseString = m.group(1);
            name = name.replace(" "+normgroesseString, "");
        }

        Extension normgroesse = new Extension("http://fhir.de/StructureDefinition/normgroesse", new CodeType(normgroesseString));
        medication.addExtension(normgroesse);

        medication.getCode().addCoding().setSystem("http://fhir.de/CodeSystem/ifa/pzn").setCode(pzn);
        medication.getCode().setText(name);
        // PF01 & PF02 11536100 & 11126514
        // PF03 03716124
        // PF07 01016144
        String darreichungsform = "FTA";
        if(pzn.equals("00102999")) {
            darreichungsform = "ISU";
        // PF08
        } else if(pzn.equals("04562798")) {
            darreichungsform = "TAB";
        // PF09
        } else if(pzn.equals("01672693")) {
            darreichungsform = "SUE";
        }

        Coding formCoding = new Coding("https://fhir.kbv.de/CodeSystem/KBV_CS_SFHIR_KBV_DARREICHUNGSFORM", darreichungsform, "");
        medication.setForm(new CodeableConcept().addCoding(formCoding));

        return medication;
    }

    private Medication createFreeTextMedicationResource(String freetext, String ingreedientForm) {
        Medication medication = new Medication();

        medication.setId(UUID.randomUUID().toString())
                .getMeta()
                .addProfile("https://fhir.kbv.de/StructureDefinition/KBV_PR_ERP_Medication_FreeText|1.0.2");


        Coding medicationCategory = new Coding("https://fhir.kbv.de/CodeSystem/KBV_CS_ERP_Medication_Category", "00", null);
        Extension medicationCategoryEx = new Extension("https://fhir.kbv.de/StructureDefinition/KBV_EX_ERP_Medication_Category", medicationCategory);
        medication.addExtension(medicationCategoryEx);

        Extension medicationVaccine = new Extension("https://fhir.kbv.de/StructureDefinition/KBV_EX_ERP_Medication_Vaccine", new BooleanType(false));
        medication.addExtension(medicationVaccine);

        medication.getCode().addCoding().setSystem("https://fhir.kbv.de/CodeSystem/KBV_CS_ERP_Medication_Type").setCode("freitext");
        medication.getCode().setText(freetext);

        medication.getForm().setText(ingreedientForm);

        return medication;
    }

    private MedicationRequest createMedicationRequest(String medicationId, String patientId, String practitionerId, String coverageId, String dosageInstruction, BigDecimal quantityNumber, String note) {
        return createMedicationRequest(medicationId, patientId, practitionerId, coverageId, dosageInstruction, quantityNumber, note, false);
    }

    private MedicationRequest createMedicationRequest(String medicationId, String patientId, String practitionerId, String coverageId, String dosageInstruction, BigDecimal quantityNumber, String note, boolean dosageFlag) {
        return createMedicationRequest(medicationId, patientId, practitionerId, coverageId, dosageInstruction, quantityNumber, note, dosageFlag, true);
    }

    private MedicationRequest createMedicationRequest(String medicationId, String patientId, String practitionerId, String coverageId, String dosageInstruction, BigDecimal quantityNumber, String note, boolean dosageFlag, boolean substitutable) {
        return createMedicationRequest(medicationId, patientId, practitionerId, coverageId, dosageInstruction, quantityNumber, note, dosageFlag, substitutable, false);
    }

    private MedicationRequest createMedicationRequest(String medicationId, String patientId, String practitionerId, String coverageId, String dosageInstruction, BigDecimal quantityNumber, String note, boolean dosageFlag, boolean substitutable, boolean emergencyServiceFeeParam) {
        return createMedicationRequest(medicationId, patientId, practitionerId, coverageId, dosageInstruction, quantityNumber, note, dosageFlag, substitutable, emergencyServiceFeeParam, false);
    }
    
    private MedicationRequest createMedicationRequest(String medicationId, String patientId, String practitionerId, String coverageId, String dosageInstruction, BigDecimal quantityNumber, String note, boolean dosageFlag, boolean substitutable, boolean emergencyServiceFeeParam, boolean unfallkennzeichen) {
        return createMedicationRequest(medicationId, patientId, practitionerId, coverageId, dosageInstruction, quantityNumber, note, dosageFlag, substitutable, emergencyServiceFeeParam, unfallkennzeichen, null, false);
    }

    private MedicationRequest createMedicationRequest(String medicationId, String patientId, String practitionerId, String coverageId, String dosageInstruction, BigDecimal quantityNumber, String note, boolean dosageFlag, boolean substitutable, boolean emergencyServiceFeeParam, boolean unfallkennzeichen, Map<String,Object> mehrfachverordnung, boolean bvgFlag) {
        return createMedicationRequest(medicationId, patientId, practitionerId, coverageId, dosageInstruction, quantityNumber, note, dosageFlag, substitutable, emergencyServiceFeeParam, unfallkennzeichen, mehrfachverordnung, bvgFlag, "0");
    }

    private MedicationRequest createMedicationRequest(String medicationId, String patientId, String practitionerId, String coverageId, String dosageInstruction, BigDecimal quantityNumber, String note, boolean dosageFlag, boolean substitutable, boolean emergencyServiceFeeParam, boolean unfallkennzeichen, Map<String,Object> mehrfachverordnung, boolean bvgFlag, String statusCoPayment) {
        MedicationRequest medicationRequest = new MedicationRequest();

        medicationRequest.setAuthoredOn(new Date());
        medicationRequest.getAuthoredOnElement().setPrecision(TemporalPrecisionEnum.DAY);


        medicationRequest.setId(UUID.randomUUID().toString());

        medicationRequest.getMeta()
                .addProfile("https://fhir.kbv.de/StructureDefinition/KBV_PR_ERP_Prescription|1.0.2");

        Coding valueCoding = new Coding("https://fhir.kbv.de/CodeSystem/KBV_CS_ERP_StatusCoPayment",
                statusCoPayment, null);
        Extension coPayment = new Extension("https://fhir.kbv.de/StructureDefinition/KBV_EX_ERP_StatusCoPayment", valueCoding);
        medicationRequest.addExtension(coPayment);

        // emergencyServiceFeeParam default false
        Extension emergencyServicesFee = new Extension("https://fhir.kbv.de/StructureDefinition/KBV_EX_ERP_EmergencyServicesFee", new BooleanType(emergencyServiceFeeParam));
        medicationRequest.addExtension(emergencyServicesFee);

        Extension bvg = new Extension("https://fhir.kbv.de/StructureDefinition/KBV_EX_ERP_BVG", new BooleanType(bvgFlag));
        medicationRequest.addExtension(bvg);
        
        //         <extension url="https://fhir.kbv.de/StructureDefinition/KBV_EX_ERP_Multiple_Prescription">
        Extension multiplePrescription = new Extension("https://fhir.kbv.de/StructureDefinition/KBV_EX_ERP_Multiple_Prescription");
        //         <extension url="Kennzeichen">
        //           <valueBoolean value="true" />
        //         </extension>
        multiplePrescription.addExtension(new Extension("Kennzeichen", new BooleanType(mehrfachverordnung != null)));
        //         <extension url="Nummerierung">
        //           <valueRatio>
        //             <numerator>
        //               <value value="2" />
        //             </numerator>
        //             <denominator>
        //               <value value="4" />
        //             </denominator>
        //           </valueRatio>
        //         </extension>
        if(mehrfachverordnung != null) {
            Ratio nummerierungRatio = new Ratio();
            nummerierungRatio.setNumerator(new Quantity((int)mehrfachverordnung.get("numerator")));
            nummerierungRatio.setDenominator(new Quantity((int)mehrfachverordnung.get("denominator")));
            Extension nummerierung = new Extension("Nummerierung", nummerierungRatio);
            multiplePrescription.addExtension(nummerierung);

            Period zeitraumPeriod = new Period();
            zeitraumPeriod.setStart((Date)mehrfachverordnung.get("start"), TemporalPrecisionEnum.DAY);
            if(mehrfachverordnung.get("end") != null) {
                zeitraumPeriod.setEnd((Date)mehrfachverordnung.get("end"), TemporalPrecisionEnum.DAY);
            }
            Extension zeitraum = new Extension("Zeitraum", zeitraumPeriod);
            multiplePrescription.addExtension(zeitraum);
        }
        //         <extension url="Zeitraum">
        //           <valuePeriod>
        //             <start value="2021-01-02" />
        //             <end value="2021-03-30" />
        //           </valuePeriod>
        //         </extension>
        //       </extension>
        medicationRequest.addExtension(multiplePrescription);

        // <extension url="https://fhir.kbv.de/StructureDefinition/KBV_EX_ERP_Accident">
        //  <extension url="unfallkennzeichen">
        //  <valueCoding>
        //    <system value="https://fhir.kbv.de/CodeSystem/KBV_CS_FOR_Ursache_Type" />
        //    <code value="1" />
        //  </valueCoding>
        // </extension>
        // <extension url="unfalltag">
        //  <valueDate value="2020-05-01" />
        // </extension>
        // </extension>

        if(unfallkennzeichen) {
            Extension KBV_EX_ERP_Accident = new Extension("https://fhir.kbv.de/StructureDefinition/KBV_EX_ERP_Accident");
            Coding unfallkennzeichenValueCoding = new Coding("https://fhir.kbv.de/CodeSystem/KBV_CS_FOR_Ursache_Type",
                "1", null);
            KBV_EX_ERP_Accident.addExtension(new Extension("unfallkennzeichen", unfallkennzeichenValueCoding));
            DateType unfalltagDate = new DateType(new Date());
            unfalltagDate.setPrecision(TemporalPrecisionEnum.DAY);
            KBV_EX_ERP_Accident.addExtension(new Extension("unfalltag", unfalltagDate));
            medicationRequest.addExtension(KBV_EX_ERP_Accident);
        }

        medicationRequest.setStatus(MedicationRequest.MedicationRequestStatus.ACTIVE)
                .setIntent(MedicationRequest.MedicationRequestIntent.ORDER)
                .getMedicationReference().setReference("Medication/" + medicationId);

        medicationRequest.getSubject().setReference("Patient/" + patientId);


        medicationRequest.getRequester().setReference(
                "Practitioner/" + practitionerId);

        medicationRequest.addInsurance().setReference(
                "Coverage/" + coverageId);

        if(note != null) {
            medicationRequest.setNote(Arrays.asList(new Annotation().setText(note)));
        }

        medicationRequest.addDosageInstruction().setText(dosageInstruction).addExtension().setUrl(
                "https://fhir.kbv.de/StructureDefinition/KBV_EX_ERP_DosageFlag"
        ).setValue(new BooleanType(dosageFlag));


        MedicationRequest.MedicationRequestDispenseRequestComponent dispenseRequest = new MedicationRequestDispenseRequestComponent();
        Quantity quantity = new Quantity();
        quantity.setValue(quantityNumber);
        quantity.setSystem("http://unitsofmeasure.org");
        quantity.setCode("{Package}");
        dispenseRequest.setQuantity(quantity);
        medicationRequest.setDispenseRequest(dispenseRequest);
        MedicationRequestSubstitutionComponent substitution = new MedicationRequestSubstitutionComponent();
        substitution.setAllowed(new BooleanType(substitutable));
        medicationRequest.setSubstitution(substitution);

        return medicationRequest;
    }
}
