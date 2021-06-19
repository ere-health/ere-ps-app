package health.ere.ps.event;

import health.ere.ps.model.pdf.ERezeptDocument;

import java.util.List;

public class ERezeptDocumentsEvent {
    private final List<ERezeptDocument> eRezeptDocuments;

    public ERezeptDocumentsEvent(List<ERezeptDocument> eRezeptDocuments) {
        this.eRezeptDocuments = eRezeptDocuments;
    }

    public List<ERezeptDocument> getERezeptWithDocuments() {
        return eRezeptDocuments;
    }
}
