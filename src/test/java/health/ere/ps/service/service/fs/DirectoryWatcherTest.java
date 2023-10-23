package health.ere.ps.service.fs;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.Files;
import java.nio.file.Path;

import javax.inject.Inject;

import org.junit.jupiter.api.Test;

import health.ere.ps.config.AppConfig;
import health.ere.ps.profile.TitusTestProfile;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.junit.TestProfile;

@QuarkusTest
@TestProfile(TitusTestProfile.class)
class DirectoryWatcherTest {

    @Inject
    AppConfig appConfig;

    @Inject
    DirectoryWatcher directoryWatcher;

    @Test
    void testWatcherDirExists() {
        assertTrue(Files.exists(Path.of(appConfig.getDirectoryWatcherDir())));
    }

}
