package health.ere.ps.service.fs;

import health.ere.ps.config.AppConfig;
import io.quarkus.test.junit.QuarkusTest;
import org.junit.jupiter.api.Test;

import javax.inject.Inject;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertTrue;

@QuarkusTest
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
