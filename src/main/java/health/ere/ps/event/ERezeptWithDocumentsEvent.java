package health.ere.ps.event;

import java.util.List;

import jakarta.websocket.Session;

import health.ere.ps.model.pdf.ERezeptDocument;

public class ERezeptWithDocumentsEvent extends AbstractEvent {
    private final List<ERezeptDocument> eRezeptDocuments;

    public ERezeptWithDocumentsEvent(List<ERezeptDocument> eRezeptDocuments) {
        this.eRezeptDocuments = eRezeptDocuments;
    }

    public ERezeptWithDocumentsEvent(List<ERezeptDocument> eRezeptDocuments, Session replyTo, String replyToMessageId) {
        this(eRezeptDocuments);
        this.replyTo = replyTo;
        this.replyToMessageId = replyToMessageId;
    }

    public List<ERezeptDocument> getERezeptWithDocuments() {
        return eRezeptDocuments;
    }
}
