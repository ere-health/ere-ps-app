package config;

import java.io.IOException;
import java.io.InputStream;
import java.util.Objects;
import java.util.Properties;

/**
 * Singleton class reading the configuration variables from application.properties
 */
public enum ApplicationConfig {
    INSTANCE;

    private static final String DEFAULT_APPLICATION_CONFIG = "application.properties";

    private final String remoteServerUri;
    private final String remoteConfigurationFilename;
    private final String remoteConfigurationCreationFolder;
    private final String applicationPath;
    private final String archiveName;

    ApplicationConfig() {
        System.Logger logger = System.getLogger(ApplicationConfig.class.getName());

        Properties properties = new Properties();
        InputStream inputStream = getClass().getClassLoader().getResourceAsStream(DEFAULT_APPLICATION_CONFIG);

        if (inputStream != null) {
            try {
                properties.load(inputStream);
            } catch (IOException e) {
                logger.log(System.Logger.Level.ERROR, "Could not parse application configuration:");
                e.printStackTrace();
            }
        } else {
            logger.log(System.Logger.Level.ERROR, "Property file " + DEFAULT_APPLICATION_CONFIG +
                    " not found in the classpath");
        }

        remoteServerUri = properties.getProperty("remote.server");
        remoteConfigurationCreationFolder = properties.getProperty("remote.config.creation.path");
        remoteConfigurationFilename = properties.getProperty("remote.config.filename");
        applicationPath = properties.getProperty("application.path");
        archiveName = properties.getProperty("application.archive.filename");

        try {
            Objects.requireNonNull(inputStream).close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public String getRemoteServerUri() {
        return remoteServerUri;
    }

    public String getApplicationPath() {
        return applicationPath;
    }

    public String getArchiveName() {
        return archiveName;
    }

    public String getRemoteConfigurationFilename() {
        return remoteConfigurationFilename;
    }

    public String getRemoteConfigurationCreationFolder() {
        return remoteConfigurationCreationFolder;
    }
}
