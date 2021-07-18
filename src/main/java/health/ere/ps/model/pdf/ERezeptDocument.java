package health.ere.ps.model.pdf;

import java.util.List;

import health.ere.ps.model.gematik.BundleWithAccessCodeOrThrowable;

public class ERezeptDocument {
    private List<BundleWithAccessCodeOrThrowable> bundleWithAccessCodeOrThrowables;
    private byte[] pdfDocument;

    public ERezeptDocument(List<BundleWithAccessCodeOrThrowable> bundleWithAccessCodeOrThrowables, byte[] pdfDocument) {
        this.bundleWithAccessCodeOrThrowables = bundleWithAccessCodeOrThrowables;
        this.pdfDocument = pdfDocument;
    }

    public List<BundleWithAccessCodeOrThrowable> getBundleWithAccessCodeOrThrowables() {
        return this.bundleWithAccessCodeOrThrowables;
    }

    public void setBundleWithAccessCodeOrThrowables(List<BundleWithAccessCodeOrThrowable> bundleWithAccessCodeOrThrowables) {
        this.bundleWithAccessCodeOrThrowables = bundleWithAccessCodeOrThrowables;
    }

    public byte[] getPdfDocument() {
        return this.pdfDocument;
    }

    public void setPdfDocument(byte[] pdfDocument) {
        this.pdfDocument = pdfDocument;
    }
}
