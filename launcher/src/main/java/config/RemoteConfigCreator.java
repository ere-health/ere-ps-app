package config;

import launcher.EreHealthApplicationLauncher;
import org.update4j.Configuration;
import org.update4j.FileMetadata;
import updateHandler.CustomUpdateHandler;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;

/**
 * Creates the remote configuration associated with an installer archive that will be read by the launcher
 * to check if the local archive matches the remote one
 */
public class RemoteConfigCreator {

    private static final System.Logger log = System.getLogger(RemoteConfigCreator.class.getName());

    public static void createRemoteConfigurationFile() throws IOException {
        ApplicationConfig applicationConfig = ApplicationConfig.INSTANCE;
        log.log(System.Logger.Level.INFO, "Creating new remote configuration file:" +
                applicationConfig.getRemoteConfigurationFilename() + " in folder:" +
                applicationConfig.getRemoteConfigurationCreationFolder() + " from archive:" +
                applicationConfig.getRemoteConfigurationCreationFolder() + "/" + applicationConfig.getArchiveName());

        Configuration remoteConfiguration = Configuration.builder()
                .launcher(EreHealthApplicationLauncher.class)
                .updateHandler(CustomUpdateHandler.class)
                .basePath(System.getProperty("user.dir") + "/" + applicationConfig.getApplicationPath())
                .baseUri(applicationConfig.getRemoteServerUri() + "/")
                .file(FileMetadata
                        .readFrom(applicationConfig.getRemoteConfigurationCreationFolder() + "/" +
                                applicationConfig.getArchiveName())
                        .classpath() //to avoid a warning in the logs
                        .uri(applicationConfig.getRemoteServerUri() + "/" + applicationConfig.getArchiveName())
                )
                .build();

        String remoteConfigurationFilePath = applicationConfig.getRemoteConfigurationCreationFolder() +
                "/" + applicationConfig.getRemoteConfigurationFilename();
        File remoteConfigurationFile = new File(remoteConfigurationFilePath);

        if (Files.notExists(remoteConfigurationFile.toPath())) {
            Files.createFile(remoteConfigurationFile.toPath());
        }

        FileWriter fileWriter = new FileWriter(remoteConfigurationFile);
        remoteConfiguration.write(fileWriter);
        fileWriter.close();

        log.log(System.Logger.Level.INFO, "Remote configuration file created");
    }
}
