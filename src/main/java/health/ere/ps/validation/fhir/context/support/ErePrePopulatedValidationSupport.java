package health.ere.ps.validation.fhir.context.support;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.microprofile.config.ConfigProvider;
import org.hl7.fhir.common.hapi.validation.support.PrePopulatedValidationSupport;
import org.hl7.fhir.r4.model.CodeSystem;
import org.hl7.fhir.r4.model.StructureDefinition;
import org.hl7.fhir.r4.model.ValueSet;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.List;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.parser.IParser;
import health.ere.ps.service.logging.EreLogger;

public class ErePrePopulatedValidationSupport extends PrePopulatedValidationSupport {
    private static final EreLogger ereLogger =
            EreLogger.getLogger(ErePrePopulatedValidationSupport.class);
    private static final List<EreLogger.SystemContext> systemContextList = List.of(
            EreLogger.SystemContext.KbvBundleValidator,
            EreLogger.SystemContext.KbvBundleValidatorConfiguration);
    private IParser xmlParser = FhirContext.forR4().newXmlParser();

    protected enum ConfigType {
        PROFILE, EXTENSION, VALUE_SET, CODE_SYSTEM, NAMING_SYSTEM, UNKNOWN
    }

    public ErePrePopulatedValidationSupport(FhirContext theContext) {
        super(theContext);

        ereLogger.setLoggingContext(systemContextList)
                .info("Loading KBV Validator configuration");
        initKbvValidatorConfiguration();
    }

    protected void addKbvProfile(InputStream configDefinitionInputStream) {
        addKbvProfile(null, null, configDefinitionInputStream);
    }

    protected void addKbvProfile(String configUrl,
                                 String configVersion,
                                 InputStream configDefinitionInputStream) {
        StructureDefinition structureDefinition;

        try (configDefinitionInputStream) {
            structureDefinition = xmlParser.parseResource(StructureDefinition.class,
                    configDefinitionInputStream);
            if (configUrl != null) {
                structureDefinition.setUrl(configUrl);
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

    protected void addKbvValueSet(InputStream configDefinitionInputStream) {
        addKbvValueSet(null, null, configDefinitionInputStream);
    }

    protected void addKbvValueSet(String configUrl, String configVersion,
                                  InputStream configDefinitionInputStream) {

        ValueSet valueSet;

        try (configDefinitionInputStream) {
            valueSet = xmlParser.parseResource(ValueSet.class,
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

    protected void addKbvCodeSystem(InputStream configDefinitionInputStream) {
        addKbvCodeSystem(null, null, configDefinitionInputStream);
    }

    protected void addKbvCodeSystem(String configUrl, String configVersion,
                                    InputStream configDefinitionInputStream) {

        CodeSystem codeSystem;

        try (configDefinitionInputStream) {
            codeSystem = xmlParser.parseResource(CodeSystem.class,
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
                    .fatal("Error occured while configuring validator", e);
        }
    }

    protected ConfigType getConfigType(Path kbvConfigFile) {
        String configFileName = kbvConfigFile.getFileName().toString();

        if (StringUtils.isNotBlank(configFileName) && configFileName.endsWith(".xml") &&
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
            switch (getConfigType(kbvConfigFile)) {
                case PROFILE:
                case EXTENSION:
                    addKbvProfile(bis);
                    break;

                case CODE_SYSTEM:
                    addKbvCodeSystem(bis);
                    break;

                case VALUE_SET:
                    addKbvValueSet(bis);
                    break;
            }
        }
    }
}
