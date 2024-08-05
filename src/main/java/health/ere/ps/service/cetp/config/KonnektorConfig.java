package health.ere.ps.service.cetp.config;

import health.ere.ps.model.config.UserConfigurations;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Properties;
import java.util.concurrent.Semaphore;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import static health.ere.ps.utils.Utils.writeFile;

public class KonnektorConfig {

    private static final Logger log = Logger.getLogger(KonnektorConfig.class.getName());

    public static final String FAILED = "failed";
    public static final String PROPERTIES_EXT = ".properties";

    Integer port;
    UserConfigurations userConfigurations;
    URI cardlinkEndpoint;
    String subscriptionId;
    File folder;
    OffsetDateTime subscriptionTime;

    private final Semaphore semaphore = new Semaphore(1);

    public KonnektorConfig() {
    }

    public KonnektorConfig(
        File folder,
        Integer port,
        UserConfigurations userConfigurations,
        URI cardlinkEndpoint
    ) {
        this.folder = folder;
        this.port = port;
        this.userConfigurations = userConfigurations;
        this.cardlinkEndpoint = cardlinkEndpoint;

        subscriptionId = null;
        subscriptionTime = OffsetDateTime.now().minusDays(30);
    }

    public static List<KonnektorConfig> readFromFolder(String folder) {
        File folderFile = new File(folder);
        if (folderFile.exists() && folderFile.isDirectory()) {
            File[] files = folderFile.listFiles();
            if (files != null) {
                return Arrays.stream(files)
                    .filter(File::isDirectory)
                    .map(KonnektorConfig::generateKonnektorConfig)
                    .filter(Objects::nonNull)
                    .sorted(Comparator.comparing(KonnektorConfig::getPort))
                    .collect(Collectors.toList());
            }
        }
        return List.of();
    }

    public static KonnektorConfig generateKonnektorConfig(File folder) {
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
                    .orElse(OffsetDateTime.now()).minusDays(30); // force subscribe if no subscription is found
                try (var fis = new FileInputStream(actualSubscription)) {
                    Properties properties = new Properties();
                    properties.load(fis);
                    KonnektorConfig konnektorConfig = new KonnektorConfig();
                    konnektorConfig.port = Integer.parseInt(folder.getName());
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

    public static void saveFile(KonnektorConfig konnektorConfig, String subscriptionId, String error) {
        try {
            createNewSubscriptionIdFile(konnektorConfig.getFolder(), subscriptionId, error);
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

    private static void createNewSubscriptionIdFile(File folder, String subscriptionId, String error) throws IOException {
        writeFile(folder.getAbsolutePath() + "/" + subscriptionId, error);
        cleanUp(folder, subscriptionId);
    }

    public static void cleanUp(File folder, String subscriptionId) {
        Arrays.stream(folder.listFiles())
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

    public Semaphore getSemaphore() {
        return semaphore;
    }

    public Integer getPort() {
        return port;
    }

    public UserConfigurations getUserConfigurations() {
        return userConfigurations;
    }

    public String getHost() {
        String connectorBaseURL = userConfigurations.getConnectorBaseURL();
        return connectorBaseURL == null ? null : connectorBaseURL.split("//")[1];
    }

    public URI getCardlinkEndpoint() {
        return cardlinkEndpoint;
    }

    public String getSubscriptionId() {
        return subscriptionId;
    }

    public void setSubscriptionId(String subscriptionId) {
        this.subscriptionId = subscriptionId;
    }

    public File getFolder() {
        return folder;
    }

    public OffsetDateTime getSubscriptionTime() {
        return subscriptionTime;
    }

    public void setSubscriptionTime(OffsetDateTime subscriptionTime) {
        this.subscriptionTime = subscriptionTime;
    }
}