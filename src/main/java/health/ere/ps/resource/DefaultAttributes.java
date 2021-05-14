package health.ere.ps.resource;

import com.hp.jipp.encoding.Attribute;
import com.hp.jipp.model.Operation;
import com.hp.jipp.model.PrinterState;

import java.util.Arrays;

import static com.hp.jipp.model.Types.*;

public class DefaultAttributes {

    final static int VERSION_NUMBER = 0x100;
    final static String[] VERSIONS_SUPPORTED = {"1.0"};
    final static String PRINTER_NAME = "ere-printer";
    final static String CHARSET = "utf-8";
    final static String LANGUAGE = "de";
    final static String LOCALE = "de-DE";
    final static String DEFAULT_FORMAT = "application/pdf";
    final static String[] SUPPORTED_FORMATS = {"application/pdf", "application/octet-stream"};

    final static Attribute<?>[] PRINTER_ATTRIBUTES = {
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

    final static Attribute<?>[] OPERATION_ATTRIBUTES = {
            attributesCharset.of("utf-8"),
            attributesNaturalLanguage.of("en-us")
    };
}