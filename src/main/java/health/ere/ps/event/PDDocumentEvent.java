package health.ere.ps.event;

import org.apache.pdfbox.pdmodel.PDDocument;

public class PDDocumentEvent {
    
    public PDDocument pDDocument;

    public PDDocumentEvent(PDDocument pDDocument) {
        this.pDDocument = pDDocument;
    }
}
