package health.ere.ps.service.muster16.parser.rgxer.provider;

import java.io.InputStream;
import java.net.URISyntaxException;
import java.util.List;

public interface DataProvider<T> {
    List<T> getRecords();

    String getFilePath();

    default InputStream getDataFile() {
        return DataProvider.class.getResourceAsStream(getFilePath());
    }
}
