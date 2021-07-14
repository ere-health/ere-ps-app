package health.ere.ps.model.erixa.api.mapping;

public class DoctorUploadToDrugstorePrescriptionModel {

    private String base64File;
    private String drugstoreEmailAddress;
    private int drugstoreSourceType;
    private String fileName;
    private int fileSize;
    private String fileType;
    private PrescriptionData prescriptionData;
    private String drugstoreId;
    private String drugstoreExternalId;

    public String getBase64File() {
        return base64File;
    }

    public void setBase64File(String base64File) {
        this.base64File = base64File;
    }

    public String getDrugstoreEmailAddress() {
        return drugstoreEmailAddress;
    }

    public void setDrugstoreEmailAddress(String drugstoreEmailAddress) {
        this.drugstoreEmailAddress = drugstoreEmailAddress;
    }

    public int getDrugstoreSourceType() {
        return drugstoreSourceType;
    }

    public void setDrugstoreSourceType(int drugstoreSourceType) {
        this.drugstoreSourceType = drugstoreSourceType;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public int getFileSize() {
        return fileSize;
    }

    public void setFileSize(int fileSize) {
        this.fileSize = fileSize;
    }

    public String getFileType() {
        return fileType;
    }

    public void setFileType(String fileType) {
        this.fileType = fileType;
    }

    public PrescriptionData getPrescriptionData() {
        return prescriptionData;
    }

    public void setPrescriptionData(PrescriptionData prescriptionData) {
        this.prescriptionData = prescriptionData;
    }

    public String getDrugstoreId() {
        return drugstoreId;
    }

    public void setDrugstoreId(String drugstoreId) {
        this.drugstoreId = drugstoreId;
    }

    public String getDrugstoreExternalId() {
        return drugstoreExternalId;
    }

    public void setDrugstoreExternalId(String drugstoreExternalId) {
        this.drugstoreExternalId = drugstoreExternalId;
    }
}
