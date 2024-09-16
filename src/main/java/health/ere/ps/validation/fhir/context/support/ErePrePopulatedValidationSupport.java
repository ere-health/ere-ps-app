package health.ere.ps.validation.fhir.context.support;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.List;
import java.util.logging.Logger;

import health.ere.ps.service.fhir.FHIRService;
import org.apache.commons.lang3.StringUtils;
import org.eclipse.microprofile.config.ConfigProvider;
import org.hl7.fhir.common.hapi.validation.support.PrePopulatedValidationSupport;
import org.hl7.fhir.r4.model.CodeSystem;
import org.hl7.fhir.r4.model.StructureDefinition;
import org.hl7.fhir.r4.model.ValueSet;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.parser.IParser;
import health.ere.ps.service.logging.EreLogger;

public class ErePrePopulatedValidationSupport extends PrePopulatedValidationSupport {
    private static final EreLogger ereLogger =
            EreLogger.getLogger(ErePrePopulatedValidationSupport.class);
    private static final List<EreLogger.SystemContext> systemContextList = List.of(
            EreLogger.SystemContext.KbvBundleValidator,
            EreLogger.SystemContext.KbvBundleValidatorConfiguration);

    private static final FhirContext fhirContext = FHIRService.getFhirContext();
    private static final IParser xmlParser = fhirContext.newXmlParser();
    private static final IParser jsonParser = fhirContext.newJsonParser();
    private static Logger log = Logger.getLogger(ErePrePopulatedValidationSupport.class.getName());

    protected enum ConfigType {
        PROFILE, EXTENSION, VALUE_SET, CODE_SYSTEM, NAMING_SYSTEM, UNKNOWN
    }

    public ErePrePopulatedValidationSupport(FhirContext theContext) {
        super(theContext);

        ereLogger.setLoggingContext(systemContextList)
                .info("Loading KBV Validator configuration");
        initKbvValidatorConfiguration();
    }

    protected void addKbvProfile(InputStream configDefinitionInputStream, boolean json) {
        addKbvProfile(null, null, configDefinitionInputStream, json);
    }

    protected void addKbvProfile(String configUrl,
                                 String configVersion,
                                 InputStream configDefinitionInputStream, boolean json) {
        StructureDefinition structureDefinition;

        try (configDefinitionInputStream) {
            structureDefinition = json ? jsonParser.parseResource(StructureDefinition.class,
                    configDefinitionInputStream) : xmlParser.parseResource(StructureDefinition.class,
                    configDefinitionInputStream);

            if (configUrl != null) {
                structureDefinition.setUrl(configUrl);
            } else {
                if (!structureDefinition.getType().equals("Extension")) {
                    structureDefinition.setUrl(structureDefinition.getUrl() + "|" + structureDefinition.getVersion());
                }
            }

            if (configVersion != null) {
                structureDefinition.setVersion(configVersion);
            }

            addStructureDefinition(structureDefinition);
        } catch (IOException e) {
            ereLogger.setLoggingContext(systemContextList,
                    "Cannot load KBV Profile Config files", true)
                    .setLoggingContext(systemContextList)
                    .errorf(e, "Error loading StructureDefinition " +
                            "profile %s", configUrl);
        }
    }

    protected void addKbvValueSet(InputStream configDefinitionInputStream, boolean json) {
        addKbvValueSet(null, null, configDefinitionInputStream, json);
    }

    protected void addKbvValueSet(String configUrl, String configVersion,
                                  InputStream configDefinitionInputStream, boolean json) {

        ValueSet valueSet;

        try (configDefinitionInputStream) {
            valueSet = json ? jsonParser.parseResource(ValueSet.class,
                    configDefinitionInputStream) : xmlParser.parseResource(ValueSet.class,
                    configDefinitionInputStream);
            if (configUrl != null) {
                valueSet.setUrl(configUrl);
            }

            if (configVersion != null) {
                valueSet.setVersion(configVersion);
            }

            addValueSet(valueSet);
        } catch (IOException e) {
            ereLogger.setLoggingContext(systemContextList,
                    "Cannot load KBV ValueSet Config Files",
                    true)
                    .errorf(e, "Error loading ValueSet profile %s",
                            configUrl);
        }
    }

    protected void addKbvCodeSystem(InputStream configDefinitionInputStream, boolean json) {
        addKbvCodeSystem(null, null, configDefinitionInputStream, json);
    }

    protected void addKbvCodeSystem(String configUrl, String configVersion,
                                    InputStream configDefinitionInputStream, boolean json) {

        CodeSystem codeSystem;

        try (configDefinitionInputStream) {
            codeSystem =  json ? jsonParser.parseResource(CodeSystem.class,
                    configDefinitionInputStream) : xmlParser.parseResource(CodeSystem.class,
                    configDefinitionInputStream);

            if (configUrl != null) {
                codeSystem.setUrl(configUrl);
            }

            if (configVersion != null) {
                codeSystem.setVersion(configVersion);
            }

            addCodeSystem(codeSystem);
        } catch (IOException e) {
            ereLogger.setLoggingContext(systemContextList,
                    "Cannot load KBV Code System Config files", true)
                    .errorf(e, "Error loading CodeSystem profile %s", configUrl);
        }
    }

    public void initKbvValidatorConfiguration() {
        String kbvValidatorConfigDirectory =
                ConfigProvider.getConfig().getValue("kbv.validator.config.dir", String.class);
        Path start = Path.of(kbvValidatorConfigDirectory).toAbsolutePath();

        try {
            Files.walkFileTree(start, new SimpleFileVisitor<Path>() {

                @Override
                public FileVisitResult visitFile(Path file, BasicFileAttributes attrs)
                        throws IOException {
                    applyConfiguration(file);
                    return FileVisitResult.CONTINUE;
                }
            });
        } catch (IOException e) {
            ereLogger.setLoggingContext(systemContextList,
                    "Cannot initialise KBV Validator", true)
                    .fatal("Error occurred while configuring validator", e);
        }
    }

    protected ConfigType getConfigType(Path kbvConfigFile) {
        String configFileName = kbvConfigFile.getFileName().toString();

        if (StringUtils.isNotBlank(configFileName) && (configFileName.endsWith(".xml") || configFileName.endsWith(".json")) &&
                (configFileName.contains("ERP") || configFileName.contains("FOR") ||
                        configFileName.contains("Base") || configFileName.contains("Profile-") ||
                        configFileName.contains("Extension-") || configFileName.contains("ValueSet-") ||
                        configFileName.contains("CodeSystem-") || configFileName.contains("SFHIR"))) {
            if (configFileName.startsWith("KBV_PR") || configFileName.startsWith("KBVPR") ||
                    configFileName.startsWith("Profile-")) {
                return ConfigType.PROFILE;
            } else if (configFileName.startsWith("KBV_CS") || configFileName.startsWith("KBVCS") ||
                    configFileName.startsWith("CodeSystem-")) {
                return ConfigType.CODE_SYSTEM;
            } else if (configFileName.startsWith("KBV_EX") || configFileName.startsWith("KBVEX") ||
                    configFileName.startsWith("Extension-")) {
                return ConfigType.EXTENSION;
            } else if (configFileName.startsWith("KBV_VS") || configFileName.startsWith("KBVVS") ||
                    configFileName.startsWith("ValueSet-")) {
                return ConfigType.VALUE_SET;
            } else if (configFileName.startsWith("KBV_NS") || configFileName.startsWith("KBVNS")) {
                return ConfigType.NAMING_SYSTEM;
            }
        }
        return ConfigType.UNKNOWN;
    }

    protected void applyConfiguration(Path kbvConfigFile) throws IOException {
        try (BufferedInputStream bis = new BufferedInputStream(Files.newInputStream(kbvConfigFile))) {
            boolean json = kbvConfigFile.getFileName().toString().endsWith(".json");
            switch (getConfigType(kbvConfigFile)) {
                case PROFILE:
                case EXTENSION:
                    log.fine("Adding profile or extension: "+kbvConfigFile.getFileName().toString());
                    addKbvProfile(bis, json);
                    break;
                case CODE_SYSTEM:
                    log.fine("Adding code system: "+kbvConfigFile.getFileName().toString());
                    addKbvCodeSystem(bis, json);
                    break;
                case VALUE_SET:
                    log.fine("Adding value set: "+kbvConfigFile.getFileName().toString());
                    addKbvValueSet(bis, json);
                    break;
                default:
                    log.fine("No config type for: "+kbvConfigFile.getFileName().toString());
                    break;
                
            }
        }
    }
}
