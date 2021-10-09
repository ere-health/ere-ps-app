package health.ere.ps.event;

import java.util.List;

import javax.websocket.Session;

import health.ere.ps.model.pdf.ERezeptDocument;

public class ERezeptDocumentsEvent extends AbstractEvent {
    private final List<ERezeptDocument> eRezeptDocuments;

    public ERezeptDocumentsEvent(List<ERezeptDocument> eRezeptDocuments) {
        this.eRezeptDocuments = eRezeptDocuments;
    }

    public ERezeptDocumentsEvent(List<ERezeptDocument> eRezeptDocuments, Session replyTo, String replyToMessageId) {
        this(eRezeptDocuments);
        this.replyTo = replyTo;
        this.replyToMessageId = replyToMessageId;
    }

    public List<ERezeptDocument> getERezeptWithDocuments() {
        return eRezeptDocuments;
    }
}
