package health.ere.ps.service.gematik;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.cert.CertificateEncodingException;
import java.security.cert.X509Certificate;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.zip.GZIPInputStream;

import javax.naming.InvalidNameException;

import org.bouncycastle.asn1.ASN1ObjectIdentifier;
import org.bouncycastle.asn1.x500.X500Name;
import org.bouncycastle.asn1.x500.style.BCStyle;
import org.bouncycastle.asn1.x500.style.IETFUtils;
import org.bouncycastle.cert.jcajce.JcaX509CertificateHolder;
import org.bouncycastle.crypto.CryptoException;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.hl7.fhir.r4.model.Address.AddressType;
import org.hl7.fhir.r4.model.Annotation;
import org.hl7.fhir.r4.model.BooleanType;
import org.hl7.fhir.r4.model.Bundle;
import org.hl7.fhir.r4.model.CodeType;
import org.hl7.fhir.r4.model.CodeableConcept;
import org.hl7.fhir.r4.model.Coding;
import org.hl7.fhir.r4.model.ContactPoint.ContactPointSystem;
import org.hl7.fhir.r4.model.Coverage;
import org.hl7.fhir.r4.model.Extension;
import org.hl7.fhir.r4.model.HumanName;
import org.hl7.fhir.r4.model.HumanName.NameUse;
import org.hl7.fhir.r4.model.Identifier;
import org.hl7.fhir.r4.model.Medication;
import org.hl7.fhir.r4.model.MedicationRequest;
import org.hl7.fhir.r4.model.MedicationRequest.MedicationRequestDispenseRequestComponent;
import org.hl7.fhir.r4.model.MedicationRequest.MedicationRequestSubstitutionComponent;
import org.hl7.fhir.r4.model.Organization;
import org.hl7.fhir.r4.model.Patient;
import org.hl7.fhir.r4.model.Practitioner;
import org.hl7.fhir.r4.model.Practitioner.PractitionerQualificationComponent;
import org.hl7.fhir.r4.model.Quantity;
import org.hl7.fhir.r4.model.StringType;

import ca.uhn.fhir.model.api.TemporalPrecisionEnum;
import de.gematik.ws.conn.cardservice.v8.CardInfoType;
import de.gematik.ws.conn.cardservicecommon.v2.CardTypeType;
import de.gematik.ws.conn.certificateservice.v6.CryptType;
import de.gematik.ws.conn.certificateservice.v6.ReadCardCertificate;
import de.gematik.ws.conn.certificateservice.wsdl.v6.CertificateServicePortType;
import de.gematik.ws.conn.certificateservicecommon.v2.CertRefEnum;
import de.gematik.ws.conn.certificateservicecommon.v2.X509DataInfoListType;
import de.gematik.ws.conn.connectorcommon.v5.Status;
import de.gematik.ws.conn.connectorcontext.v2.ContextType;
import de.gematik.ws.conn.eventservice.v7.GetCards;
import de.gematik.ws.conn.eventservice.v7.GetCardsResponse;
import de.gematik.ws.conn.eventservice.wsdl.v7.EventServicePortType;
import de.gematik.ws.conn.eventservice.wsdl.v7.FaultMessage;
import de.gematik.ws.conn.vsds.vsdservice.v5.VSDStatusType;
import de.gematik.ws.fa.vsdm.vsd.v5.UCAllgemeineVersicherungsdatenXML;
import de.gematik.ws.fa.vsdm.vsd.v5.UCGeschuetzteVersichertendatenXML;
import de.gematik.ws.fa.vsdm.vsd.v5.UCPersoenlicheVersichertendatenXML;
import health.ere.ps.config.RuntimeConfig;
import health.ere.ps.event.BundlesEvent;
import health.ere.ps.event.PrefillBundleEvent;
import health.ere.ps.service.connector.provider.MultiConnectorServicesProvider;
import health.ere.ps.service.idp.crypto.CryptoLoader;
import health.ere.ps.service.kbv.KBVFHIRUtil;
import health.ere.ps.websocket.ExceptionWithReplyToException;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.Event;
import jakarta.enterprise.event.ObservesAsync;
import jakarta.inject.Inject;
import jakarta.xml.bind.JAXBContext;
import jakarta.xml.bind.JAXBException;
import jakarta.xml.ws.Holder;

@ApplicationScoped
public class PrefillPrescriptionService {

	private static Logger log = Logger.getLogger(PrefillPrescriptionService.class.getName());

	static JAXBContext jaxbContext;

	static Pattern STREET_AND_NUMBER = Pattern.compile("(.*) ([^ ]*)$");

	static {
		try {
			jaxbContext = JAXBContext.newInstance(UCPersoenlicheVersichertendatenXML.class,
					UCAllgemeineVersicherungsdatenXML.class, UCGeschuetzteVersichertendatenXML.class);
		} catch (JAXBException e) {
			log.log(Level.SEVERE, "Could not init jaxb context", e);
		}
	}

	@Inject
	MultiConnectorServicesProvider connectorServicesProvider;

	@Inject
	Event<BundlesEvent> bundleEvent;

	@Inject
	Event<Exception> exceptionEvent;

	public Bundle get(RuntimeConfig runtimeConfig)
		throws FaultMessage, de.gematik.ws.conn.vsds.vsdservice.v5.FaultMessage, JAXBException,
		de.gematik.ws.conn.certificateservice.wsdl.v6.FaultMessage, CryptoException, IOException,
		InvalidNameException, CertificateEncodingException {
			return get(runtimeConfig, null);
	}
	
	public Bundle get(RuntimeConfig runtimeConfig, String egkHandleParameter)
			throws FaultMessage, de.gematik.ws.conn.vsds.vsdservice.v5.FaultMessage, JAXBException,
			de.gematik.ws.conn.certificateservice.wsdl.v6.FaultMessage, CryptoException, IOException,
			InvalidNameException, CertificateEncodingException {
		return get(runtimeConfig, egkHandleParameter, null);
	}


	
	public Bundle get(RuntimeConfig runtimeConfig, String egkHandleParameter, String smcbHandleParameter)
			throws FaultMessage, de.gematik.ws.conn.vsds.vsdservice.v5.FaultMessage, JAXBException,
			de.gematik.ws.conn.certificateservice.wsdl.v6.FaultMessage, CryptoException, IOException,
			InvalidNameException, CertificateEncodingException {
		return get(runtimeConfig, egkHandleParameter, smcbHandleParameter, null);
	}


	public Bundle get(RuntimeConfig runtimeConfig, String egkHandleParameter, String smcbHandleParameter, String hbaHandleParameter)
			throws FaultMessage, de.gematik.ws.conn.vsds.vsdservice.v5.FaultMessage, JAXBException,
			de.gematik.ws.conn.certificateservice.wsdl.v6.FaultMessage, CryptoException, IOException,
			InvalidNameException, CertificateEncodingException {
		// Get data from egk
		ContextType context = connectorServicesProvider.getContextType(runtimeConfig);

		EventServicePortType eventService = connectorServicesProvider.getEventServicePortType(runtimeConfig);

		String egkHandle = egkHandleParameter != null ? egkHandleParameter : getFirstCardOfType(eventService, CardTypeType.EGK, context);
		String smcbHandle = smcbHandleParameter != null ? smcbHandleParameter : (runtimeConfig != null && runtimeConfig.getSMCBHandle() != null) ? runtimeConfig.getSMCBHandle() : getFirstCardOfType(eventService, CardTypeType.SMC_B, context);
		String hbaHandle = hbaHandleParameter != null ? hbaHandleParameter : (runtimeConfig != null && runtimeConfig.getEHBAHandle() != null) ? runtimeConfig.getEHBAHandle() : getFirstCardOfType(eventService, CardTypeType.HBA, context);

		Patient patient = null;
		Coverage coverage = null;
		if (egkHandle != null) {
			Holder<byte[]> persoenlicheVersichertendaten = new Holder<>();
			Holder<byte[]> allgemeineVersicherungsdaten = new Holder<>();
			Holder<byte[]> geschuetzteVersichertendaten = new Holder<>();
			Holder<VSDStatusType> vSD_Status = new Holder<>();
			Holder<byte[]> pruefungsnachweis = new Holder<>();
			connectorServicesProvider.getVSDServicePortType(runtimeConfig).readVSD(egkHandle, smcbHandle, false, false,
					context, persoenlicheVersichertendaten, allgemeineVersicherungsdaten, geschuetzteVersichertendaten,
					vSD_Status, pruefungsnachweis);

			InputStream isPersoenlicheVersichertendaten = new GZIPInputStream(
					new ByteArrayInputStream(persoenlicheVersichertendaten.value));
			UCPersoenlicheVersichertendatenXML schaumberg = (UCPersoenlicheVersichertendatenXML) jaxbContext
					.createUnmarshaller().unmarshal(isPersoenlicheVersichertendaten);
			patient = KBVFHIRUtil.UCPersoenlicheVersichertendatenXML2Patient(schaumberg);

			InputStream isAllgemeineVersicherungsdaten = new GZIPInputStream(
					new ByteArrayInputStream(allgemeineVersicherungsdaten.value));
			UCAllgemeineVersicherungsdatenXML versicherung = (UCAllgemeineVersicherungsdatenXML) jaxbContext
					.createUnmarshaller().unmarshal(isAllgemeineVersicherungsdaten);
			InputStream isVersichungKennzeichen = new GZIPInputStream(
					new ByteArrayInputStream(geschuetzteVersichertendaten.value));
			UCGeschuetzteVersichertendatenXML versichungKennzeichen = (UCGeschuetzteVersichertendatenXML) jaxbContext
					.createUnmarshaller().unmarshal(isVersichungKennzeichen);
			coverage = KBVFHIRUtil.UCAllgemeineVersicherungsdatenXML2Coverage(versicherung,
					patient.getIdElement().getIdPart(), versichungKennzeichen);
		}

		CertificateServicePortType certificateService = connectorServicesProvider
				.getCertificateServicePortType(runtimeConfig);

		Practitioner practitioner = null;

		if (hbaHandle != null) {
			practitioner = hbaHandle2Practitioner(hbaHandle, runtimeConfig, certificateService, context);
		}

		Organization organization = null;

		if (smcbHandle != null) {
			organization = smcbHandle2Organization(smcbHandle, runtimeConfig, certificateService, context);
		}

		Medication medication = createMedicationResource();

		MedicationRequest medicationRequest = createMedicationRequest(medication.getIdElement().getIdPart(),
				patient.getIdElement().getIdPart(), practitioner.getIdElement().getIdPart(),
				coverage.getIdElement().getIdPart());

		Bundle bundle = KBVFHIRUtil.assembleBundle(practitioner, organization, patient, coverage, medication,
				medicationRequest, null, null);

		return bundle;
	}

	private Organization smcbHandle2Organization(String hbaHandle, RuntimeConfig runtimeConfig,
			CertificateServicePortType certificateService, ContextType context)
			throws de.gematik.ws.conn.certificateservice.wsdl.v6.FaultMessage, CryptoException,
			CertificateEncodingException {

		CertRefEnum certRef = CertRefEnum.C_AUT;

		X509Certificate x509Certificate = getCertificateFor(hbaHandle, certificateService, context, certRef);
		X500Name x500name = new JcaX509CertificateHolder(x509Certificate).getSubject();

		// C=DE,L=Freiburg,PostalCode=79114,STREET=Sundgauallee
		// 59,SERIALNUMBER=80276883110000118001,CN=VincenzkrankenhausTEST-ONLY

		String bsnr = "";
		String phone = "";
		String city = getRdnValue(x500name, BCStyle.L);
		String postalCode = getRdnValue(x500name, BCStyle.POSTAL_CODE);

		String streetName = "";
		String houseNumber = "";

		String street = getRdnValue(x500name, BCStyle.STREET);
		Matcher m = STREET_AND_NUMBER.matcher(street);
		if (m.matches()) {
			streetName = m.group(1);
			houseNumber = m.group(2);
		} else {
			streetName = street;
		}

		String organizationName = getRdnValue(x500name, BCStyle.CN);

		Organization organization = new Organization();

		organization.setId(UUID.randomUUID().toString()).getMeta()
				.addProfile("https://fhir.kbv.de/StructureDefinition/KBV_PR_FOR_Organization|1.1.0");

		Identifier identifier = organization.addIdentifier();

		CodeableConcept codeableConcept = identifier.getType();

		codeableConcept.addCoding().setSystem("http://terminology.hl7.org/CodeSystem/v2-0203").setCode("BSNR");

		identifier.setSystem("https://fhir.kbv.de/NamingSystem/KBV_NS_Base_BSNR");
		identifier.setValue(bsnr);

		organization.setName(organizationName);

		organization.addTelecom().setSystem(ContactPointSystem.PHONE).setValue(phone);

		organization.addAddress().setType(AddressType.BOTH).setCity(city).setPostalCode(postalCode)
				.addLine(streetName + " " + houseNumber).setCountry("D");

		StringType line = organization.getAddress().get(0).getLine().get(0);
		line.addExtension("http://hl7.org/fhir/StructureDefinition/iso21090-ADXP-streetName",
				new StringType(streetName));
		line.addExtension("http://hl7.org/fhir/StructureDefinition/iso21090-ADXP-houseNumber",
				new StringType(houseNumber));

		return organization;
	}

	private Practitioner hbaHandle2Practitioner(String hbaHandle, RuntimeConfig runtimeConfig,
			CertificateServicePortType certificateService, ContextType context)
			throws de.gematik.ws.conn.certificateservice.wsdl.v6.FaultMessage, CryptoException, InvalidNameException,
			CertificateEncodingException {
		CertRefEnum certRef = CertRefEnum.C_QES;

		X509Certificate x509Certificate = getCertificateFor(hbaHandle, certificateService, context, certRef);

		X500Name x500name = new JcaX509CertificateHolder(x509Certificate).getSubject();

		String lanr = "";
		String namePrefix = "";
		String firstName = getRdnValue(x500name, BCStyle.GIVENNAME);
		String lastName = getRdnValue(x500name, BCStyle.SURNAME);

		Practitioner practitioner = new Practitioner();

		practitioner.setId(UUID.randomUUID().toString()).getMeta()
				.addProfile("https://fhir.kbv.de/StructureDefinition/KBV_PR_FOR_Practitioner|1.1.0");

		Identifier identifier = practitioner.addIdentifier();
		CodeableConcept identifierCodeableConcept = identifier.getType();
		identifierCodeableConcept.addCoding().setSystem("http://terminology.hl7.org/CodeSystem/v2-0203")
				.setCode("LANR");

		identifier.setSystem("https://fhir.kbv.de/NamingSystem/KBV_NS_Base_ANR");
		identifier.setValue(lanr);

		List<StringType> prefixList = new ArrayList<StringType>();
		if (namePrefix != null && !"".equals(namePrefix)) {
			StringType prefix = new StringType(namePrefix);
			Extension extension = new Extension("http://hl7.org/fhir/StructureDefinition/iso21090-EN-qualifier",
					new CodeType("AC"));
			prefix.addExtension(extension);
			prefixList.add(prefix);
		}

		HumanName humanName = practitioner.addName();

		humanName.setUse(NameUse.OFFICIAL).setPrefix(prefixList).addGiven(firstName);

		StringType familyElement = humanName.getFamilyElement();
		List<String> nameParts = new ArrayList<>();

		Extension extension = new Extension("http://hl7.org/fhir/StructureDefinition/humanname-own-name",
				new StringType(lastName));
		familyElement.addExtension(extension);
		nameParts.add(lastName);
		familyElement.setValue(nameParts.stream().collect(Collectors.joining(" ")));

		PractitionerQualificationComponent qualification = new PractitionerQualificationComponent();
		CodeableConcept qualificationCodeableConcept = new CodeableConcept();
		Coding practitionerQualificationCoding = new Coding(
				"https://fhir.kbv.de/CodeSystem/KBV_CS_FOR_Qualification_Type", "00", null);
		qualificationCodeableConcept.addCoding(practitionerQualificationCoding);

		qualification.setCode(qualificationCodeableConcept);
		practitioner.addQualification(qualification);
		qualification = practitioner.addQualification();
        CodeableConcept code = qualification.getCode();
        code.setText("Arzt");
        practitionerQualificationCoding = new Coding("https://fhir.kbv.de/CodeSystem/KBV_CS_FOR_Berufsbezeichnung", "Berufsbezeichnung", null);
        code.getCoding().add(practitionerQualificationCoding);

		return practitioner;
	}

	private String getRdnValue(X500Name x500name, ASN1ObjectIdentifier rdnType) {
		return IETFUtils.valueToString(Stream.of(x500name.getRDNs(rdnType)[0].getTypesAndValues())
				.filter(tv -> tv.getType() == rdnType).findFirst().get().getValue());
	}

	private X509Certificate getCertificateFor(String hbaHandle, CertificateServicePortType certificateService,
			ContextType context, CertRefEnum certRef)
			throws de.gematik.ws.conn.certificateservice.wsdl.v6.FaultMessage, CryptoException {
		ReadCardCertificate.CertRefList certRefList = new ReadCardCertificate.CertRefList();
		certRefList.getCertRef().add(certRef);

		Holder<Status> statusHolder = new Holder<>();
		Holder<X509DataInfoListType> certHolder = new Holder<>();
		if(context.getUserId() == null || context.getUserId().isEmpty()) {
			context.setUserId(UUID.randomUUID().toString());
		}
		try {
			certificateService.readCardCertificate(hbaHandle, context, certRefList, CryptType.ECC, statusHolder, certHolder);
		} catch(Exception e) {
			log.log(Level.WARNING, "Could not read ECC certificate, trying RSA", e);
			certificateService.readCardCertificate(hbaHandle, context, certRefList, CryptType.RSA, statusHolder, certHolder);
		}

		return CryptoLoader.getCertificateFromAsn1DERCertBytes(
				certHolder.value.getX509DataInfo().get(0).getX509Data().getX509Certificate());
	}

	static String getFirstCardOfType(EventServicePortType eventService, CardTypeType type, ContextType context)
			throws FaultMessage {
		GetCards parameter = new GetCards();
		parameter.setContext(context);
		parameter.setCardType(type);
		GetCardsResponse getCardsResponse = eventService.getCards(parameter);

		List<CardInfoType> cards = getCardsResponse.getCards().getCard();
		if (cards.size() > 0) {
			String ehcHandle = cards.get(0).getCardHandle();
			return ehcHandle;
		} else {
			return null;
		}
	}

	private Medication createMedicationResource() {
		Medication medication = new Medication();

		medication.setId(UUID.randomUUID().toString()).getMeta()
				.addProfile("https://fhir.kbv.de/StructureDefinition/KBV_PR_ERP_Medication_PZN|1.1.0");

		Coding medicationType = new Coding("http://snomed.info/sct", "763158003", "Medicinal product (product)");
		medicationType.setVersion("http://snomed.info/sct/900000000000207008/version/20220331");
		CodeableConcept codeableConcept = new CodeableConcept(medicationType);
		Extension medicationTypeEx = new Extension("https://fhir.kbv.de/StructureDefinition/KBV_EX_Base_Medication_Type", codeableConcept);
		medication.addExtension(medicationTypeEx);

		Coding medicationCategory = new Coding("https://fhir.kbv.de/CodeSystem/KBV_CS_ERP_Medication_Category", "00",
				null);
		Extension medicationCategoryEx = new Extension(
				"https://fhir.kbv.de/StructureDefinition/KBV_EX_ERP_Medication_Category", medicationCategory);
		medication.addExtension(medicationCategoryEx);

		Extension medicationVaccine = new Extension(
				"https://fhir.kbv.de/StructureDefinition/KBV_EX_ERP_Medication_Vaccine", new BooleanType(false));
		medication.addExtension(medicationVaccine);

		String normgroesseString = "N1";

		Extension normgroesse = new Extension("http://fhir.de/StructureDefinition/normgroesse",
				new CodeType(normgroesseString));
		medication.addExtension(normgroesse);

		medication.getCode().addCoding().setSystem("http://fhir.de/CodeSystem/ifa/pzn").setCode("");
		medication.getCode().setText("");
		String darreichungsform = "TAB";
		Coding formCoding = new Coding("https://fhir.kbv.de/CodeSystem/KBV_CS_SFHIR_KBV_DARREICHUNGSFORM",
				darreichungsform, "");
		medication.setForm(new CodeableConcept().addCoding(formCoding));

		return medication;
	}

	private MedicationRequest createMedicationRequest(String medicationId, String patientId, String practitionerId,
			String coverageId) {
		MedicationRequest medicationRequest = new MedicationRequest();

		medicationRequest.setAuthoredOn(new Date());
		medicationRequest.getAuthoredOnElement().setPrecision(TemporalPrecisionEnum.DAY);

		medicationRequest.setId(UUID.randomUUID().toString());

		medicationRequest.getMeta().addProfile("https://fhir.kbv.de/StructureDefinition/KBV_PR_ERP_Prescription|1.1.0");

		Coding valueCoding = new Coding("https://fhir.kbv.de/CodeSystem/KBV_CS_FOR_StatusCoPayment", "0", null);
		Extension coPayment = new Extension("https://fhir.kbv.de/StructureDefinition/KBV_EX_FOR_StatusCoPayment",
				valueCoding);
		medicationRequest.addExtension(coPayment);

		// emergencyServiceFeeParam default false
		Extension emergencyServicesFee = new Extension(
				"https://fhir.kbv.de/StructureDefinition/KBV_EX_ERP_EmergencyServicesFee", new BooleanType(false));
		medicationRequest.addExtension(emergencyServicesFee);

		Extension bvg = new Extension("https://fhir.kbv.de/StructureDefinition/KBV_EX_ERP_BVG", new BooleanType(false));
		medicationRequest.addExtension(bvg);

		// <extension
		// url="https://fhir.kbv.de/StructureDefinition/KBV_EX_ERP_Multiple_Prescription">
		Extension multiplePrescription = new Extension(
				"https://fhir.kbv.de/StructureDefinition/KBV_EX_ERP_Multiple_Prescription");
		// <extension url="Kennzeichen">
		// <valueBoolean value="true" />
		// </extension>
		multiplePrescription.addExtension(new Extension("Kennzeichen", new BooleanType(false)));
		medicationRequest.addExtension(multiplePrescription);

		medicationRequest.setStatus(MedicationRequest.MedicationRequestStatus.ACTIVE)
				.setIntent(MedicationRequest.MedicationRequestIntent.ORDER).getMedicationReference()
				.setReference("Medication/" + medicationId);

		medicationRequest.getSubject().setReference("Patient/" + patientId);

		medicationRequest.getRequester().setReference("Practitioner/" + practitionerId);

		medicationRequest.addInsurance().setReference("Coverage/" + coverageId);

		medicationRequest.setNote(Arrays.asList(new Annotation().setText("")));

		medicationRequest.addDosageInstruction().setText("").addExtension()
				.setUrl("https://fhir.kbv.de/StructureDefinition/KBV_EX_ERP_DosageFlag")
				.setValue(new BooleanType(true));

		MedicationRequest.MedicationRequestDispenseRequestComponent dispenseRequest = new MedicationRequestDispenseRequestComponent();
		Quantity quantity = new Quantity();
		quantity.setValue(1);
		quantity.setSystem("http://unitsofmeasure.org");
		quantity.setCode("{Package}");
		dispenseRequest.setQuantity(quantity);
		medicationRequest.setDispenseRequest(dispenseRequest);
		MedicationRequestSubstitutionComponent substitution = new MedicationRequestSubstitutionComponent();
		substitution.setAllowed(new BooleanType(true));
		medicationRequest.setSubstitution(substitution);

		return medicationRequest;
	}

	public void onPrefillBundleEvent(@ObservesAsync PrefillBundleEvent prefillBundleEvent) {
		try {
			bundleEvent.fireAsync(new BundlesEvent(Arrays.asList(get(prefillBundleEvent.getRuntimeConfig(), prefillBundleEvent.getEgkHandle(), prefillBundleEvent.getSmcbHandle(), prefillBundleEvent.getHbaHandle()))));
		} catch (Exception e) {
			log.log(Level.SEVERE, "Could not create bundles", e);
			exceptionEvent.fireAsync(new ExceptionWithReplyToException(e, prefillBundleEvent.getReplyTo(),
					prefillBundleEvent.getId()));
		}
	}

	public Bundle getTestPrescriptionBundle(RuntimeConfig runtimeConfig) throws FaultMessage, de.gematik.ws.conn.certificateservice.wsdl.v6.FaultMessage, InvalidNameException, CertificateEncodingException, CryptoException, ParseException {

		ContextType context = connectorServicesProvider.getContextType(runtimeConfig);
		EventServicePortType eventService = connectorServicesProvider.getEventServicePortType(runtimeConfig);

		String smcbHandle = (runtimeConfig.getSMCBHandle() != null) ? runtimeConfig.getSMCBHandle() : getFirstCardOfType(eventService, CardTypeType.SMC_B, context);
		String hbaHandle = (runtimeConfig.getEHBAHandle() != null) ? runtimeConfig.getEHBAHandle() : getFirstCardOfType(eventService, CardTypeType.HBA, context);

		CertificateServicePortType certificateService = connectorServicesProvider.getCertificateServicePortType(runtimeConfig);

		Practitioner practitioner = null;
		if (hbaHandle != null) {
			practitioner = hbaHandle2Practitioner(hbaHandle, runtimeConfig, certificateService, context);
		} else {
			throw new RuntimeException("No HBA found");
		}
		practitioner.getIdentifier().get(0).setValue("999999999");

		Organization organization = null;
		if (smcbHandle != null) {
			organization = smcbHandle2Organization(smcbHandle, runtimeConfig, certificateService, context);
		} else {
			throw new RuntimeException("No SMC-B found");
		}
		organization.getIdentifier().get(0).setValue("999999999");
		organization.getTelecom().get(0).setValue("0123456789");

		Patient patient = new Patient();
		patient.setId(UUID.randomUUID().toString())
				.getMeta()
				.addProfile("https://fhir.kbv.de/StructureDefinition/KBV_PR_FOR_Patient|1.1.0");
		Identifier identifier = patient.addIdentifier();
				CodeableConcept identifierCodeableConcept = identifier.getType();
		identifierCodeableConcept.addCoding()
				.setSystem("http://fhir.de/CodeSystem/identifier-type-de-basis")
				.setCode("GKV");
		identifier.setSystem("http://fhir.de/sid/gkv/kvid-10")
				.setValue("T555558879");
		patient.addName()
				.setUse(NameUse.OFFICIAL)
				.addGiven("Max")
				.setFamily("TK-Mustermann");
		patient.getName().get(0).getFamilyElement().addExtension("http://hl7.org/fhir/StructureDefinition/humanname-own-name", new StringType("TK-Mustermann"));
		Date birthdate = new SimpleDateFormat("yyyyMMdd").parse("19950101");
		patient.setBirthDate(birthdate);
		patient.addAddress()
				.setType(AddressType.BOTH)
				.setCountry("D")
				.setCity("Hamburg")
				.setPostalCode("22305")
				.addLine("Bramfelder Str. 140");
		patient.getAddress().get(0).getLine().get(0).addExtension("http://hl7.org/fhir/StructureDefinition/iso21090-ADXP-houseNumber", new StringType("140"));
		patient.getAddress().get(0).getLine().get(0).addExtension("http://hl7.org/fhir/StructureDefinition/iso21090-ADXP-streetName", new StringType("Bramfelder Str."));

		Coverage coverage = new Coverage();
		coverage.setId(UUID.randomUUID().toString())
				.getMeta()
				.addProfile("https://fhir.kbv.de/StructureDefinition/KBV_PR_FOR_Coverage|1.1.0");
		Coding besonderePersonengruppe = new Coding("https://fhir.kbv.de/CodeSystem/KBV_CS_SFHIR_KBV_PERSONENGRUPPE", "00", null);
		Extension besonderePersonengruppeEx = new Extension("http://fhir.de/StructureDefinition/gkv/besondere-personengruppe", besonderePersonengruppe);
		coverage.addExtension(besonderePersonengruppeEx);
		Coding dmp = new Coding("https://fhir.kbv.de/CodeSystem/KBV_CS_SFHIR_KBV_DMP", "00", null);
		Extension dmpEx = new Extension("http://fhir.de/StructureDefinition/gkv/dmp-kennzeichen", dmp);
		coverage.addExtension(dmpEx);
		Coding versichertenart = new Coding("https://fhir.kbv.de/CodeSystem/KBV_CS_SFHIR_KBV_VERSICHERTENSTATUS", "1", null);
		Extension versichertenartEx = new Extension("http://fhir.de/StructureDefinition/gkv/versichertenart", versichertenart);
		coverage.addExtension(versichertenartEx);
		coverage.setStatus(Coverage.CoverageStatus.fromCode("active"));
		coverage.setType(new CodeableConcept().addCoding(new Coding("http://fhir.de/CodeSystem/versicherungsart-de-basis", "GKV", "")));
		coverage.getBeneficiary().setReference("Patient/" + patient.getId());
		coverage.addPayor()
				.setDisplay("TK")
				.getIdentifier()
				.setSystem("http://fhir.de/sid/arge-ik/iknr")
				.setValue("101575519");

		Medication medication = new Medication();
		medication.setId(UUID.randomUUID().toString())
				.getMeta()
				.addProfile("https://fhir.kbv.de/StructureDefinition/KBV_PR_ERP_Medication_FreeText|1.1.0");
		Coding medicationCategory = new Coding("https://fhir.kbv.de/CodeSystem/KBV_CS_ERP_Medication_Category", "00",null);
		Extension medicationCategoryEx = new Extension("https://fhir.kbv.de/StructureDefinition/KBV_EX_ERP_Medication_Category", medicationCategory);
		medication.addExtension(medicationCategoryEx);
		medication.addExtension(new Extension("https://fhir.kbv.de/StructureDefinition/KBV_EX_ERP_Medication_Vaccine", new BooleanType(false)));
		medication.getCode()
				.setText("Test")
				.addCoding()
				.setSystem("https://fhir.kbv.de/CodeSystem/KBV_CS_ERP_Medication_Type").setCode("freitext");

		MedicationRequest medicationRequest = createMedicationRequest(
				medication.getIdElement().getIdPart(),
				patient.getIdElement().getIdPart(),
				practitioner.getIdElement().getIdPart(),
				coverage.getIdElement().getIdPart()
		);
		medicationRequest.getDosageInstruction().get(0).setText("Zu Testzwecken");

        return KBVFHIRUtil.assembleBundle(practitioner, organization, patient, coverage, medication,
				medicationRequest, null, null);
	}
}