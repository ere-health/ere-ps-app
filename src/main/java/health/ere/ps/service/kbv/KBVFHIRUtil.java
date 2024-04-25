package health.ere.ps.service.kbv;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import org.hl7.fhir.r4.model.Address.AddressType;
import org.hl7.fhir.r4.model.Bundle;
import org.hl7.fhir.r4.model.Bundle.BundleType;
import org.hl7.fhir.r4.model.CodeType;
import org.hl7.fhir.r4.model.CodeableConcept;
import org.hl7.fhir.r4.model.Coding;
import org.hl7.fhir.r4.model.Composition;
import org.hl7.fhir.r4.model.Coverage;
import org.hl7.fhir.r4.model.Extension;
import org.hl7.fhir.r4.model.HumanName;
import org.hl7.fhir.r4.model.HumanName.NameUse;
import org.hl7.fhir.r4.model.Identifier;
import org.hl7.fhir.r4.model.Medication;
import org.hl7.fhir.r4.model.MedicationRequest;
import org.hl7.fhir.r4.model.Organization;
import org.hl7.fhir.r4.model.Patient;
import org.hl7.fhir.r4.model.Practitioner;
import org.hl7.fhir.r4.model.PractitionerRole;
import org.hl7.fhir.r4.model.StringType;

import ca.uhn.fhir.model.api.TemporalPrecisionEnum;
import de.gematik.ws.fa.vsdm.vsd.v5.UCAllgemeineVersicherungsdatenXML;
import de.gematik.ws.fa.vsdm.vsd.v5.UCGeschuetzteVersichertendatenXML;
import de.gematik.ws.fa.vsdm.vsd.v5.UCPersoenlicheVersichertendatenXML;
import de.gematik.ws.fa.vsdm.vsd.v5.UCPersoenlicheVersichertendatenXML.Versicherter;
import de.gematik.ws.fa.vsdm.vsd.v5.UCPersoenlicheVersichertendatenXML.Versicherter.Person;
import de.gematik.ws.fa.vsdm.vsd.v5.UCPersoenlicheVersichertendatenXML.Versicherter.Person.StrassenAdresse;

public class KBVFHIRUtil {
	
	private static Logger log = Logger.getLogger(KBVFHIRUtil.class.getName());

	public static Coverage UCAllgemeineVersicherungsdatenXML2Coverage(UCAllgemeineVersicherungsdatenXML versicherung, String patientId, UCGeschuetzteVersichertendatenXML versichungKennzeichen) {
	    Coverage coverage = new Coverage();
	
	    coverage.setId(UUID.randomUUID().toString());
	    coverage.getMeta()
	            .addProfile("https://fhir.kbv.de/StructureDefinition/KBV_PR_FOR_Coverage|1.1.0");
	
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
	            .setSystem("http://fhir.de/sid/arge-ik/iknr")
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

	public static Patient UCPersoenlicheVersichertendatenXML2Patient(UCPersoenlicheVersichertendatenXML schaumberg) {
	    
	    Versicherter versicherter = schaumberg.getVersicherter();
	    Person person = versicherter.getPerson();
	    Patient patient = new Patient();
	    StrassenAdresse strassenAdresse = person.getStrassenAdresse();
	
	    patient.setId(UUID.randomUUID().toString());
	    patient.getMeta()
	            .addProfile("https://fhir.kbv.de/StructureDefinition/KBV_PR_FOR_Patient|1.1.0");
	
	    Identifier identifier = patient.addIdentifier();
	
	    Coding typeDeBasis = identifier.getType()
	            .addCoding();
	    typeDeBasis.setSystem("http://fhir.de/CodeSystem/identifier-type-de-basis");
	    typeDeBasis.setCode("GKV");
	    identifier.getSystemElement().setValue("http://fhir.de/sid/gkv/kvid-10");
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

	public static Bundle assembleBundle(Practitioner practitioner, Organization organization, Patient patient,
	        Coverage coverage, Medication medication, MedicationRequest medicationRequest, PractitionerRole practitionerRole, Practitioner attester) {
	
	    Bundle bundle = new Bundle();
	
	    bundle.setId(UUID.randomUUID().toString());
	
	    // This will be set by the erezept workflow
	    bundle.setType(BundleType.DOCUMENT);
	
	    bundle.getIdentifier().setSystem("https://gematik.de/fhir/erp/NamingSystem/GEM_ERP_NS_PrescriptionId");
	
	    generateIdentifier(bundle);
	    
	    // Add composition resource.
	    Composition compositionResource = KBVFHIRUtil.createComposition(medicationRequest.getIdElement().getIdPart(), patient.getIdElement().getIdPart(), practitioner.getIdElement().getIdPart(), organization.getIdElement().getIdPart(), coverage.getIdElement().getIdPart(), attester != null ? attester.getIdElement().getIdPart() : null, practitionerRole != null ? practitionerRole.getIdElement().getIdPart() : null);
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
	            .addProfile("https://fhir.kbv.de/StructureDefinition/KBV_PR_ERP_Bundle|1.1.0");
	
	    bundle.setTimestamp(new Date());
	    return bundle;
	}

	public static void generateIdentifier(Bundle bundle) {
	    long part1 = Math.round(100+Math.random()*900);
	    long part2 = Math.round(100+Math.random()*900);
	    long part3 = Math.round(100+Math.random()*900);
	    long part4 = Math.round(100+Math.random()*900);
	
	    long fullNumber = 160*1000000000000l+(part1*1000000000l)+(part2*1000000l)+(part3*1000l)+part4;
	    long lastDigits = 98-(fullNumber % 97);
	
	    bundle.getIdentifier().setValue("160."+part1+"."+part2+"."+part3+"."+part4+"."+(lastDigits<10? "0" : "")+lastDigits);
	}

	public static Composition createComposition(String medicationRequestId, String patientId, String practitionerId, String organizationId, String coverageId, String attesterId, String asvAusuebungRoleId) {
	    Composition composition = new Composition();
	
	    composition.setId(UUID.randomUUID().toString());
	
	    composition.getMeta().addProfile(
	            "https://fhir.kbv.de/StructureDefinition/KBV_PR_ERP_Composition|1.1.0");
	
	    Coding valueCoding = new Coding("https://fhir.kbv.de/CodeSystem/KBV_CS_SFHIR_KBV_STATUSKENNZEICHEN", asvAusuebungRoleId != null ? "01" : "00", null); //todo add 04 Entlassmanagement, 07 TSS Kennzeichen, 10 nur Ersatzverordnungskennzeichen, 11 ASV+Ersatzverordnung, 14 Entlass+Ersatz, 17 TSS+Ersatz
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
