package health.ere.ps.service.fs;

import java.nio.file.ClosedWatchServiceException;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.WatchEvent;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Event;
import javax.inject.Inject;

import static java.nio.file.StandardWatchEventKinds.*;

import java.io.File;
import java.io.IOException;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.eclipse.microprofile.config.inject.ConfigProperty;

import io.quarkus.runtime.Startup;
import io.quarkus.scheduler.Scheduled;

/**
 * Watches a directory and if PDF files are placed their they will be thrown as
 * a PDDocument event.
 */
@ApplicationScoped
@Startup
public class DirectoryWatcher {

    @Inject
    Event<PDDocument> pdDocumentEvent;

    private static Logger log = Logger.getLogger(DirectoryWatcher.class.getName());

    @ConfigProperty(name = "directory-watcher.dir", defaultValue = "!")
    String dir;

    private WatchService watcher = null;
    private Map<WatchKey, Path> keys = new HashMap<WatchKey, Path>();

    Path watchPath;

    @PostConstruct
    public void init() {
        if (dir == null || dir.equals("") || dir.equals("!")) {
            log.info("Not watching any directory");
            return;
        }
        try {
            watcher = FileSystems.getDefault().newWatchService();
            watchPath = Paths.get(dir);
            WatchKey key = watchPath.register(watcher, ENTRY_CREATE);
            keys.put(key, watchPath);
        } catch (IOException e) {
            log.log(Level.SEVERE, "Could not start directory watcher", e);
        }
    }

    @Scheduled(every = "1s")
    public void checkForFilesEverySecond() {
        WatchKey key = null;
        try {
            key = watcher.take();
        } catch (InterruptedException | ClosedWatchServiceException e) {
            log.log(Level.SEVERE, "Could not start directory watcher", e);
        }
        List<WatchEvent<?>> keys = key.pollEvents();
        for (WatchEvent<?> watchEvent : keys) {
            WatchEvent.Kind<?> watchEventKind = watchEvent.kind();

            if (watchEventKind == ENTRY_CREATE) {
                Path filePath = ((Path) watchEvent.context());
                log.info("Processing file: " + filePath);
                try {
                    pdDocumentEvent.fireAsync(PDDocument
                            .load(new File(watchPath.toFile().getAbsolutePath() + "/" + filePath.getFileName())));
                } catch (IOException e) {
                    log.log(Level.SEVERE, "Could not parse PDF", e);
                }
            }
            key.reset();
        }
    }
}
