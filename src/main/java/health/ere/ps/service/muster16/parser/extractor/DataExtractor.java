package health.ere.ps.service.muster16.parser.extractor;

import java.io.InputStream;
import java.util.Optional;

import health.ere.ps.exception.muster16.parser.extractor.Muster16DataExtractorException;

public interface DataExtractor<T> {
    Optional<T> extractData(InputStream muster16PdfInputStream) throws Muster16DataExtractorException;
}
