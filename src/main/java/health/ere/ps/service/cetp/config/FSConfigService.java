package health.ere.ps.service.cetp.config;

import health.ere.ps.config.AppConfig;
import health.ere.ps.config.UserConfig;
import health.ere.ps.model.config.UserConfigurations;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.eclipse.microprofile.config.inject.ConfigProperty;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import static health.ere.ps.service.cetp.SubscriptionManager.FAILED;
import static health.ere.ps.utils.Utils.writeFile;

@ApplicationScoped
public class FSConfigService implements KonnektorConfigService {

    private final static Logger log = Logger.getLogger(FSConfigService.class.getName());

    public static final String PROPERTIES_EXT = ".properties";

    @ConfigProperty(name = "ere.per.konnektor.config.folder")
    String configFolder;

    @Inject
    AppConfig appConfig;

    @Inject
    UserConfig userConfig;


    @Override
    public Map<String, KonnektorConfig> loadConfigs() {
        List<KonnektorConfig> configs = new ArrayList<>();
        var konnektorConfigFolder = new File(configFolder);
        if (konnektorConfigFolder.exists()) {
            configs = readFromPath(konnektorConfigFolder.getAbsolutePath());
        }
        if (configs.isEmpty()) {
            configs.add(
                new KonnektorConfig(
                    konnektorConfigFolder,
                    appConfig.getCetpPort(),
                    userConfig.getConfigurations(),
                    appConfig.getCardLinkURI()
                )
            );
        }
        return configs.stream().collect(Collectors.toMap(this::getKonnectorKey, config -> config));
    }

    private String getKonnectorKey(KonnektorConfig config) {
        String konnectorHost = config.getHost();
        String host = konnectorHost == null ? appConfig.getKonnectorHost() : konnectorHost;
        return String.format("%d_%s", config.getCetpPort(), host);
    }

    public List<KonnektorConfig> readFromPath(String path) {
        File folderFile = new File(path);
        if (folderFile.exists() && folderFile.isDirectory()) {
            File[] files = folderFile.listFiles();
            if (files != null) {
                return Arrays.stream(files)
                    .filter(File::isDirectory)
                    .map(this::fromFolder)
                    .filter(Objects::nonNull)
                    .sorted(Comparator.comparing(KonnektorConfig::getCetpPort))
                    .collect(Collectors.toList());
            }
        }
        return List.of();
    }

    private KonnektorConfig fromFolder(File folder) {
        Optional<File> userPropertiesOpt = Arrays.stream(folder.listFiles())
            .filter(f -> f.getName().endsWith(PROPERTIES_EXT))
            .max(Comparator.comparingLong(File::lastModified));

        Optional<File> subscriptionFileOpt = Arrays.stream(folder.listFiles())
            .filter(f -> !f.getName().startsWith(FAILED) && !f.getName().endsWith(PROPERTIES_EXT))
            .max(Comparator.comparingLong(File::lastModified));

        if (userPropertiesOpt.isPresent()) {
            File actualSubscription = userPropertiesOpt.get();
            if (actualSubscription.exists()) {
                String subscriptionId = subscriptionFileOpt.map(File::getName).orElse(null);
                OffsetDateTime subscriptionTime = subscriptionFileOpt
                    .map(f -> OffsetDateTime.ofInstant(Instant.ofEpochMilli(f.lastModified()), ZoneId.systemDefault()))
                    .orElse(OffsetDateTime.now().minusDays(30)); // force subscribe if no subscription is found
                try (var fis = new FileInputStream(actualSubscription)) {
                    Properties properties = new Properties();
                    properties.load(fis);
                    KonnektorConfig konnektorConfig = new KonnektorConfig();
                    konnektorConfig.cetpPort = Integer.parseInt(folder.getName());
                    konnektorConfig.userConfigurations = new UserConfigurations(properties);
                    konnektorConfig.cardlinkEndpoint = new URI(properties.getProperty("cardlinkServerURL"));
                    konnektorConfig.subscriptionId = subscriptionId;
                    konnektorConfig.subscriptionTime = subscriptionTime;
                    konnektorConfig.folder = folder;
                    return konnektorConfig;
                } catch (URISyntaxException | IOException e) {
                    String msg = String.format(
                        "Could not read konnektor config: folder=%s, subscriptionId=%s", folder.getName(), subscriptionId
                    );
                    log.log(Level.WARNING, msg, e);
                }
            }
        }
        return null;
    }

    @Override
    public void saveSubscription(KonnektorConfig konnektorConfig, String subscriptionId, String error) {
        try {
            writeFile(konnektorConfig.getFolder().getAbsolutePath() + "/" + subscriptionId, error);
            cleanUp(konnektorConfig, subscriptionId);
            konnektorConfig.setSubscriptionId(subscriptionId);
            konnektorConfig.setSubscriptionTime(OffsetDateTime.now());
        } catch (IOException e) {
            String msg = String.format(
                "Error while recreating subscription properties in folder: %s",
                konnektorConfig.getFolder().getAbsolutePath()
            );
            log.log(Level.SEVERE, msg, e);
        }
    }

    @Override
    public void cleanUp(KonnektorConfig konnektorConfig, String subscriptionId) {
        Arrays.stream(konnektorConfig.getFolder().listFiles())
            .filter(file -> !file.getName().equals(subscriptionId) && !file.getName().endsWith(PROPERTIES_EXT))
            .forEach(file -> {
                boolean deleted = file.delete();
                if (!deleted) {
                    String msg = String.format("Unable to delete previous subscription file: %s", file.getName());
                    log.log(Level.SEVERE, msg);
                    file.renameTo(new File(String.format("%s_DELETING", file.getAbsolutePath())));
                }
            });
    }
}
