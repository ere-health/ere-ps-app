package health.ere.ps.model.pdf;

import java.util.ArrayList;
import java.util.List;

import health.ere.ps.model.gematik.BundleWithAccessCodeOrThrowable;

public class ERezeptDocument {
    public List<BundleWithAccessCodeOrThrowable> bundleWithAccessCodeOrThrowables = new ArrayList<>();
    public byte[] pdfDocument;

    public ERezeptDocument(List<BundleWithAccessCodeOrThrowable> bundleWithAccessCodeOrThrowables, byte[] pdfDocument) {
        this.bundleWithAccessCodeOrThrowables = bundleWithAccessCodeOrThrowables;
        this.pdfDocument = pdfDocument;
    }
}
