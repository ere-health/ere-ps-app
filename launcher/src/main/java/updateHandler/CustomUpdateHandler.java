package updateHandler;

import config.ApplicationConfig;
import me.tongfei.progressbar.DelegatingProgressBarConsumer;
import me.tongfei.progressbar.ProgressBar;
import me.tongfei.progressbar.ProgressBarBuilder;
import me.tongfei.progressbar.ProgressBarStyle;
import org.update4j.FileMetadata;
import org.update4j.service.UpdateHandler;
import popup.PopupManager;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;


/**
 * Custom update handler that overrides some UpdateHandler methods to allow for our own logic.
 * Logs what is going on with the download process, extracts the downloaded archive and sets up + feeds the
 * progress bar
 */
public class CustomUpdateHandler implements UpdateHandler {

    private static final System.Logger log = System.getLogger(CustomUpdateHandler.class.getName());
    private final ApplicationConfig applicationConfig = ApplicationConfig.INSTANCE;
    private final PopupManager popupManager = PopupManager.INSTANCE;

    private final ProgressBar pb = new ProgressBarBuilder()
            .setInitialMax(100)
            .setTaskName("Download")
            .setConsumer(new DelegatingProgressBarConsumer(number -> log.log(System.Logger.Level.INFO, number)))
            .setConsumer(new DelegatingProgressBarConsumer(popupManager::updateProgressBar))
            .setStyle(ProgressBarStyle.ASCII)
            .setUpdateIntervalMillis(100)
            .build();


    @Override
    public void doneCheckUpdateFile(FileMetadata file, boolean requires) {
        String message;
        if (requires) {
            message = "The file: " + file.getPath() + " needs to be downloaded, starting download";
            popupManager.startProgressBar();
        } else {
            message = "The file: " + file.getPath() + " is up-to-date, no need to download anything";
        }
        popupManager.addTextToPanelAndLog(message);
    }

    @Override
    public void doneDownloads() {
        popupManager.addTextToPanelAndLog("Download done, extraction of archive in progress");
        popupManager.closeProgressBar();
        pb.close();

        try {
            extractArchiveToApplicationFolder();
        } catch (IOException e) {
            log.log(System.Logger.Level.ERROR, "There was an error during the extraction of the archive:");
            e.printStackTrace();
        }
    }

    @Override
    public void failed(Throwable t) {
        log.log(System.Logger.Level.ERROR, "Update failed because:" + t.getLocalizedMessage());
        popupManager.addTextToPanelAndLog("There was en ERROR during the update:" + t.getLocalizedMessage());
    }

    @Override
    public void updateDownloadFileProgress(FileMetadata file, float progress) {
        pb.stepTo((long) (progress * 100));
    }


    private void extractArchiveToApplicationFolder() throws IOException {
        String archive = applicationConfig.getApplicationPath() + "/" + applicationConfig.getArchiveName();
        File destDir = new File(applicationConfig.getApplicationPath());

        byte[] buffer = new byte[1024];
        ZipInputStream zis = new ZipInputStream(new FileInputStream(archive));
        ZipEntry zipEntry = zis.getNextEntry();

        while (zipEntry != null) {
            File newFile = newFile(destDir, zipEntry);

            if (zipEntry.isDirectory()) {
                if (!newFile.isDirectory() && !newFile.mkdirs()) {
                    throw new IOException("Failed to create directory " + newFile);
                }
            } else {
                // fix for Windows-created archives
                File parent = newFile.getParentFile();
                if (!parent.isDirectory() && !parent.mkdirs()) {
                    throw new IOException("Failed to create directory " + parent);
                }

                // write file content
                FileOutputStream fos = new FileOutputStream(newFile);
                int len;
                while ((len = zis.read(buffer)) > 0) {
                    fos.write(buffer, 0, len);
                }
                fos.close();
            }
            zipEntry = zis.getNextEntry();
        }
        zis.closeEntry();
        zis.close();
    }

    private File newFile(File destinationDir, ZipEntry zipEntry) throws IOException {
        File destFile = new File(destinationDir, zipEntry.getName());

        String destDirPath = destinationDir.getCanonicalPath();
        String destFilePath = destFile.getCanonicalPath();

        if (!destFilePath.startsWith(destDirPath + File.separator)) {
            throw new IOException("Entry is outside of the target dir: " + zipEntry.getName());
        }
        return destFile;
    }
}
