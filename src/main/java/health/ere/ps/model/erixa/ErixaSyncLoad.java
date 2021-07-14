package health.ere.ps.model.erixa;

@Deprecated
public class ErixaSyncLoad {

    private String document;
    private String patient;

    public ErixaSyncLoad() {
    }

    public ErixaSyncLoad(String document, String patient) {
        this.document = document;
        this.patient = patient;
    }

    public void setDocument(String document) {
        this.document = document;
    }

    public void setPatient(String patient) {
        this.patient = patient;
    }

    public String getDocument() {
        return document;
    }

    public String getPatient() {
        return patient;
    }
}
