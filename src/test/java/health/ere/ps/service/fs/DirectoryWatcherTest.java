package health.ere.ps.service.fs;

import org.junit.jupiter.api.Test;

class DirectoryWatcherTest {

    @Test
    void testInitAndFoFileEventsOnce() {
        DirectoryWatcher directoryWatcher = new DirectoryWatcher();
        directoryWatcher.dir = "target";
        directoryWatcher.init();
        // This will wait forever if no file activity is done
        // directoryWatcher.checkForFilesEverySecond();
    }

}
