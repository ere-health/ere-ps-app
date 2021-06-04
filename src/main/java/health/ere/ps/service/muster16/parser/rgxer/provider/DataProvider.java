package health.ere.ps.service.muster16.parser.rgxer.provider;

import ca.uhn.fhir.rest.server.exceptions.ResourceNotFoundException;

import java.io.File;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.List;

public interface DataProvider<T> {
    List<T> getRecords();

    String getFilePath();

    default File getDataFile() throws URISyntaxException {
        String resourcePath = getFilePath();
        URL resource = getClass().getClassLoader().getResource(resourcePath);
        if (resource == null)
            throw new ResourceNotFoundException(resourcePath);
        else
            return new File(resource.toURI());

    }
}
