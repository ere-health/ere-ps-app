package health.ere.ps.service.cetp.config;

import health.ere.ps.model.config.UserConfigurations;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
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

public class KonnektorConfig {

    public static final String DEFAULT_SUBSCRIPTION = "default.properties";

    private static final Logger log = Logger.getLogger(KonnektorConfig.class.getName());

    Integer port;
    UserConfigurations userConfigurations;
    URI cardlinkEndpoint;
    String subscriptionId;
    File folder;

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

        subscriptionId = DEFAULT_SUBSCRIPTION;
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
        Optional<File> actualOpt = Arrays.stream(folder.listFiles())
            .filter(f -> f.getName().endsWith(".properties"))
            .max(Comparator.comparingLong(File::lastModified));
        if (actualOpt.isPresent()) {
            File actualSubscription = actualOpt.get();
            if (actualSubscription.exists()) {
                String subscriptionId = actualSubscription.getName();
                try (var fis = new FileInputStream(actualSubscription)) {
                    Properties properties = new Properties();
                    properties.load(fis);
                    KonnektorConfig konnektorConfig = new KonnektorConfig();
                    konnektorConfig.port = Integer.parseInt(folder.getName());
                    konnektorConfig.userConfigurations = new UserConfigurations(properties);
                    konnektorConfig.cardlinkEndpoint = new URI(properties.getProperty("cardlinkServerURL"));
                    konnektorConfig.subscriptionId = subscriptionId;
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

    private static String preparePropertiesPath(String folderPath, String file) {
        return folderPath + "/" + file + ".properties";
    }

    public static void recreateSubscriptionProperties(
        File folder,
        String prevSubscriptionId,
        String subscriptionId
    ) throws IOException {
        String folderPath = folder.getAbsolutePath();
        String prevSubscriptionPath = folderPath + "/" + prevSubscriptionId;
        File prevPropertiesFile = new File(prevSubscriptionPath);
        if (prevPropertiesFile.exists()) {
            try (FileInputStream is = new FileInputStream(prevSubscriptionPath)) {
                try (FileOutputStream os = new FileOutputStream(preparePropertiesPath(folderPath, subscriptionId))) {
                    is.transferTo(os);
                }
            }
        } else {
            try (FileOutputStream os = new FileOutputStream(preparePropertiesPath(folderPath, subscriptionId))) {
                // TODO confirm which properties to write for default KonnektorConfig when user.properties is empty
                os.flush();
            }
        }
        Arrays.stream(folder.listFiles())
            .filter(file -> !file.getName().contains(subscriptionId))
            .forEach(file -> {
                boolean deleted = file.delete();
                if (!deleted) {
                    String msg = String.format("Unable to delete previous subscription file: %s", file.getName());
                    log.log(Level.SEVERE, msg);
                    file.renameTo(new File("failed_" + file.getAbsolutePath()));
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
}
