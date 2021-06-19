package health.ere.ps.event;

import org.apache.pdfbox.pdmodel.PDDocument;

public final class PDDocumentEvent {
    private final PDDocument pDDocument;

    public PDDocumentEvent(PDDocument pDDocument) {
        this.pDDocument = pDDocument;
    }

    public PDDocument getPDDocument() {
        return pDDocument;
    }
}
