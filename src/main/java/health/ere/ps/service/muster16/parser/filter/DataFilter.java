package health.ere.ps.service.muster16.parser.filter;

public interface DataFilter<T> {
    T filter(String muster16PdfData);
}
