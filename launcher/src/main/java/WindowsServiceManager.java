import config.ApplicationConfig;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class WindowsServiceManager {

    private static final System.Logger logger = System.getLogger(WindowsServiceManager.class.getName());
    private static final ApplicationConfig applicationConfig = ApplicationConfig.INSTANCE;


    private static void createServiceInstallerConfig() throws IOException {
        Path serviceInstallerConfigPath = Paths.get(applicationConfig.getApplicationPath() + "\\service_installer.xml");
        Charset charset = StandardCharsets.UTF_8;

        logger.log(System.Logger.Level.INFO, "Current path:" + System.getProperty("user.dir"));
        String content = Files.readString(serviceInstallerConfigPath, charset);
        content = content.replaceAll("\\$PATH",
                System.getProperty("user.dir").replace("\\", "\\\\"));
        Files.write(serviceInstallerConfigPath, content.getBytes(charset));
    }

    public static void createAndStartWindowsService() {
        logger.log(System.Logger.Level.INFO, "Now creating the ere-health Windows service.");

        ProcessBuilder setEnvVariableProcess = new ProcessBuilder();
        setEnvVariableProcess.command("setx", applicationConfig.getApplicationPathEnvVariable(),
                System.getProperty("user.dir"));

        String binPath = "\"java -jar %ERE_PATH%ere-health-launcher.jar\"";
        ProcessBuilder installProcess = new ProcessBuilder();
        installProcess.command("powershell", "-Command",
                "Start-Process 'sc' -ArgumentList 'create','ERE_ESSAI2','binPath=" + binPath + "' -Verb runAs");

        ProcessBuilder startProcess = new ProcessBuilder();
        startProcess.command("powershell", "-Command",
                "Start-Process 'C:\\Users\\IEUser\\Desktop\\app\\application\\service_installer.exe' 'start' -Verb runAs");
        try {
//            createServiceInstallerConfig();
            setEnvVariableProcess.start();
//            installProcess.start();
            String command = "powershell -Command \"Start-Process 'sc' -ArgumentList 'create','ERE_ESSAI2','binPath= \"java -jar %ERE_PATH%\\ere-health-launcher.jar\"' -Verb runAs\"";
            logger.log(System.Logger.Level.INFO, "command:" + command);
            Runtime.getRuntime().exec(command);
//            startProcess.start();
        } catch (IOException e) {
            logger.log(System.Logger.Level.ERROR, "Could not install ere-health Windows service:");
            e.printStackTrace();
        }
    }
}
