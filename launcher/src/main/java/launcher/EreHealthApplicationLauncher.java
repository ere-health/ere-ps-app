package launcher;

import config.ApplicationConfig;
import org.update4j.LaunchContext;
import org.update4j.service.Launcher;
import popup.PopupManager;
import thirdparty.ThirdPartySoftwaresInstaller;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Runs the application, closes the popup and opens the webapp in Chrome
 */
public class EreHealthApplicationLauncher implements Launcher {
    private static final String DEFAULT_APPLICATION_JAR = "\\quarkus-app\\quarkus-run.jar";
    private static final System.Logger logger = System.getLogger(EreHealthApplicationLauncher.class.getName());
    private final PopupManager popupManager = PopupManager.INSTANCE;


    @Override
    public void run(LaunchContext context) {
        ApplicationConfig applicationConfig = ApplicationConfig.INSTANCE;
        logger.log(System.Logger.Level.INFO, "Now launching ere-health application from path:" +
                applicationConfig.getApplicationPath() + DEFAULT_APPLICATION_JAR);

        ProcessBuilder builder = new ProcessBuilder();
        builder.command("java", "-Dquarkus.profile=PU", "-jar", applicationConfig.getApplicationPath() + DEFAULT_APPLICATION_JAR);

        try {
            builder.start();
        } catch (IOException e) {
            logger.log(System.Logger.Level.ERROR, "Could not launch ere-health application due to:");
            e.printStackTrace();
        }

        try {
            Thread.sleep(5000);
            popupManager.closePopup();
            startWebappInChrome();
        } catch (InterruptedException | IOException e) {
            logger.log(System.Logger.Level.ERROR, "Could not start the webapp in Chrome due to:");
            e.printStackTrace();
        }

    }

    private void startWebappInChrome() throws IOException {
        if (Files.exists(Path.of(ThirdPartySoftwaresInstaller.CHROME_X86_PATH))) {
            Runtime.getRuntime().exec(ThirdPartySoftwaresInstaller.CHROME_X86_PATH +
                    " http://localhost:8080/frontend/app/src/index.html");
        } else if (Files.exists(Path.of(ThirdPartySoftwaresInstaller.CHROME_X64_PATH))) {
            Runtime.getRuntime().exec(ThirdPartySoftwaresInstaller.CHROME_X64_PATH +
                    " http://localhost:8080/frontend/app/src/index.html");
        } else {
            logger.log(System.Logger.Level.ERROR, "Could not start the webapp on Chrome as no Chrome was detected");
        }
    }
}
