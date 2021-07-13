package thirdparty;

import popup.PopupManager;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;


/**
 * Install Java and Chrome if needed + creates the startup script
 */
public enum ThirdPartySoftwaresInstaller {
    INSTANCE;

    private static final System.Logger log = System.getLogger(ThirdPartySoftwaresInstaller.class.getName());
    private static final PopupManager popupManager = PopupManager.INSTANCE;

    public static final String CHROME_X86_PATH = "C:\\Program Files (x86)\\Google\\Chrome\\Application\\chrome.exe";
    public static final String CHROME_X64_PATH = "C:\\Program Files\\Google\\Chrome\\Application\\chrome.exe";
    private static final String INSTALL_CHROME_POWERSHELL_COMMAND =
            "powershell.exe -command \"$Path = $env:TEMP; $Installer = 'chrome_installer.exe'; Invoke-WebRequest " +
                    "'http://dl.google.com/chrome/install/375.126/chrome_installer.exe' -OutFile $Path$Installer; " +
                    "Start-Process -FilePath $Path$Installer -Args '/silent /install' -Verb RunAs -Wait; Remove-Item " +
                    "$Path$Installer\"";


    public void installJavaIfNeeded() throws IOException {
        if (System.getenv("JAVA_HOME") == null) {
            popupManager.addTextToPanelAndLog("Java is missing, setting bundled jre as default Java");

            String currentDir = System.getProperty("user.dir");
            String setPathCommand = "setx PATH \"" + currentDir + "\\jre\\bin;%PATH%\"";
            String setJavaHomeCommand = "setx JAVA_HOME \"" + currentDir + "\\jre\"";

            Runtime.getRuntime().exec(setPathCommand);
            Runtime.getRuntime().exec(setJavaHomeCommand);
        } else {
            popupManager.addTextToPanelAndLog("Java detected, no installation needed");
        }
    }

    public void installChromeIfNeeded() throws IOException, InterruptedException {
        if (Files.exists(Path.of(CHROME_X64_PATH)) || Files.exists(Path.of(CHROME_X86_PATH))) {
            popupManager.addTextToPanelAndLog("Chrome detected, no installation needed");
        } else {
            popupManager.addTextToPanelAndLog("Chrome browser missing, installing it");
            Runtime.getRuntime().exec(INSTALL_CHROME_POWERSHELL_COMMAND).waitFor();
        }
    }

    //https://stackoverflow.com/questions/43063303/mac-os-terminal-command-to-set-jar-program-to-run-at-startup
    public void createWindowsStartupScript() {
        popupManager.addTextToPanelAndLog("Now creating startup script");

        List<String> scriptContent = List.of("@ECHO OFF", "cd " + System.getProperty("user.dir") +
                " & start ere-health-launcher.exe");
        Path startupScriptPath = Path.of(System.getenv("APPDATA") +
                "\\Microsoft\\Windows\\Start Menu\\Programs\\Startup\\ere-health-launcher.bat");
        try {
            Files.createFile(startupScriptPath);
            Files.write(startupScriptPath, scriptContent, StandardCharsets.UTF_8);
        } catch (IOException e) {
            log.log(System.Logger.Level.ERROR, "Could not create startup script:");
            e.printStackTrace();
        }
    }
}