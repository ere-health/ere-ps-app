import config.ApplicationConfig;
import config.RemoteConfigCreator;
import org.update4j.Configuration;
import updateHandler.CustomUpdateHandler;

import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;

public class AutoUpdater {
    private static final System.Logger logger = System.getLogger(AutoUpdater.class.getName());
    private static boolean isFirstInstallation = false;

    public static void main(String[] args) {
        System.setProperty("java.util.logging.SimpleFormatter.format", "[%1$tF %1$tT] [%4$-7s] %5$s %n");

        if (args.length > 0 && "--create-remote-config".equals(args[0])) {
            try {
                RemoteConfigCreator.createRemoteConfigurationFile();
            } catch (IOException e) {
                logger.log(System.Logger.Level.ERROR, "Error when creating the new remote configuration file");
                e.printStackTrace();
            }
        } else {
            ApplicationConfig applicationConfig = ApplicationConfig.INSTANCE;

            InputStreamReader remoteConfigurationFileInputStream = null;
            String remoteConfigurationFileUrl = applicationConfig.getRemoteServerUri() + "/"
                    + applicationConfig.getRemoteConfigurationFilename();
            try {
                remoteConfigurationFileInputStream = new InputStreamReader(new URL(remoteConfigurationFileUrl).openStream());
            } catch (IOException e) {
                logger.log(System.Logger.Level.ERROR, "Remote configuration file not found at url:" +
                        remoteConfigurationFileUrl);
                e.printStackTrace();
            }

            Configuration config = null;
            try {
                config = Configuration.read(remoteConfigurationFileInputStream);
                logger.log(System.Logger.Level.DEBUG, "Using config:" + config);
            } catch (IOException e) {
                logger.log(System.Logger.Level.ERROR,
                        "Configuration file could not be read, is the remote configuration valid?");
                e.printStackTrace();
            }

            if (Files.notExists(Path.of(applicationConfig.getApplicationPath()))) {
                isFirstInstallation = true;
                try {
                    Files.createDirectory(Path.of(applicationConfig.getApplicationPath()));
                } catch (IOException e) {
                    logger.log(System.Logger.Level.ERROR, "Could not create application folder:" +
                            applicationConfig.getApplicationPath());
                    e.printStackTrace();
                }
            }

            //Up-to-date logic involves putting all files to update into an archive but our download is already an archive
            //Also extracts the downloaded archive automatically in application.path, check CustomUpdateHandler#doneDownloads
            config.update();

            if (isFirstInstallation) {
                logger.log(System.Logger.Level.INFO, "First installation detected, creating the startup script");
                CustomUpdateHandler.createStartupScript();
            } else {
                logger.log(System.Logger.Level.INFO,
                        "Update process complete, now launching the ere-health application");
            }
            config.launch();
        }
    }
}
