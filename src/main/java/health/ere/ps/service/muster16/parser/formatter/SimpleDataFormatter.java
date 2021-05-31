package health.ere.ps.service.muster16.parser.formatter;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import health.ere.ps.exception.muster16.parser.formatter.Muster16DataFormatterException;

public class SimpleDataFormatter implements DataFormatter<List<String>> {
    @Override
    public Optional<List<String>> formatData(String muster16PdfData) throws Muster16DataFormatterException {
        Optional<List<String>> formattedData = Optional.empty();

        try {
            String lines[] = muster16PdfData.split("\\r?\\n");

            formattedData = Optional.ofNullable(Arrays.asList(lines));

        } catch(Throwable e) {
            throw new Muster16DataFormatterException("Muster 16 extracted data formatting exception.",
                    e);
        }

        return formattedData;
    }
}
