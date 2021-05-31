package health.ere.ps.service.muster16.parser.formatter;

import java.util.Optional;

import health.ere.ps.exception.muster16.parser.formatter.Muster16DataFormatterException;

public interface DataFormatter<T> {
    Optional<T> formatData(String muster16PdfData) throws Muster16DataFormatterException;
}
