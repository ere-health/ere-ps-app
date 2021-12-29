package health.ere.ps.service.kbv;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.math.BigDecimal;
import java.text.ParseException;
import java.text.SimpleDateFormat;
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
import java.util.stream.Collectors;

import javax.enterprise.context.ApplicationScoped;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;

import org.hl7.fhir.r4.model.Address.AddressType;
import org.hl7.fhir.r4.model.Annotation;
import org.hl7.fhir.r4.model.BooleanType;
import org.hl7.fhir.r4.model.Bundle;
import org.hl7.fhir.r4.model.CodeType;
import org.hl7.fhir.r4.model.CodeableConcept;
import org.hl7.fhir.r4.model.Coding;
import org.hl7.fhir.r4.model.Composition;
import org.hl7.fhir.r4.model.Coverage;
import org.hl7.fhir.r4.model.DateType;
import org.hl7.fhir.r4.model.Extension;
import org.hl7.fhir.r4.model.HumanName;
import org.hl7.fhir.r4.model.HumanName.NameUse;
import org.hl7.fhir.r4.model.Identifier;
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
import org.hl7.fhir.r4.model.StringType;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.model.api.TemporalPrecisionEnum;
import ca.uhn.fhir.parser.DataFormatException;
import de.gematik.ws.fa.vsdm.vsd.v5.UCAllgemeineVersicherungsdatenXML;
import de.gematik.ws.fa.vsdm.vsd.v5.UCGeschuetzteVersichertendatenXML;
import de.gematik.ws.fa.vsdm.vsd.v5.UCPersoenlicheVersichertendatenXML;
import de.gematik.ws.fa.vsdm.vsd.v5.UCPersoenlicheVersichertendatenXML.Versicherter;
import de.gematik.ws.fa.vsdm.vsd.v5.UCPersoenlicheVersichertendatenXML.Versicherter.Person;
import de.gematik.ws.fa.vsdm.vsd.v5.UCPersoenlicheVersichertendatenXML.Versicherter.Person.StrassenAdresse;

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
    
            Bundle bundle = assembleBundle(practitionerKlaus, organization, patientAlthaus, coverage, medication,
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
                generateIdentifier(bundle);
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
        Patient patient = UCPersoenlicheVersichertendatenXML2Patient(schaumberg);

        UCAllgemeineVersicherungsdatenXML versicherung = (UCAllgemeineVersicherungsdatenXML) jaxbContext.createUnmarshaller().unmarshal(new InputStreamReader(new FileInputStream("src/test/resources/kbv-certification-samples/patients/"+patientFile+"/"+patientFile+"_vd.xml"), "ISO-8859-1"));
        UCGeschuetzteVersichertendatenXML versichungKennzeichen = (UCGeschuetzteVersichertendatenXML) jaxbContext.createUnmarshaller().unmarshal(new InputStreamReader(new FileInputStream("src/test/resources/kbv-certification-samples/patients/"+patientFile+"/"+patientFile+"_gvd.xml"), "ISO-8859-1"));
        Coverage coverage = UCAllgemeineVersicherungsdatenXML2Coverage(versicherung, patient.getIdElement().getIdPart(), versichungKennzeichen);
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
        return assembleBundle(practitioner, organization, patientSchaumberg,
                coverage, medication, medicationRequest, null, null);
    }

    private Bundle assembleBundle(Practitioner practitioner, Organization organization, Patient patientSchaumberg,
            Coverage coverage, Medication medication, MedicationRequest medicationRequest, PractitionerRole practitionerRole) {
        return assembleBundle(practitioner, organization, patientSchaumberg,
                coverage, medication, medicationRequest, practitionerRole, null);
    }
    
    private Bundle assembleBundle(Practitioner practitioner, Organization organization, Patient patient,
            Coverage coverage, Medication medication, MedicationRequest medicationRequest, PractitionerRole practitionerRole, Practitioner attester) {

        Bundle bundle = new Bundle();

        bundle.setId(UUID.randomUUID().toString());
   
        // This will be set by the erezept workflow
        bundle.setType(Bundle.BundleType.DOCUMENT);

        bundle.getIdentifier().setSystem("https://gematik.de/fhir/NamingSystem/PrescriptionID");

        generateIdentifier(bundle);
        
        // Add composition resource.
        Composition compositionResource = createComposition(medicationRequest.getIdElement().getIdPart(), patient.getIdElement().getIdPart(), practitioner.getIdElement().getIdPart(), organization.getIdElement().getIdPart(), coverage.getIdElement().getIdPart(), attester != null ? attester.getIdElement().getIdPart() : null, practitionerRole != null ? practitionerRole.getIdElement().getIdPart() : null);
        bundle.addEntry()
                .setFullUrl("http://pvs.praxis.local/fhir/Composition/" + compositionResource.getIdElement().getIdPart())
                .setResource(compositionResource);
        // Add patient resource.
        bundle.addEntry()
                .setFullUrl("http://pvs.praxis.local/fhir/Patient/" +
                    patient.getIdElement().getIdPart())
                .setResource(patient);
   
        // Add medication resource.
        bundle.addEntry()
                .setFullUrl("http://pvs.praxis.local/fhir/Medication/" +
                medication.getIdElement().getIdPart())
                .setResource(medication);
   
        // Add medication request resource.
        bundle.addEntry()
                .setFullUrl("http://pvs.praxis.local/fhir/MedicationRequest/" +
                medicationRequest.getIdElement().getIdPart())
                .setResource(medicationRequest);
   
        // Add practitioner resource.
        bundle.addEntry()
                .setFullUrl("http://pvs.praxis.local/fhir/Practitioner/" +
                        practitioner.getIdElement().getIdPart())
                .setResource(practitioner);
        
        if(attester != null) {
                // Add attester resource.
                bundle.addEntry()
                        .setFullUrl("http://pvs.praxis.local/fhir/Practitioner/" +
                        attester.getIdElement().getIdPart())
                        .setResource(attester);
        }
   
        // Add organization resource.
        bundle.addEntry()
                .setFullUrl("http://pvs.praxis.local/fhir/Organization/" +
                        organization.getIdElement().getIdPart())
                .setResource(organization);
   
        // Add coverage resource.
        bundle.addEntry()
                .setFullUrl("http://pvs.praxis.local/fhir/Coverage/" +
                        coverage.getIdElement().getIdPart())
                .setResource(coverage);

        if(practitionerRole != null) {
                // Add practitionerRole resource.
                bundle.addEntry()
                        .setFullUrl("http://pvs.praxis.local/fhir/PractitionerRole/" +
                                practitionerRole.getIdElement().getIdPart())
                        .setResource(practitionerRole);
        }
   
        // All time related details should be registered after all resources have been created
        // and packaged for transmission.
        bundle.getMeta()
                .setLastUpdated(new Date())
                .addProfile("https://fhir.kbv.de/StructureDefinition/KBV_PR_ERP_Bundle|1.0.2");
   
        bundle.setTimestamp(new Date());
        return bundle;
    }

    private void generateIdentifier(Bundle bundle) {
        long part1 = Math.round(100+Math.random()*900);
        long part2 = Math.round(100+Math.random()*900);
        long part3 = Math.round(100+Math.random()*900);
        long part4 = Math.round(100+Math.random()*900);

        long fullNumber = 160*1000000000000l+(part1*1000000000l)+(part2*1000000l)+(part3*1000l)+part4;
        long lastDigits = 98-(fullNumber % 97);

        bundle.getIdentifier().setValue("160."+part1+"."+part2+"."+part3+"."+part4+"."+(lastDigits<10? "0" : "")+lastDigits);
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

    private Coverage UCAllgemeineVersicherungsdatenXML2Coverage(UCAllgemeineVersicherungsdatenXML versicherung, String patientId, UCGeschuetzteVersichertendatenXML versichungKennzeichen) {
        Coverage coverage = new Coverage();

        coverage.setId(UUID.randomUUID().toString());
        coverage.getMeta()
                .addProfile("https://fhir.kbv.de/StructureDefinition/KBV_PR_FOR_Coverage|1.0.3");

        Coding besonderePersonengruppe = new Coding("https://fhir.kbv.de/CodeSystem/KBV_CS_SFHIR_KBV_PERSONENGRUPPE", String.format("%02d", versichungKennzeichen.getBesonderePersonengruppe() != null ? versichungKennzeichen.getBesonderePersonengruppe() : 0), null);
        Extension besonderePersonengruppeEx = new Extension("http://fhir.de/StructureDefinition/gkv/besondere-personengruppe", besonderePersonengruppe);
        coverage.addExtension(besonderePersonengruppeEx);

        Coding dmp = new Coding("https://fhir.kbv.de/CodeSystem/KBV_CS_SFHIR_KBV_DMP", String.format("%02d", versichungKennzeichen.getDMPKennzeichnung() != null ? versichungKennzeichen.getDMPKennzeichnung() : 0), null);
        Extension dmpEx = new Extension("http://fhir.de/StructureDefinition/gkv/dmp-kennzeichen", dmp);
        coverage.addExtension(dmpEx);

        Coding wop = new Coding("https://fhir.kbv.de/CodeSystem/KBV_CS_SFHIR_ITA_WOP", versicherung.getVersicherter().getZusatzinfos().getZusatzinfosGKV().getZusatzinfosAbrechnungGKV().getWOP(), null);
        Extension wopEx = new Extension("http://fhir.de/StructureDefinition/gkv/wop", wop);
        coverage.addExtension(wopEx);

        String patientStatus = versicherung.getVersicherter().getZusatzinfos().getZusatzinfosGKV().getVersichertenart();
        Coding versichertenart = new Coding("https://fhir.kbv.de/CodeSystem/KBV_CS_SFHIR_KBV_VERSICHERTENSTATUS", patientStatus, null);
        Extension versichertenartEx = new Extension("http://fhir.de/StructureDefinition/gkv/versichertenart", versichertenart);

        coverage.addExtension(versichertenartEx);

        coverage.setStatus(Coverage.CoverageStatus.fromCode("active"));

        coverage.setType(new CodeableConcept().addCoding(new Coding("http://fhir" +
                ".de/CodeSystem/versicherungsart-de-basis", "GKV", "")));

        coverage.getBeneficiary().setReference("Patient/" + patientId);

        String kostentraeger = versicherung.getVersicherter().getVersicherungsschutz().getKostentraeger().getName();
        String iknr = versicherung.getVersicherter().getVersicherungsschutz().getKostentraeger().getKostentraegerkennung().toString();

        if(versicherung.getVersicherter().getVersicherungsschutz().getKostentraeger().getAbrechnenderKostentraeger() != null) {
            kostentraeger = versicherung.getVersicherter().getVersicherungsschutz().getKostentraeger().getAbrechnenderKostentraeger().getName();
            iknr = versicherung.getVersicherter().getVersicherungsschutz().getKostentraeger().getAbrechnenderKostentraeger().getKostentraegerkennung().toString();
        }

        coverage.addPayor()
                .setDisplay(kostentraeger)
                .getIdentifier()
                .setSystem("http://fhir.de/NamingSystem/arge-ik/iknr")
                .setValue(iknr);

        if(versicherung.getVersicherter().getVersicherungsschutz().getBeginn() != null && versicherung.getVersicherter().getVersicherungsschutz().getEnde() != null) {
            try {
                // Date start = new SimpleDateFormat("yyyyMMdd")
                //  .parse(versicherung.getVersicherter().getVersicherungsschutz().getBeginn());
                Date end = new SimpleDateFormat("yyyyMMdd")
                 .parse(versicherung.getVersicherter().getVersicherungsschutz().getEnde());
                // coverage.getPeriod().setStart(start, TemporalPrecisionEnum.DAY);
                coverage.getPeriod().setEnd(end, TemporalPrecisionEnum.DAY);
            } catch (ParseException e) {
                log.warning("Could not parse versicherungsschutz beginn or ende");
            }
        }

        return coverage;
    }

    private Patient UCPersoenlicheVersichertendatenXML2Patient(UCPersoenlicheVersichertendatenXML schaumberg) {
        
        Versicherter versicherter = schaumberg.getVersicherter();
        Person person = versicherter.getPerson();
        Patient patient = new Patient();
        StrassenAdresse strassenAdresse = person.getStrassenAdresse();

        patient.setId(UUID.randomUUID().toString());
        patient.getMeta()
                .addProfile("https://fhir.kbv.de/StructureDefinition/KBV_PR_FOR_Patient|1.0.3");

        Identifier identifier = patient.addIdentifier();

        Coding typeDeBasis = identifier.getType()
                .addCoding();
        typeDeBasis.setSystem("http://fhir.de/CodeSystem/identifier-type-de-basis");
        typeDeBasis.setCode("GKV");
        identifier.getSystemElement().setValue("http://fhir.de/NamingSystem/gkv/kvid-10");
        identifier.setValue(versicherter.getVersichertenID());
        
        List<StringType> prefixList = new ArrayList<StringType>();
        
        if(person.getTitel() != null) {
            StringType prefix = new StringType(person.getTitel());
            Extension extension = new Extension("http://hl7.org/fhir/StructureDefinition/iso21090-EN-qualifier", new CodeType("AC"));
            prefix.addExtension(extension);
            prefixList.add(prefix);
        }
        
        HumanName humanName = patient.addName();

        humanName
                .setUse(NameUse.OFFICIAL)
                .setPrefix(prefixList)
                .addGiven(person.getVorname());
        
        StringType familyElement = humanName.getFamilyElement();
        List<String> nameParts = new ArrayList<>();
        if(person.getNamenszusatz() != null && !"".equals(person.getNamenszusatz())) {
                Extension extension = new Extension("http://fhir.de/StructureDefinition/humanname-namenszusatz", new StringType(person.getNamenszusatz()));
                familyElement.addExtension(extension);
                nameParts.add(person.getNamenszusatz());
        }
        if(person.getVorsatzwort() != null && !"".equals(person.getVorsatzwort())) {
                Extension extension = new Extension("http://hl7.org/fhir/StructureDefinition/humanname-own-prefix", new StringType(person.getVorsatzwort()));
                familyElement.addExtension(extension);
                nameParts.add(person.getVorsatzwort());
        }
        if(person.getNachname() != null && !"".equals(person.getNachname())) {
                Extension extension = new Extension("http://hl7.org/fhir/StructureDefinition/humanname-own-name", new StringType(person.getNachname()));
                familyElement.addExtension(extension);
                nameParts.add(person.getNachname());
        }
        familyElement.setValue(nameParts.stream().collect(Collectors.joining(" ")));


        String patientBirthDate = person.getGeburtsdatum();

        try {
            Date birthdate = new SimpleDateFormat("yyyyMMdd")
            .parse(patientBirthDate);
            patient.setBirthDate(birthdate);
        } catch (ParseException e) {
            log.warning("Could not parse this birthdate when creating the bundle:" + patientBirthDate);
        }

        patient.addAddress()
                .setType(AddressType.BOTH)
                .setCountry(strassenAdresse.getLand().getWohnsitzlaendercode())
                .setCity(strassenAdresse.getOrt())
                .setPostalCode(strassenAdresse.getPostleitzahl())
                .addLine(strassenAdresse.getStrasse() + " " +
                    strassenAdresse.getHausnummer());
        if(strassenAdresse.getAnschriftenzusatz() != null) {
            patient.getAddress().get(0).addLine(strassenAdresse.getAnschriftenzusatz());
            StringType line = patient.getAddress().get(0).getLine().get(1);
            line.addExtension(new Extension("http://hl7.org/fhir/StructureDefinition/iso21090-ADXP-additionalLocator", new StringType(strassenAdresse.getAnschriftenzusatz())));
        }

        StringType line = patient.getAddress().get(0).getLine().get(0);
        line.addExtension("http://hl7.org/fhir/StructureDefinition/iso21090-ADXP-streetName", new StringType(strassenAdresse.getStrasse()));
        line.addExtension("http://hl7.org/fhir/StructureDefinition/iso21090-ADXP-houseNumber", new StringType(strassenAdresse.getHausnummer()));
        return patient;
    }

    private Composition createComposition(String medicationRequestId, String patientId, String practitionerId, String organizationId, String coverageId, String attesterId, String asvAusuebungRoleId) {
        Composition composition = new Composition();

        composition.setId(UUID.randomUUID().toString());

        composition.getMeta().addProfile(
                "https://fhir.kbv.de/StructureDefinition/KBV_PR_ERP_Composition|1.0.2");

        Coding valueCoding = new Coding("https://fhir.kbv.de/CodeSystem/KBV_CS_SFHIR_KBV_STATUSKENNZEICHEN", asvAusuebungRoleId != null ? "01" : "00", null);
        Extension legalBasis = new Extension("https://fhir.kbv.de/StructureDefinition/KBV_EX_FOR_Legal_basis", valueCoding);
        composition.addExtension(legalBasis);

        composition.setStatus(Composition.CompositionStatus.FINAL)
                .getType()
                .addCoding()
                .setSystem("https://fhir.kbv.de/CodeSystem/KBV_CS_SFHIR_KBV_FORMULAR_ART")
                .setCode("e16A");

        composition.getSubject().setReference("Patient/" + patientId);

        composition.setDate(new Date());

        composition.addAuthor()
                .setReference("Practitioner/" + practitionerId)
                .setType("Practitioner");

        composition.addAuthor()
                .setType("Device")
                .getIdentifier()
                .setSystem("https://fhir.kbv.de/NamingSystem/KBV_NS_FOR_Pruefnummer")
                .setValue("Y/400/1904/36/112");

        composition.setTitle("elektronische Arzneimittelverordnung");

        if(attesterId != null) {
            composition.addAttester()
                    .setMode(Composition.CompositionAttestationMode.LEGAL)
                    .getParty().setReference("Practitioner/" +attesterId);
        }

        composition.getCustodian().setReference(
                "Organization/" + organizationId);

        Composition.SectionComponent sectionComponent = composition.addSection();

        sectionComponent.getCode().addCoding()
                .setSystem("https://fhir.kbv.de/CodeSystem/KBV_CS_ERP_Section_Type")
                .setCode("Prescription");
        sectionComponent.addEntry()
                .setReference("MedicationRequest/" + medicationRequestId);

        sectionComponent = composition.addSection();

        sectionComponent.getCode().addCoding()
                .setSystem("https://fhir.kbv.de/CodeSystem/KBV_CS_ERP_Section_Type")
                .setCode("Coverage");
        sectionComponent.addEntry()
                .setReference("Coverage/" + coverageId);

        if(asvAusuebungRoleId != null) {
            sectionComponent = composition.addSection();

            sectionComponent.getCode().addCoding()
                    .setSystem("https://fhir.kbv.de/CodeSystem/KBV_CS_ERP_Section_Type")
                    .setCode("FOR_PractitionerRole");
            sectionComponent.addEntry()
                    .setReference("PractitionerRole/" + asvAusuebungRoleId);
        }

        return composition;
    }
}
