package config;

import launcher.EreHealthApplicationLauncher;
import org.update4j.Configuration;
import org.update4j.FileMetadata;
import updateHandler.CustomUpdateHandler;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;

public class RemoteConfigCreator {

    private static final System.Logger logger = System.getLogger(RemoteConfigCreator.class.getName());

    public static void createRemoteConfigurationFile() throws IOException {
        ApplicationConfig applicationConfig = ApplicationConfig.INSTANCE;
        logger.log(System.Logger.Level.INFO, "Creating new remote configuration file:" +
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

        logger.log(System.Logger.Level.INFO, "Remote configuration file created");
    }
}
