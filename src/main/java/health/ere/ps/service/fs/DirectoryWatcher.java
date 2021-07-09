package health.ere.ps.service.fs;

import static java.nio.file.StandardWatchEventKinds.ENTRY_CREATE;

import java.io.File;
import java.io.IOException;
import java.nio.file.ClosedWatchServiceException;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.WatchEvent;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Event;
import javax.inject.Inject;

import org.apache.commons.lang3.StringUtils;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.eclipse.microprofile.config.inject.ConfigProperty;

import health.ere.ps.event.PDDocumentEvent;
import io.quarkus.runtime.Startup;
import io.quarkus.scheduler.Scheduled;

/**
 * Watches a directory and if PDF files are placed their they will be thrown as
 * a PDDocument event.
 */
@ApplicationScoped
@Startup
public class DirectoryWatcher {

    private static final Logger log = Logger.getLogger(DirectoryWatcher.class.getName());

    private static final String PROCESSED_DIRECTORY_NAME = "processed";
    private static final String FAILED_DIRECTORY_NAME = "failed";
    private static final List<String> SUB_DIRECTORIES = List.of(PROCESSED_DIRECTORY_NAME, FAILED_DIRECTORY_NAME);

    @Inject
    Event<PDDocumentEvent> pdDocumentEvent;

    @ConfigProperty(name = "directory-watcher.dir", defaultValue = "watch-pdf")
    String dir;

    private WatchService watcher = null;
    private Path watchPath;

    @PostConstruct
    public void init() {
        if (StringUtils.isEmpty(dir)) {
            log.info("Not watching any directory");
            return;
        }
        log.info("Watching directory: " + dir);

        try {
            watcher = FileSystems.getDefault().newWatchService();
            watchPath = Paths.get(dir);
            File watchPathFile = watchPath.toFile();

            if (!watchPathFile.exists()) {
                log.info("Creating directory for watching pdf muster 16 forms: " + watchPathFile);
                watchPathFile.mkdirs();
            }

            for (String subDirectory : SUB_DIRECTORIES) {
                Path subDirectoryPath = Path.of(dir + File.separator + subDirectory);
                if (Files.notExists(subDirectoryPath)) {
                    Files.createDirectory(subDirectoryPath);
                }
            }

            watchPath.register(watcher, ENTRY_CREATE);
        } catch (IOException e) {
            log.log(Level.SEVERE, "Could not start directory watcher", e);
        }
    }

    @Scheduled(every = "1s")
    public void checkForFilesEverySecond() {
        WatchKey key = null;
        try {
            key = watcher.poll();
            if (key == null) {
                return;
            }
        } catch (ClosedWatchServiceException e) {
            log.log(Level.SEVERE, "Could not start directory watcher", e);
        }

        List<WatchEvent<?>> keys = key.pollEvents();
        for (WatchEvent<?> watchEvent : keys) {
            WatchEvent.Kind<?> watchEventKind = watchEvent.kind();

            if (watchEventKind == ENTRY_CREATE) {
                Path filePath = ((Path) watchEvent.context());
                log.info("Processing file: " + filePath);
                File pdfFile = new File(watchPath.toFile().getAbsolutePath() + File.separator +
                        filePath.getFileName());
                try {
                    pdDocumentEvent.fireAsync(new PDDocumentEvent(PDDocument.load(pdfFile)));
                    storePdfFile(filePath, true);
                } catch (IOException e) {
                    log.log(Level.SEVERE, "Could not parse PDF", e);
                    storePdfFile(filePath, false);
                }
            }
            key.reset();
        }
    }

    private void storePdfFile(Path filePath, boolean wasParsingSuccessful) {
        String newFilename = new SimpleDateFormat("yyyy-MM-dd_kk:mm:ss")
                .format(new Date()) + "__" + filePath.getFileName();
        Path destinationFolder = Path.of(wasParsingSuccessful ? PROCESSED_DIRECTORY_NAME : FAILED_DIRECTORY_NAME);

        Path currentPath = Path.of(watchPath.toFile().getAbsolutePath() + File.separator +
                filePath.getFileName());
        Path newPath = Path.of(watchPath.toFile().getAbsolutePath() + File.separator +
                destinationFolder + File.separator + newFilename);
        try {
            Files.move(currentPath, newPath);
        } catch (IOException e) {
            log.severe("There was a problem when moving processed pdf file:" + filePath.getFileName());
            e.printStackTrace();
        }
    }
}
