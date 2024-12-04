package health.ere.ps.service.kbv;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Date;
import java.util.UUID;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import org.hl7.fhir.r4.model.*;
import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.model.api.TemporalPrecisionEnum;
import ca.uhn.fhir.parser.IParser;


public class KBVFHIRUtilTest {
    IParser parser = FhirContext.forR4().newXmlParser();
    IParser jsonParser = FhirContext.forR4().newJsonParser();

    @ParameterizedTest
    @CsvSource({
            "Beirliner_Maria.xml, Praxis_Sigmuntowski.xml, TK-Mustermann_Max.xml, TK-Testprescription.xml, Freetext/_Test.xml, MedicationRequest_GebPflicht.xml",
/*
            "Beirliner_Maria.xml, Praxis_Sigmuntowski.xml, Joe_Doe.xml, TestGKVSV-Joe_Doe.xml, PZN/Ibuflam600N3.xml, Dosierung_Gekreuzt.xml",

            "Beirliner_Maria.xml, Praxis_Sigmuntowski.xml, Joe_Doe.xml, TestGKVSV-Joe_Doe.xml, PZN/LThyroxin75N3.xml, MedicationRequest_GebPflicht.xml",
            "Beirliner_Maria.xml, Praxis_Sigmuntowski.xml, Grundner_Finn_SECRET.xml, TestGKVSV-Grundner_Finn_SECRET.xml, PZN/Ibuflam600N3.xml, Dosierung_Gekreuzt.xml",
            "Beirliner_Maria.xml, Praxis_Sigmuntowski.xml, Collins_Klaus_SECRET.xml, TestGKVSV-Collins_Klaus_SECRET.xml, PZN/LThyroxin75N3.xml, MedicationRequest_GebPflicht.xml",

            "Beirliner_Maria.xml, Praxis_Sigmuntowski.xml, Mondwürfel_Mia_SECRET.xml, TestGKVSV-Mondwürfel_Mia_SECRET.xml, PZN/Metoprolol95N3.xml, MedicationRequest_GebPflicht.xml",

            "Beirliner_Maria.xml, Praxis_Sigmuntowski.xml, Müller-Lügenscheidt_Tina_SECRET.xml, TestGKVSV-Müller-Lügenscheidt_Tina_SECRET.xml, PZN/NovaminsulfonLichN3.xml, MedicationRequest_GebPflicht.xml",
            "Beirliner_Maria.xml, Praxis_Sigmuntowski.xml, Kraus_Bertha_SECRET.xml, TestGKVSV-Kraus_Bertha_SECRET.xml, PZN/Ramilich10N3.xml, MedicationRequest_GebPflicht.xml",
            "Beirliner_Maria.xml, Praxis_Sigmuntowski.xml, Baggins_Bilbo_SECRET.xml, TestGKVSV-Baggins_Bilbo_SECRET.xml, PZN/TorasemidALN3.xml, MedicationRequest_GebPflicht.xml",

            "Beirliner_Maria.xml, Praxis_Sigmuntowski.xml, Otis_Thorsten.xml, TestGKVSV-Otis_Thorsten.xml, PZN/Amvuttra.xml, MedicationRequest_GebPflicht.xml",
            "Beirliner_Maria.xml, Praxis_Sigmuntowski.xml, Doria_Finn_Sigurd.xml, TestGKVSV-Doria_Finn_Sigurd.xml, PZN/Impfstoff_Beriglobin.xml, Z_ä_A_MedicationRequest.xml",
            "Beirliner_Maria.xml, Praxis_Sigmuntowski.xml, Doria_Finn_Sigurd.xml, TestGKVSV-Doria_Finn_Sigurd.xml, Freetext/Impfstoff_Beriglobin.xml, Z_ä_A_MedicationRequest.xml",

            "Beirliner_Maria.xml, Praxis_Sigmuntowski.xml, Doria_Finn_Sigurd.xml, TestGKVSV-Doria_Finn_Sigurd.xml, Ingredient/Metronidazol_400_N3.xml, Ingredient_GebPflicht.xml",
            "Beirliner_Maria.xml, Praxis_Sigmuntowski.xml, Collins_Klaus_SECRET.xml, TestGKVSV-Collins_Klaus_SECRET.xml, Ingredient/PrednicarbatFettsalbe.xml, Ingredient_GebPflicht.xml",
            "Beirliner_Maria.xml, Praxis_Sigmuntowski.xml, Kätner_Paula_SECRET.xml, TestGKVSV-Kätner_Paula_SECRET.xml, Ingredient/Metamizol500N1.xml, Ingredient_GebPflicht.xml",
            "Beirliner_Maria.xml, Praxis_Sigmuntowski.xml, Kraus_Bertha_SECRET.xml, TestGKVSV-Kraus_Bertha_SECRET.xml, Ingredient/Ibuprofen600N1.xml, Ingredient_GebPflicht.xml",

            "Beirliner_Maria.xml, Praxis_Sigmuntowski.xml, Beutelsbacher-Tütenheim_Anastasia_SECRET.xml, TestGKVSV-Beutelsbacher-Tütenheim_Anastasia_SECRET.xml, Ingredient/Ramipril5N3.xml, Ingredient_GebPflicht.xml",

            "Beirliner_Maria.xml, Praxis_Sigmuntowski.xml, Mondwürfel_Mia_SECRET.xml, TestGKVSV-Mondwürfel_Mia_SECRET.xml, Ingredient/Paracetamol40mgmlN1.xml, Ingredient_GebFrei.xml",

            "Beirliner_Maria.xml, Praxis_Sigmuntowski.xml, Doria_Finn_Sigurd.xml, TestGKVSV-Doria_Finn_Sigurd.xml, Freetext/Tavor_1_50Tab.xml, MedicationRequest_GebPflicht.xml",
            "Beirliner_Maria.xml, Praxis_Sigmuntowski.xml, Bräuer_Dominik-Peter_SECRET.xml, TestGKVSV-Bräuer_Dominik-Peter_SECRET.xml, Freetext/Tavor_1_50Tab.xml, Dosierung_Gekreuzt.xml",

            "Beirliner_Maria.xml, Praxis_Sigmuntowski.xml, Popowitsch_Martina_SECRET.xml, TestGKVSV-Popowitsch_Martina_SECRET.xml, Freetext/Fastjekt.xml, MedicationRequest_GebPflicht.xml",
            "Beirliner_Maria.xml, Praxis_Sigmuntowski.xml, Popowitsch_Martina_SECRET.xml, TestGKVSV-Popowitsch_Martina_SECRET.xml, Freetext/Ibuprofen600N1.xml, MedicationRequest_GebPflicht.xml",
            "Beirliner_Maria.xml, Praxis_Sigmuntowski.xml, Popowitsch_Martina_SECRET.xml, TestGKVSV-Popowitsch_Martina_SECRET.xml, Freetext/TilidinComp50_4_N1.xml, MedicationRequest_GebPflicht.xml",

            "Beirliner_Maria.xml, Praxis_Sigmuntowski.xml, Dammer-Orsted_Tobias_SECRET.xml, TestGKVSV-Dammer-Orsted_Tobias_SECRET.xml, Freetext/Ramipril_5_N3_Tab.xml, MedicationRequest_GebPflicht.xml",

            "Beirliner_Maria.xml, Praxis_Sigmuntowski.xml, Cördes_Kevin_SECRET.xml, TestGKVSV-Cördes_Kevin_SECRET.xml, Freetext/Rezeptur_Freitext_Creme.xml, MedicationRequest_GebPflicht.xml",

            "Beirliner_Maria.xml, Praxis_Sigmuntowski.xml, Broder_Julian_SECRET.xml, TestGKVSV-Broder_Julian_SECRET.xml, Freetext/Rezeptur_Freitext_Tube.xml, MedicationRequest_GebPflicht.xml",

            "Beirliner_Maria.xml, Praxis_Sigmuntowski.xml, Doria_Finn_Sigurd.xml, TestGKVSV-Doria_Finn_Sigurd.xml, Compounding/Triam01Clotri2BasiscremeAd100.xml, Rezeptur_GebPflicht.xml",

            "Beirliner_Maria.xml, Praxis_Sigmuntowski.xml, Klein_Teddy.xml, AOKBY-Klein_Teddy.xml, Compounding/Triam01Clotri2BasiscremeAd100.xml, Rezeptur_GebFrei.xml",

            "Beirliner_Maria.xml, Praxis_Sigmuntowski.xml, Broder_Julian_SECRET.xml, TestGKVSV-Broder_Julian_SECRET.xml, Compounding/Triam01Clotri2BasiscremeAd100.xml, Rezeptur_27aSGBV.xml",
            "Beirliner_Maria.xml, Praxis_Sigmuntowski.xml, Broder_Julian_SECRET.xml, TestGKVSV-Broder_Julian_SECRET.xml, PZN/Orgalutran.xml, 27aSGBV.xml",

            "Beirliner_Maria.xml, Praxis_Sigmuntowski.xml, Dammer-Orsted_Tobias_SECRET.xml, TestGKVSV-Dammer-Orsted_Tobias_SECRET.xml, Ingredient/Paracetamol40mgmlN1.xml, Ingredient_GebPflicht.xml",

            "Beirliner_Maria.xml, Praxis_Sigmuntowski.xml, Königsstein_Ludger.xml, AOKNO-Königsstein_Ludger.xml, Ingredient/Paracetamol40mgmlN1.xml, Ingredient_GebPflicht.xml",
            "Beirliner_Maria.xml, Praxis_Sigmuntowski.xml, Pan_Peter.xml, TK-Pan_Peter.xml, Ingredient/Paracetamol40mgmlN1.xml, Ingredient_GebPflicht.xml",

            "Beirliner_Maria.xml, Praxis_Sigmuntowski.xml, Broder_Julian_SECRET.xml, TestGKVSV-Broder_Julian_SECRET.xml, Ingredient/Paracetamol40mgmlN1.xml, Ingredient_GebPflicht.xml",

            "Beirliner_Maria.xml, Praxis_Sigmuntowski.xml, Broder_Julian_SECRET.xml, TestGKVSV-Broder_Julian_SECRET.xml, PZN/LactuloseAL1000N3.xml, MedicationRequest_GebPflicht.xml",

            "Beirliner_Maria.xml, Praxis_Sigmuntowski.xml, Königsstein_Ludger.xml, AOKNO-Königsstein_Ludger.xml, PZN/LactuloseAL1000N3.xml, MedicationRequest_GebPflicht.xml",
            "Beirliner_Maria.xml, Praxis_Sigmuntowski.xml, Pan_Peter.xml, TK-Pan_Peter.xml, PZN/LactuloseAL1000N3.xml, MedicationRequest_GebPflicht.xml",
            "Beirliner_Maria.xml, Praxis_Sigmuntowski.xml, Grossherzog_Friedrich.xml, mhplusBKK-Grossherzog_Friedrich.xml, PZN/LactuloseAL1000N3.xml, MedicationRequest_GebPflicht.xml",

            "Beirliner_Maria.xml, Praxis_Sigmuntowski.xml, PKV-Privatus_Paulus.xml, AllianzPKV-Privatus_Paulus.xml, PZN/LactuloseAL1000N3.xml, PKV-MedicationRequest.xml",
            "Beirliner_Maria.xml, Praxis_Sigmuntowski.xml, PKV-Privatus_Paulus.xml, AllianzPKV-Privatus_Paulus.xml, PZN/Amvuttra.xml, PKV-MedicationRequest.xml",
            "Beirliner_Maria.xml, Praxis_Sigmuntowski.xml, PKV-Privatus_Paulus.xml, AllianzPKV-Privatus_Paulus.xml, Ingredient/Metronidazol_400_N3.xml, PKV-Ingredient.xml",
            "Beirliner_Maria.xml, Praxis_Sigmuntowski.xml, PKV-Privati_Paula.xml, AllianzPKV-Privati_Paula.xml, Freetext/Impfstoff_Beriglobin.xml, PKV-MedicationRequest.xml",
            "Beirliner_Maria.xml, Praxis_Sigmuntowski.xml, PKV-Privati_Paula.xml, AllianzPKV-Privati_Paula.xml, Compounding/Triam01Clotri2BasiscremeAd100.xml, PKV-Rezeptur.xml",
            "Beirliner_Maria.xml, Praxis_Sigmuntowski.xml, PKV-Privati_Teddy.xml, AllianzPKV-Privati_Teddy.xml, Ingredient/Paracetamol40mgmlN1.xml, PKV-Ingredient.xml",
            "Beirliner_Maria.xml, Praxis_Sigmuntowski.xml, PKV-Privati_Teddy.xml, AllianzPKV-Privati_Teddy.xml, Ingredient/Paracetamol40mgmlN1.xml, PKV-Ingredient.xml",
            "Beirliner_Maria.xml, Praxis_Sigmuntowski.xml, PKV-Privati_Teddy.xml, AllianzPKV-Privati_Teddy.xml, Ingredient/Paracetamol40mgmlN1.xml, PKV-Ingredient.xml",
            "Beirliner_Maria.xml, Praxis_Sigmuntowski.xml, PKV-Privati_Teddy.xml, AllianzPKV-Privati_Teddy.xml, Compounding/Triam01Clotri2BasiscremeAd100.xml, PKV-Rezeptur.xml",

            "Beirliner_Maria.xml, Praxis_Sigmuntowski.xml, Pan_Peter.xml, Selbstzahler-Pan_Peter.xml, PZN/LactuloseAL1000N3.xml, MedicationRequest_GebPflicht.xml",
            "Beirliner_Maria.xml, Praxis_Sigmuntowski.xml, Pan_Peter.xml, Selbstzahler-Pan_Peter.xml, PZN/Amvuttra.xml, MedicationRequest_GebPflicht.xml",
            "Beirliner_Maria.xml, Praxis_Sigmuntowski.xml, Pan_Peter.xml, Selbstzahler-Pan_Peter.xml, Ingredient/Ramipril5N3.xml, Ingredient_GebPflicht.xml",
            "Beirliner_Maria.xml, Praxis_Sigmuntowski.xml, Pan_Peter.xml, Selbstzahler-Pan_Peter.xml, Freetext/Tavor_1_50Tab.xml, MedicationRequest_GebPflicht.xml",

            "FehlenderArztstempel.xml, Praxis_Sigmuntowski.xml, vonSchaumberg_Karl.xml, AOKRLHH-vonSchaumberg_Karl.xml, PZN/Amvuttra.xml, MedicationRequest_GebPflicht.xml",
            "Beirliner_Maria.xml, Praxis_Sigmuntowski.xml, vonSchaumberg_Karl.xml, AOKRLHH-vonSchaumberg_Karl.xml, Ingredient/Metamizol500N1.xml, ExtremLangeDosierung.xml",
            "Beirliner_Maria.xml, Praxis_Sigmuntowski.xml, vonSchaumberg_Karl.xml, Versicherung_fehlt1.xml, Ingredient/Metamizol500N1.xml, Ingredient_GebPflicht.xml",
            "Beirliner_Maria.xml, Praxis_Sigmuntowski.xml, vonSchaumberg_Karl.xml, Versicherung_fehlt2.xml, Ingredient/Metamizol500N1.xml, Ingredient_GebPflicht.xml",
            "Beirliner_Maria.xml, Praxis_Sigmuntowski.xml, vonSchaumberg_Karl.xml, AOKRLHH-vonSchaumberg_Karl.xml, PZN/ProstaguttUno_NichtErstatt.xml, MedicationRequest_GebPflicht.xml",

            "Beirliner_Maria.xml, Praxis_Sigmuntowski.xml, Birke_Bruno.xml, VerwaltungsBG-Birke_Bruno.xml, Ingredient/PrednicarbatFettsalbe.xml, Ingredient_Arbeitsunfall.xml",
            "Beirliner_Maria.xml, Praxis_Sigmuntowski.xml, Birke_Bruno.xml, VerwaltungsBG-Birke_Bruno.xml, Ingredient/PrednicarbatFettsalbe.xml, Ingredient_Berufskrankheit.xml",
            "Beirliner_Maria.xml, Praxis_Sigmuntowski.xml, Birke_Bruno.xml, KN-Birke_Bruno.xml, Ingredient/PrednicarbatFettsalbe.xml, Ingredient_Unfall.xml",


            //Entlassmanagement needs another BSNR and legal basis - not "just send"
            "Beirliner_Maria.xml, Vincenzkrankenhaus.xml, Erbprinzessin_Ingrid.xml, AOKBY-Erbprinzessin_Ingrid.xml, PZN/Clexane40N1.xml, MedicationRequest_GebFrei.xml",
            "Beirliner_Maria.xml, Vincenzkrankenhaus.xml, Erbprinzessin_Ingrid.xml, AOKBY-Erbprinzessin_Ingrid.xml, Freetext/Ibuprofen600N1.xml, MedicationRequest_GebFrei.xml",
            "Beirliner_Maria.xml, Vincenzkrankenhaus.xml, Erbprinzessin_Ingrid.xml, AOKBY-Erbprinzessin_Ingrid.xml, Freetext/TilidinComp50_4_N1.xml, MedicationRequest_GebFrei.xml",
            "Beirliner_Maria.xml, Vincenzkrankenhaus.xml, Erbprinzessin_Ingrid.xml, AOKBY-Erbprinzessin_Ingrid.xml, Ingredient/Metamizol500N1.xml, Ingredient_GebFrei.xml",
            "Beirliner_Maria.xml, Vincenzkrankenhaus.xml, Erbprinzessin_Ingrid.xml, AOKBY-Erbprinzessin_Ingrid.xml, Ingredient/Ibuprofen600N1.xml, Ingredient_GebFrei.xml",


             // MFVO ... sharing the same ID! Not "just send"
            "Beirliner_Maria.xml, Praxis_Sigmuntowski.xml, vonSchaumberg_Karl.xml, AOKRLHH-vonSchaumberg_Karl.xml, PZN/Metoprolol95N3.xml, MFVO1_3_GebPflicht.xml",
            "Beirliner_Maria.xml, Praxis_Sigmuntowski.xml, vonSchaumberg_Karl.xml, AOKRLHH-vonSchaumberg_Karl.xml, PZN/Metoprolol95N3.xml, MFVO2_3_GebPflicht.xml",
            "Beirliner_Maria.xml, Praxis_Sigmuntowski.xml, vonSchaumberg_Karl.xml, AOKRLHH-vonSchaumberg_Karl.xml, PZN/Metoprolol95N3.xml, MFVO3_3_GebPflicht.xml",

            "Beirliner_Maria.xml, Praxis_Sigmuntowski.xml, vonSchaumberg_Karl.xml, AOKRLHH-vonSchaumberg_Karl.xml, PZN/TorasemidALN3.xml, MFVO1_3_GebPflicht.xml",
            "Beirliner_Maria.xml, Praxis_Sigmuntowski.xml, vonSchaumberg_Karl.xml, AOKRLHH-vonSchaumberg_Karl.xml, PZN/TorasemidALN3.xml, MFVO2_3_GebPflicht.xml",
            "Beirliner_Maria.xml, Praxis_Sigmuntowski.xml, vonSchaumberg_Karl.xml, AOKRLHH-vonSchaumberg_Karl.xml, PZN/TorasemidALN3.xml, MFVO3_3_GebPflicht.xml",


            "Beirliner_Maria.xml, Praxis_Sigmuntowski.xml, vonSchaumberg_Karl.xml, AOKRLHH-vonSchaumberg_Karl.xml, PZN/Ramilich10N3.xml, MFVO1_3_GebPflicht.xml",
            "Beirliner_Maria.xml, Praxis_Sigmuntowski.xml, vonSchaumberg_Karl.xml, AOKRLHH-vonSchaumberg_Karl.xml, PZN/Ramilich10N3.xml, MFVO2_3_GebPflicht.xml",
            "Beirliner_Maria.xml, Praxis_Sigmuntowski.xml, vonSchaumberg_Karl.xml, AOKRLHH-vonSchaumberg_Karl.xml, PZN/Ramilich10N3.xml, MFVO3_3_GebPflicht.xml",
            */
    })
    public void testAssembleBundle(String practitionerFilename, String organizationFilename, String patientFilename, String coverageFilename, String medicationSubfolderFilename, String medicationRequestFilename) throws IOException {
        Practitioner practitioner = parser.parseResource(Practitioner.class, Files.readString(Paths.get("./src/test/resources/secret/bundle-entries/Practitioner/" + practitionerFilename)));
        Organization organization = parser.parseResource(Organization.class, Files.readString(Paths.get("./src/test/resources/secret/bundle-entries/Organization/" + organizationFilename)));
        PractitionerRole practitionerRole = null;
        Practitioner attester = null;

        Patient patient = parser.parseResource(Patient.class, Files.readString(Paths.get("./src/test/resources/secret/bundle-entries/Patient/" + patientFilename)));
        Coverage coverage = parser.parseResource(Coverage.class, Files.readString(Paths.get("./src/test/resources/secret/bundle-entries/Coverage/" + coverageFilename)));

        Medication medication = parser.parseResource(Medication.class, Files.readString(Paths.get("./src/test/resources/secret/bundle-entries/Medication/" + medicationSubfolderFilename)));

        MedicationRequest medicationRequest = parser.parseResource(MedicationRequest.class, Files.readString(Paths.get("./src/test/resources/secret/bundle-entries/MedicationRequest/" + medicationRequestFilename)));
        medicationRequest.setId(UUID.randomUUID().toString());

        medicationRequest.setAuthoredOn(new Date());
        medicationRequest.getAuthoredOnElement().setPrecision(TemporalPrecisionEnum.DAY);

        medicationRequest.getRequester().setReference(practitioner.getId());
        medicationRequest.getSubject().setReference(patient.getId());
        medicationRequest.getInsurance().get(0).setReference(coverage.getId());
        medicationRequest.getMedicationReference().setReference(medication.getId());

        Bundle bundle = KBVFHIRUtil.assembleBundle(practitioner, organization, patient, coverage, medication, medicationRequest, null, null);
        //System.out.println(parser.encodeResourceToString(bundle));

        /*
        // 04 = Entlassmanagement Krankenhaus
        Coding valueCoding = new Coding("https://fhir.kbv.de/CodeSystem/KBV_CS_SFHIR_KBV_STATUSKENNZEICHEN", "04", null);
        bundle.getEntry().forEach(entry -> {
            if (entry.getResource() instanceof Composition) {
                ((Composition) entry.getResource()).getExtensionByUrl("https://fhir.kbv.de/StructureDefinition/KBV_EX_FOR_Legal_basis").setValue(valueCoding);
            }
        });
        // Entlassmanagement BSNR
        bundle.getEntry().forEach(entry -> {
            if (entry.getResource() instanceof Organization) {
                ((Organization) entry.getResource()).getIdentifierFirstRep().setValue("771234567");
            }
        });
        */

        //Encode to JSON
        System.out.println(jsonParser.encodeResourceToString(bundle));

        //Encode to XML
        System.out.println(parser.encodeResourceToString(bundle).replaceAll("\"", "'"));
    }
}



