package health.ere.ps.model.pdf;

import health.ere.ps.model.gematik.BundleWithAccessCodeOrThrowable;

import java.util.List;

public class ERezeptDocument {
    public final List<BundleWithAccessCodeOrThrowable> bundleWithAccessCodeOrThrowables;
    public final byte[] pdfDocument;

    public ERezeptDocument(List<BundleWithAccessCodeOrThrowable> bundleWithAccessCodeOrThrowables, byte[] pdfDocument) {
        this.bundleWithAccessCodeOrThrowables = bundleWithAccessCodeOrThrowables;
        this.pdfDocument = pdfDocument;
    }
}
