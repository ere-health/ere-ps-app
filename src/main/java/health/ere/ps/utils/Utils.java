package health.ere.ps.utils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Predicate;

public class Utils {

    public static void writeFile(String absolutePath, String content) throws IOException {
        try (FileOutputStream os = new FileOutputStream(absolutePath)) {
            if (content != null) {
                os.write(content.getBytes());
            }
            os.flush();
        }
    }

    public static boolean deleteFiles(File folder, Predicate<File> predicate) {
        AtomicBoolean result = new AtomicBoolean(true);
        Arrays.stream(folder.listFiles())
            .filter(predicate)
            .forEach(file -> result.set(result.get() & file.delete()));
        return result.get();
    }
}
