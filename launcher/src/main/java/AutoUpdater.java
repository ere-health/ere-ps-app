import config.ApplicationConfig;
import config.RemoteConfigCreator;
import org.update4j.Configuration;
import updateHandler.CustomUpdateHandler;
import popup.PopupManager;

import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;

public class AutoUpdater {
    private static final System.Logger log = System.getLogger(AutoUpdater.class.getName());
    private static final PopupManager popupManager = PopupManager.INSTANCE;

    private static boolean isFirstInstallation = false;

    public static void main(String[] args) throws InterruptedException {
        System.setProperty("java.util.logging.SimpleFormatter.format", "[%1$tF %1$tT] [%4$-7s] %5$s %n");

        if (args.length > 0 && "--create-remote-config".equals(args[0])) {
            try {
                RemoteConfigCreator.createRemoteConfigurationFile();
            } catch (IOException e) {
                log.log(System.Logger.Level.ERROR, "Error when creating the new remote configuration file");
                e.printStackTrace();
            }
        } else {
            ApplicationConfig applicationConfig = ApplicationConfig.INSTANCE;
            Configuration config = readRemoteConfiguration(applicationConfig);

            createApplicationPathIfNeeded(applicationConfig);

            if (isFirstInstallation) {
                popupManager.addTextToPanel("Installation of ere-ps-app started");
            } else {
                popupManager.addTextToPanel("Update process of ere-ps-app started");
            }

            //Up-to-date logic involves putting all files to update into an archive but our download is already an archive
            //Also extracts the downloaded archive automatically in application.path, check CustomUpdateHandler#doneDownloads
            config.update();

            if (isFirstInstallation) {
                log.log(System.Logger.Level.INFO, "First installation detected, creating the startup script");
                popupManager.addTextToPanel("Now creating startup script");
                CustomUpdateHandler.createStartupScript();
            } else {
                log.log(System.Logger.Level.INFO,
                        "Update process complete, now launching the ere-health application");
            }

            popupManager.addTextToPanel("Process done successfully! Now launching ere-ps-app");
            log.log(System.Logger.Level.INFO, "Fake launching of the app");
//            config.launch();

            Thread.sleep(3000);
            popupManager.closePopup();
        }
    }

    private static Configuration readRemoteConfiguration(ApplicationConfig applicationConfig) {
        InputStreamReader remoteConfigurationFileInputStream = null;
        String remoteConfigurationFileUrl = applicationConfig.getRemoteServerUri() + "/"
                + applicationConfig.getRemoteConfigurationFilename();
        try {
            remoteConfigurationFileInputStream = new InputStreamReader(new URL(remoteConfigurationFileUrl).openStream());
        } catch (IOException e) {
            log.log(System.Logger.Level.ERROR, "Remote configuration file not found at url:" +
                    remoteConfigurationFileUrl);
            e.printStackTrace();
        }

        Configuration config = null;
        try {
            config = Configuration.read(remoteConfigurationFileInputStream);
            log.log(System.Logger.Level.DEBUG, "Using config:" + config);
        } catch (IOException e) {
            log.log(System.Logger.Level.ERROR,
                    "Configuration file could not be read, is the remote configuration valid?");
            e.printStackTrace();
        }
        return config;
    }

    private static void createApplicationPathIfNeeded(ApplicationConfig applicationConfig) {
        if (Files.notExists(Path.of(applicationConfig.getApplicationPath()))) {
            isFirstInstallation = true;
            try {
                Files.createDirectory(Path.of(applicationConfig.getApplicationPath()));
            } catch (IOException e) {
                log.log(System.Logger.Level.ERROR, "Could not create application folder:" +
                        applicationConfig.getApplicationPath());
                e.printStackTrace();
            }
        }
    }
}
