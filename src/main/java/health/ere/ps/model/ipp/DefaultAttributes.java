package health.ere.ps.model.ipp;

import com.hp.jipp.encoding.Attribute;
import com.hp.jipp.model.Operation;
import com.hp.jipp.model.PrinterState;

import java.util.Arrays;

import static com.hp.jipp.model.Types.*;

public class DefaultAttributes {

    public static final int VERSION_NUMBER = 0x100;
    public static final String[] VERSIONS_SUPPORTED = {"1.0"};
    public static final String PRINTER_NAME = "ere-printer";
    public static final String CHARSET = "utf-8";
    public static final String LANGUAGE = "de";
    public static final String LOCALE = "de-DE";
    public static final String DEFAULT_FORMAT = "application/pdf";
    public static final String[] SUPPORTED_FORMATS = {"application/pdf", "application/octet-stream"};

    public static final Attribute<?>[] PRINTER_ATTRIBUTES = {
            printerName.of(PRINTER_NAME),
            printerState.of(PrinterState.idle),
            ippVersionsSupported.of(Arrays.asList(VERSIONS_SUPPORTED)),
            operationsSupported.of(Operation.printJob, Operation.getPrinterAttributes),
            charsetConfigured.of(CHARSET),
            charsetSupported.of(CHARSET),
            naturalLanguageConfigured.of(LOCALE),
            generatedNaturalLanguageSupported.of(LOCALE),
            documentFormatDefault.of(DEFAULT_FORMAT),
            documentFormatSupported.of(Arrays.asList(SUPPORTED_FORMATS)),
    };

    public static final Attribute<?>[] OPERATION_ATTRIBUTES = {
            attributesCharset.of("utf-8"),
            attributesNaturalLanguage.of("en-us")
    };
}