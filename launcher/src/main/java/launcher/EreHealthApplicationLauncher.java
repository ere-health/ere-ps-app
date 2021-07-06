package launcher;

import config.ApplicationConfig;
import org.update4j.LaunchContext;
import org.update4j.service.Launcher;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

public class EreHealthApplicationLauncher implements Launcher {
    public static final String DEFAULT_APPLICATION_JAR = "\\quarkus-app\\quarkus-run.jar";
    private static final System.Logger logger = System.getLogger(EreHealthApplicationLauncher.class.getName());

    @Override
    public void run(LaunchContext context) {
        ApplicationConfig applicationConfig = ApplicationConfig.INSTANCE;
        logger.log(System.Logger.Level.INFO, "Now launching ere-health application on path:" +
                applicationConfig.getApplicationPath() + DEFAULT_APPLICATION_JAR);

        ProcessBuilder builder = new ProcessBuilder();
        builder.command("java", "-jar", applicationConfig.getApplicationPath() + DEFAULT_APPLICATION_JAR);

        try {
            //TODO: Remove this at the end of the task
            builder.redirectErrorStream(true);
            Process start = builder.start();

            BufferedReader in = new BufferedReader(new InputStreamReader(start.getInputStream()));
            String line;
            while ((line = in.readLine()) != null) {
                logger.log(System.Logger.Level.INFO, line);
            }
            logger.log(System.Logger.Level.INFO, "launch finished");

            in.close();
        } catch (IOException e) {
            logger.log(System.Logger.Level.ERROR, "Could not launch ere-health application due to:");
            e.printStackTrace();
        }
    }
}
