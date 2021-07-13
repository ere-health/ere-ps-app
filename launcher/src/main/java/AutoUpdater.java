import config.ApplicationConfig;
import config.RemoteConfigCreator;
import org.update4j.Configuration;
import popup.PopupManager;
import thirdparty.ThirdPartySoftwaresInstaller;

import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;

public class AutoUpdater {
    private static final System.Logger log = System.getLogger(AutoUpdater.class.getName());
    private static final PopupManager popupManager = PopupManager.INSTANCE;
    private static final ApplicationConfig applicationConfig = ApplicationConfig.INSTANCE;
    private static final ThirdPartySoftwaresInstaller thirdPartySoftwaresInstaller =
            ThirdPartySoftwaresInstaller.INSTANCE;

    private static boolean isFirstInstallation = false;

    public static void main(String[] args) throws InterruptedException, IOException {
        System.setProperty("java.util.logging.SimpleFormatter.format", "[%1$tF %1$tT] [%4$-7s] %5$s %n");

        if (args.length > 0 && "--create-remote-config".equals(args[0])) {
            try {
                RemoteConfigCreator.createRemoteConfigurationFile();
            } catch (IOException e) {
                log.log(System.Logger.Level.ERROR, "Error when creating the new remote configuration file");
                e.printStackTrace();
            }
        } else {
            if (!System.getProperty("os.name").toLowerCase().contains("windows")) {
                popupManager.addTextToPanelAndLog("Only Windows is supported for now, aborting installation");
                System.exit(1);
            } else {
                Configuration config = readRemoteConfiguration(applicationConfig);

                createApplicationPathIfNeeded(applicationConfig);

                if (isFirstInstallation) {
                    popupManager.addTextToPanelAndLog("Installation of ere-ps-app started");
                } else {
                    popupManager.addTextToPanelAndLog("Update process of ere-ps-app started");
                }

                config.update();

                if (isFirstInstallation) {
                    thirdPartySoftwaresInstaller.installJavaIfNeeded();
                    thirdPartySoftwaresInstaller.installChromeIfNeeded();
                    thirdPartySoftwaresInstaller.createWindowsStartupScript();
                    popupManager.addTextToPanelAndLog("Everything has been installed successfully");
                } else {
                    popupManager.addTextToPanelAndLog("Update process complete, now launching the ere-health application");
                }

                popupManager.addTextToPanelAndLog("Process done successfully! Now launching ere-ps-app and starting Chrome");
                config.launch();
            }
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
