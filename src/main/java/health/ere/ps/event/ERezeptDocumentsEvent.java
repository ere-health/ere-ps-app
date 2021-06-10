package health.ere.ps.event;

import java.util.ArrayList;
import java.util.List;

import health.ere.ps.model.pdf.ERezeptDocument;

public class ERezeptDocumentsEvent {
    private final List<ERezeptDocument> eRezeptDocuments = new ArrayList<>();

    public List<ERezeptDocument> getERezeptWithDocuments() {
        return eRezeptDocuments;
    }
}
