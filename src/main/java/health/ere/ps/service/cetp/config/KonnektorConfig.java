package health.ere.ps.service.cetp.config;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import health.ere.ps.model.config.UserConfigurations;

public class KonnektorConfig {
    private static final Logger log = Logger.getLogger(KonnektorConfig.class.getName());
    Integer port;
    UserConfigurations userConfigurations;
    URI cardlinkEndpoint;

    public KonnektorConfig() {

    }

    public KonnektorConfig(Integer port, UserConfigurations userConfigurations, URI cardlinkEndpoint) {
        this.port = port;
        this.userConfigurations = userConfigurations;
        this.cardlinkEndpoint = cardlinkEndpoint;
    }

    public static List<KonnektorConfig> readFromFolder(String folder) {
        File folderFile = new File(folder);
        if(folderFile.exists() && folderFile.isDirectory()) {
            File[] files = folderFile.listFiles();
            if(files != null) {
                return Arrays.stream(files)
                        .filter(file -> file.isDirectory())
                        .map(file -> {
                            return generateKonnektorConfig(file);
                        })
                        .filter(Objects::nonNull)
                        .sorted((a,b) -> a.getPort().compareTo(b.getPort()))
                        .collect(Collectors.toList());
            }
        }
        return Arrays.asList();
    }

    public static KonnektorConfig generateKonnektorConfig(File folder) {
        var userProperties = new File(folder, "user.properties");
        if(userProperties.exists()) {
            try (var fis = new FileInputStream(userProperties)) {
                Properties properties = new Properties();
                properties.load(fis);
                KonnektorConfig konnektorConfig = new KonnektorConfig();
                konnektorConfig.port = Integer.parseInt(folder.getName());
                konnektorConfig.userConfigurations = new UserConfigurations(properties);
                konnektorConfig.cardlinkEndpoint = new URI(properties.getProperty("cardlinkServerURL"));
                return konnektorConfig;
            } catch (URISyntaxException | IOException e) {
                log.log(Level.WARNING, "Could not read konnektor config", e);
            }
        }
        return null;
    }

    public Integer getPort() {
        return port;
    }

    public void setPort(Integer port) {
        this.port = port;
    }

    public UserConfigurations getUserConfigurations() {
        return userConfigurations;
    }

    public void setUserConfigurations(UserConfigurations userConfigurations) {
        this.userConfigurations = userConfigurations;
    }

    public URI getCardlinkEndpoint() {
        return cardlinkEndpoint;
    }

    public void setCardlinkEndpoint(URI cardlinkEndpoint) {
        this.cardlinkEndpoint = cardlinkEndpoint;
    }

}
