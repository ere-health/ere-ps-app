package health.ere.ps.validation.fhir.context.support;

import org.apache.commons.lang3.StringUtils;
import org.hl7.fhir.instance.model.api.IBase;
import org.hl7.fhir.instance.model.api.IBaseBundle;
import org.hl7.fhir.instance.model.api.IBaseResource;
import org.hl7.fhir.instance.model.api.IPrimitiveType;
import org.jboss.logging.Logger;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import javax.annotation.Nullable;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.context.RuntimeResourceDefinition;
import ca.uhn.fhir.context.support.IValidationSupport;
import ca.uhn.fhir.rest.api.Constants;
import ca.uhn.fhir.util.BundleUtil;

public class KbvValidationSupport implements IValidationSupport {
    private static final String URL_PREFIX_STRUCTURE_DEFINITION = "https://fhir.kbv.de/StructureDefinition/";
    private static final String URL_PREFIX_STRUCTURE_DEFINITION_BASE = "http://hl7.org/fhir/";
    private static final Logger ourLog = Logger.getLogger(KbvValidationSupport.class);
    private final FhirContext myCtx;
    private Map<String, IBaseResource> myCodeSystems;
    private Map<String, IBaseResource> myStructureDefinitions;
    private Map<String, IBaseResource> myValueSets;
    private List<String> myTerminologyResources;
    private List<String> myStructureDefinitionResources;

    public KbvValidationSupport(FhirContext theFhirContext) {
        this.myCtx = theFhirContext;
    }

    private void initializeResourceLists() {
        if (this.myTerminologyResources == null || this.myStructureDefinitionResources == null) {
            ArrayList terminologyResources = new ArrayList();
            ArrayList structureDefinitionResources = new ArrayList();

            switch(this.getFhirContext().getVersion().getVersion()) {
                case R4:
                    terminologyResources.add("/fhir/r4/valueset/v1_0_1/KBV_VS_ERP_Accident_Type.xml");
                    terminologyResources.add("/fhir/r4/valueset/v1_0_1/KBV_VS_ERP_Medication_Category.xml");
                    terminologyResources.add("/fhir/r4/valueset/v1_0_1/KBV_VS_ERP_StatusCoPayment.xml");

                    terminologyResources.add("/fhir/r4/valueset/v1_0_3/KBV_VS_FOR_Payor_type.xml");
                    terminologyResources.add("/fhir/r4/valueset/v1_0_3/KBV_VS_FOR_Qualification_Type.xml");

                    terminologyResources.add("/fhir/r4/codesystem/v1_0_1/KBV_CS_ERP_Medication_Category.xml");
                    terminologyResources.add("/fhir/r4/codesystem/v1_0_1/KBV_CS_ERP_Medication_Type.xml");
                    terminologyResources.add("/fhir/r4/codesystem/v1_0_1/KBV_CS_ERP_Section_Type.xml");
                    terminologyResources.add("/fhir/r4/codesystem/v1_0_1/KBV_CS_ERP_StatusCoPayment.xml");

                    terminologyResources.add("/fhir/r4/codesystem/v1_0_3/KBV_CS_ERP_StatusCoPayment.xml");
                    terminologyResources.add("/fhir/r4/codesystem/v1_0_3/KBV_CS_FOR_Qualification_Type.xml");
                    terminologyResources.add("/fhir/r4/codesystem/v1_0_3/KBV_CS_FOR_Ursache_Type.xml");

                    terminologyResources.add("/fhir/r4/namingsystems/Pruefnummer.xml");

                    structureDefinitionResources.add("/fhir/r4/profile/v1_0_1/KBV_PR_ERP_Bundle.xml");
                    structureDefinitionResources.add("/fhir/r4/profile/v1_0_1/KBV_PR_ERP_Composition.xml");
                    structureDefinitionResources.add("/fhir/r4/profile/v1_0_1/KBV_PR_ERP_Medication_Compounding.xml");
                    structureDefinitionResources.add("/fhir/r4/profile/v1_0_1/KBV_PR_ERP_Medication_FreeText.xml");
                    structureDefinitionResources.add("/fhir/r4/profile/v1_0_1/KBV_PR_ERP_Medication_Ingredient.xml");
                    structureDefinitionResources.add("/fhir/r4/profile/v1_0_1/KBV_PR_ERP_Medication_PZN.xml");
                    structureDefinitionResources.add("/fhir/r4/profile/v1_0_1/KBV_PR_ERP_PracticeSupply.xml");
                    structureDefinitionResources.add("/fhir/r4/profile/v1_0_1/KBV_PR_ERP_Prescription.xml");

                    structureDefinitionResources.add("/fhir/r4/profile/v1_0_3/KBV_PR_FOR_Coverage.xml");
                    structureDefinitionResources.add("/fhir/r4/profile/v1_0_3/KBV_PR_FOR_Organization.xml");
                    structureDefinitionResources.add("/fhir/r4/profile/v1_0_3/KBV_PR_FOR_Patient.xml");
                    structureDefinitionResources.add("/fhir/r4/profile/v1_0_3/KBV_PR_FOR_Practitioner.xml");
                    structureDefinitionResources.add("/fhir/r4/profile/v1_0_3/KBV_PR_FOR_PractitionerRole.xml");

                    structureDefinitionResources.add("/fhir/r4/extension/v1_0_1/KBV_EX_ERP_Accident.xml");
                    structureDefinitionResources.add("/fhir/r4/extension/v1_0_1/KBV_EX_ERP_BVG.xml");
                    structureDefinitionResources.add("/fhir/r4/extension/v1_0_1/KBV_EX_ERP_DosageFlag.xml");
                    structureDefinitionResources.add("/fhir/r4/extension/v1_0_1/KBV_EX_ERP_EmergencyServicesFee.xml");
                    structureDefinitionResources.add("/fhir/r4/extension/v1_0_1/KBV_EX_ERP_Medication_Category.xml");
                    structureDefinitionResources.add("/fhir/r4/extension/v1_0_1/KBV_EX_ERP_Medication_CompoundingInstruction.xml");
                    structureDefinitionResources.add("/fhir/r4/extension/v1_0_1/KBV_EX_ERP_Medication_Ingredient_Amount.xml");
                    structureDefinitionResources.add("/fhir/r4/extension/v1_0_1/KBV_EX_ERP_Medication_Ingredient_Form.xml");
                    structureDefinitionResources.add("/fhir/r4/extension/v1_0_1/KBV_EX_ERP_Medication_Packaging.xml");
                    structureDefinitionResources.add("/fhir/r4/extension/v1_0_1/KBV_EX_ERP_Medication_Vaccine.xml");
                    structureDefinitionResources.add("/fhir/r4/extension/v1_0_1/KBV_EX_ERP_Multiple_Prescription.xml");
                    structureDefinitionResources.add("/fhir/r4/extension/v1_0_1/KBV_EX_ERP_PracticeSupply_Payor.xml");
                    structureDefinitionResources.add("/fhir/r4/extension/v1_0_1/KBV_EX_ERP_StatusCoPayment.xml");

                    structureDefinitionResources.add("/fhir/r4/extension/v1_0_3/KBV_EX_FOR_Alternative_IK.xml");
                    structureDefinitionResources.add("/fhir/r4/extension/v1_0_3/KBV_EX_FOR_Legal_basis.xml");
                    structureDefinitionResources.add("/fhir/r4/extension/v1_0_3/KBV_EX_FOR_PKV_Tariff.xml");

                    break;
            }

            this.myTerminologyResources = terminologyResources;
            this.myStructureDefinitionResources = structureDefinitionResources;
        }
    }

    @Override
    public List<IBaseResource> fetchAllConformanceResources() {
        ArrayList<IBaseResource> retVal = new ArrayList();
        retVal.addAll(this.myCodeSystems.values());
        retVal.addAll(this.myStructureDefinitions.values());
        retVal.addAll(this.myValueSets.values());
        return retVal;
    }

    @Override
    public List<IBaseResource> fetchAllStructureDefinitions() {
        return toList(this.provideStructureDefinitionMap());
    }

    @Override
    public IBaseResource fetchCodeSystem(String theSystem) {
        return this.fetchCodeSystemOrValueSet(theSystem, true);
    }

    private IBaseResource fetchCodeSystemOrValueSet(String theSystem, boolean codeSystem) {
        synchronized(this) {
            Map<String, IBaseResource> codeSystems = this.myCodeSystems;
            Map<String, IBaseResource> valueSets = this.myValueSets;
            String version;
            if (codeSystems == null || valueSets == null) {
                codeSystems = new HashMap();
                valueSets = new HashMap();
                this.initializeResourceLists();
                Iterator var6 = this.myTerminologyResources.iterator();

                while(var6.hasNext()) {
                    version = (String)var6.next();
                    this.loadCodeSystems((Map)codeSystems, (Map)valueSets, version);
                }

                this.myCodeSystems = (Map)codeSystems;
                this.myValueSets = (Map)valueSets;
            }

            String system = theSystem;
            if (theSystem.contains("|")) {
                version = theSystem.substring(theSystem.indexOf(124) + 1);
                if (version.matches("^[0-9.]+$")) {
                    system = theSystem.substring(0, theSystem.indexOf(124));
                }
            }

            return codeSystem ? (IBaseResource)((Map)codeSystems).get(system) : (IBaseResource)((Map)valueSets).get(system);
        }
    }

    @Override
    public IBaseResource fetchStructureDefinition(String theUrl) {
        String url = theUrl;
        if (!theUrl.startsWith(URL_PREFIX_STRUCTURE_DEFINITION)) {
            if (theUrl.indexOf(47) == -1) {
                url = URL_PREFIX_STRUCTURE_DEFINITION + theUrl;
            } else if (StringUtils.countMatches(theUrl, '/') == 1) {
                url = URL_PREFIX_STRUCTURE_DEFINITION_BASE + theUrl;
            }
        }

        Map<String, IBaseResource> structureDefinitionMap = this.provideStructureDefinitionMap();
        IBaseResource retVal = (IBaseResource)structureDefinitionMap.get(url);
        return retVal;
    }

    @Override
    public IBaseResource fetchValueSet(String theUrl) {
        IBaseResource retVal = this.fetchCodeSystemOrValueSet(theUrl, false);
        return retVal;
    }

    public void flush() {
        this.myCodeSystems = null;
        this.myStructureDefinitions = null;
    }

    @Override
    public FhirContext getFhirContext() {
        return this.myCtx;
    }

    private Map<String, IBaseResource> provideStructureDefinitionMap() {
        Map<String, IBaseResource> structureDefinitions = this.myStructureDefinitions;
        if (structureDefinitions == null) {
            structureDefinitions = new HashMap();
            this.initializeResourceLists();
            Iterator var2 = this.myStructureDefinitionResources.iterator();

            while(var2.hasNext()) {
                String next = (String)var2.next();
                this.loadStructureDefinitions((Map)structureDefinitions, next);
            }

            this.myStructureDefinitions = (Map)structureDefinitions;
        }

        return (Map)structureDefinitions;
    }

    private void loadCodeSystems(Map<String, IBaseResource> theCodeSystems, Map<String, IBaseResource> theValueSets, String theClasspath) {
        ourLog.infof("Loading CodeSystem/ValueSet from classpath: %s", theClasspath);
        InputStream inputStream = getClass().getResourceAsStream(theClasspath);
        InputStreamReader reader = null;
        if (inputStream != null) {
            try {
                reader = new InputStreamReader(inputStream, Constants.CHARSET_UTF8);
                List<IBaseResource> resources = this.parseBundle(reader);
                Iterator var7 = resources.iterator();

                while(var7.hasNext()) {
                    IBaseResource next = (IBaseResource)var7.next();
                    RuntimeResourceDefinition nextDef = this.getFhirContext().getResourceDefinition(next);
                    Map<String, IBaseResource> map = null;
                    String urlValueString = nextDef.getName();
                    byte var12 = -1;
                    switch(urlValueString.hashCode()) {
                        case -1345530543:
                            if (urlValueString.equals("ValueSet")) {
                                var12 = 1;
                            }
                            break;
                        case 1076953756:
                            if (urlValueString.equals("CodeSystem")) {
                                var12 = 0;
                            }
                    }

                    switch(var12) {
                        case 0:
                            map = theCodeSystems;
                            break;
                        case 1:
                            map = theValueSets;
                    }

                    if (map != null) {
                        urlValueString = this.getConformanceResourceUrl(next);
                        if (StringUtils.isNotBlank(urlValueString)) {
                            map.put(urlValueString, next);
                        }

                        switch(this.myCtx.getVersion().getVersion()) {
                            case DSTU2:
                            case DSTU2_HL7ORG:
                                IPrimitiveType<?> codeSystem = (IPrimitiveType)this.myCtx.newTerser().getSingleValueOrNull(next, "ValueSet.codeSystem.system", IPrimitiveType.class);
                                if (codeSystem != null && StringUtils.isNotBlank(codeSystem.getValueAsString())) {
                                    theCodeSystems.put(codeSystem.getValueAsString(), next);
                                }
                            case DSTU2_1:
                            case DSTU3:
                            case R4:
                            case R5:
                        }
                    }
                }
            } finally {
                try {
                    if (reader != null) {
                        reader.close();
                    }

                    inputStream.close();
                } catch (IOException var18) {
                    ourLog.warn("Failure closing stream", var18);
                }

            }
        } else {
            ourLog.warnf("Unable to load resource: %s", theClasspath);
        }

    }

    private void loadStructureDefinitions(Map<String, IBaseResource> theCodeSystems, String theClasspath) {
        ourLog.infof("Loading structure definitions from classpath: %s", theClasspath);

        try {
            InputStream valuesetText = KbvValidationSupport.class.getResourceAsStream(theClasspath);

            try {
                if (valuesetText != null) {
                    InputStreamReader reader = new InputStreamReader(valuesetText, Constants.CHARSET_UTF8);

                    try {
                        List<IBaseResource> resources = this.parseBundle(reader);
                        Iterator var6 = resources.iterator();

                        while(var6.hasNext()) {
                            IBaseResource next = (IBaseResource)var6.next();
                            String nextType = this.getFhirContext().getResourceType(next);
                            if ("StructureDefinition".equals(nextType)) {
                                String url = this.getConformanceResourceUrl(next);
                                if (StringUtils.isNotBlank(url)) {
                                    theCodeSystems.put(url, next);
                                }
                            }
                        }
                    } catch (Throwable var12) {
                        try {
                            reader.close();
                        } catch (Throwable var11) {
                            var12.addSuppressed(var11);
                        }

                        throw var12;
                    }

                    reader.close();
                } else {
                    ourLog.warnf("Unable to load resource: %s", theClasspath);
                }
            } catch (Throwable var13) {
                if (valuesetText != null) {
                    try {
                        valuesetText.close();
                    } catch (Throwable var10) {
                        var13.addSuppressed(var10);
                    }
                }

                throw var13;
            }

            if (valuesetText != null) {
                valuesetText.close();
            }
        } catch (IOException var14) {
            ourLog.warnf("Unable to load resource: %s", theClasspath);
        }

    }

    private String getConformanceResourceUrl(IBaseResource theResource) {
        return getConformanceResourceUrl(this.getFhirContext(), theResource);
    }

    private List<IBaseResource> parseBundle(InputStreamReader theReader) {
        IBaseResource parsedObject = this.getFhirContext().newXmlParser().parseResource(theReader);
        if (parsedObject instanceof IBaseBundle) {
            IBaseBundle bundle = (IBaseBundle)parsedObject;
            return BundleUtil.toListOfResources(this.getFhirContext(), bundle);
        } else {
            return Collections.singletonList(parsedObject);
        }
    }

    @Nullable
    public static String getConformanceResourceUrl(FhirContext theFhirContext, IBaseResource theResource) {
        String urlValueString = null;
        Optional<IBase> urlValue = theFhirContext.getResourceDefinition(theResource).getChildByName("url").getAccessor().getFirstValueOrNull(theResource);
        if (urlValue.isPresent()) {
            IPrimitiveType<?> urlValueType = (IPrimitiveType)urlValue.get();
            urlValueString = urlValueType.getValueAsString();
        }

        return urlValueString;
    }

    static List<IBaseResource> toList(Map<String, IBaseResource> theMap) {
        ArrayList<IBaseResource> retVal = new ArrayList(theMap.values());
        return Collections.unmodifiableList(retVal);
    }
}
