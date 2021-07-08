package health.ere.ps.model.muster16;

import java.util.List;

public class Muster16PrescriptionForm {

    private String insuranceCompany;
    private String insuranceCompanyId;
    private List<String> PatientNamePrefix;
    private String patientFirstName;
    private String patientLastName;
    private String patientStreetName;
    private String patientStreetNumber;
    private String patientCity;
    private String patientZipCode;
    private String patientInsuranceId;
    private String patientDateOfBirth;
    private String patientStatus;
    private String clinicId;
    private String practitionerId;
    private String prescriptionDate;
    private String practitionerFirstName;
    private String practitionerLastName;
    private String practitionerStreetName;
    private String practitionerStreetNumber;
    private String practitionerCity;
    private String practitionerZipCode;
    private String practitionerNamePrefix;
    private String practitionerPhone;
    private String practitionerFax;
    private Boolean isWithPayment;

    private List<MedicationString> prescriptionList;

    public Muster16PrescriptionForm(String insuranceCompany, String insuranceCompanyId, List<String> patientNamePrefix,
                                    String patientFirstName, String patientLastName, String patientStreetName,
                                    String patientStreetNumber, String patientCity, String patientZipCode,
                                    String patientInsuranceId, String patientDateOfBirth, String patientStatus,
                                    String clinicId, String practitionerId, String prescriptionDate,
                                    String practitionerFirstName, String practitionerLastName, String practitionerStreetName,
                                    String practitionerStreetNumber, String practitionerCity, String practitionerZipCode,
                                    String practitionerNamePrefix, String practitionerPhone, String practitionerFax,
                                    Boolean isWithPayment, List<MedicationString> prescriptionList) {
        this.insuranceCompany = insuranceCompany;
        this.insuranceCompanyId = insuranceCompanyId;
        this.PatientNamePrefix = patientNamePrefix;
        this.patientFirstName = patientFirstName;
        this.patientLastName = patientLastName;
        this.patientStreetName = patientStreetName;
        this.patientStreetNumber = patientStreetNumber;
        this.patientCity = patientCity;
        this.patientZipCode = patientZipCode;
        this.patientInsuranceId = patientInsuranceId;
        this.patientDateOfBirth = patientDateOfBirth;
        this.patientStatus = patientStatus;
        this.clinicId = clinicId;
        this.practitionerId = practitionerId;
        this.prescriptionDate = prescriptionDate;
        this.practitionerFirstName = practitionerFirstName;
        this.practitionerLastName = practitionerLastName;
        this.practitionerStreetName = practitionerStreetName;
        this.practitionerStreetNumber = practitionerStreetNumber;
        this.practitionerCity = practitionerCity;
        this.practitionerZipCode = practitionerZipCode;
        this.practitionerNamePrefix = practitionerNamePrefix;
        this.practitionerPhone = practitionerPhone;
        this.practitionerFax = practitionerFax;
        this.isWithPayment = isWithPayment;
        this.prescriptionList = prescriptionList;
    }

    public Muster16PrescriptionForm() {

    }

    public String getInsuranceCompany() {
        return insuranceCompany;
    }

    public void setInsuranceCompany(String insuranceCompany) {
        this.insuranceCompany = insuranceCompany;
    }

    public String getInsuranceCompanyId() {
        return insuranceCompanyId;
    }

    public void setInsuranceCompanyId(String insuranceCompanyId) {
        this.insuranceCompanyId = insuranceCompanyId;
    }

    public List<String> getPatientNamePrefix() {
        return PatientNamePrefix;
    }

    public void setPatientNamePrefix(List<String> patientNamePrefix) {
        PatientNamePrefix = patientNamePrefix;
    }

    public String getPatientFirstName() {
        return patientFirstName;
    }

    public void setPatientFirstName(String patientFirstName) {
        this.patientFirstName = patientFirstName;
    }

    public String getPatientLastName() {
        return patientLastName;
    }

    public void setPatientLastName(String patientLastName) {
        this.patientLastName = patientLastName;
    }

    public String getPatientStreetName() {
        return patientStreetName;
    }

    public void setPatientStreetName(String patientStreetName) {
        this.patientStreetName = patientStreetName;
    }

    public String getPatientStreetNumber() {
        return patientStreetNumber;
    }

    public void setPatientStreetNumber(String patientStreetNumber) {
        this.patientStreetNumber = patientStreetNumber;
    }

    public String getPatientCity() {
        return patientCity;
    }

    public void setPatientCity(String patientCity) {
        this.patientCity = patientCity;
    }

    public String getPatientZipCode() {
        return patientZipCode;
    }

    public void setPatientZipCode(String patientZipCode) {
        this.patientZipCode = patientZipCode;
    }

    public String getPatientInsuranceId() {
        return patientInsuranceId;
    }

    public void setPatientInsuranceId(String patientInsuranceId) {
        this.patientInsuranceId = patientInsuranceId;
    }

    public String getPatientDateOfBirth() {
        return patientDateOfBirth;
    }

    public void setPatientDateOfBirth(String patientDateOfBirth) {
        this.patientDateOfBirth = patientDateOfBirth;
    }

    public String getPatientStatus() {
        return patientStatus;
    }

    public void setPatientStatus(String patientStatus) {
        this.patientStatus = patientStatus;
    }

    public String getClinicId() {
        return clinicId;
    }

    public void setClinicId(String clinicId) {
        this.clinicId = clinicId;
    }

    public String getPractitionerId() {
        return practitionerId;
    }

    public void setPractitionerId(String practitionerId) {
        this.practitionerId = practitionerId;
    }

    public String getPrescriptionDate() {
        return prescriptionDate;
    }

    public void setPrescriptionDate(String prescriptionDate) {
        this.prescriptionDate = prescriptionDate;
    }

    public String getPractitionerFirstName() {
        return practitionerFirstName;
    }

    public void setPractitionerFirstName(String practitionerFirstName) {
        this.practitionerFirstName = practitionerFirstName;
    }

    public String getPractitionerLastName() {
        return practitionerLastName;
    }

    public void setPractitionerLastName(String practitionerLastName) {
        this.practitionerLastName = practitionerLastName;
    }

    public String getPractitionerStreetName() {
        return practitionerStreetName;
    }

    public void setPractitionerStreetName(String practitionerStreetName) {
        this.practitionerStreetName = practitionerStreetName;
    }

    public String getPractitionerStreetNumber() {
        return practitionerStreetNumber;
    }

    public void setPractitionerStreetNumber(String practitionerStreetNumber) {
        this.practitionerStreetNumber = practitionerStreetNumber;
    }

    public String getPractitionerCity() {
        return practitionerCity;
    }

    public void setPractitionerCity(String practitionerCity) {
        this.practitionerCity = practitionerCity;
    }

    public String getPractitionerZipCode() {
        return practitionerZipCode;
    }

    public void setPractitionerZipCode(String practitionerZipCode) {
        this.practitionerZipCode = practitionerZipCode;
    }

    public String getPractitionerNamePrefix() {
        return practitionerNamePrefix;
    }

    public void setPractitionerNamePrefix(String practitionerNamePrefix) {
        this.practitionerNamePrefix = practitionerNamePrefix;
    }

    public String getPractitionerPhone() {
        return practitionerPhone;
    }

    public void setPractitionerPhone(String practitionerPhone) {
        this.practitionerPhone = practitionerPhone;
    }

    public String getPractitionerFax() {
        return practitionerFax;
    }

    public void setPractitionerFax(String practitionerFax) {
        this.practitionerFax = practitionerFax;
    }

    public Boolean getWithPayment() {
        return isWithPayment;
    }

    public void setWithPayment(Boolean withPayment) {
        isWithPayment = withPayment;
    }

    public List<MedicationString> getPrescriptionList() {
        return prescriptionList;
    }

    public void setPrescriptionList(List<MedicationString> prescriptionList) {
        this.prescriptionList = prescriptionList;
    }

    @Override
    public String toString() {
        return "Muster16PrescriptionForm{" +
                "insuranceCompany='" + insuranceCompany + '\'' +
                ", insuranceCompanyId='" + insuranceCompanyId + '\'' +
                ", PatientNamePrefix=" + PatientNamePrefix +
                ", patientFirstName='" + patientFirstName + '\'' +
                ", patientLastName='" + patientLastName + '\'' +
                ", patientStreetName='" + patientStreetName + '\'' +
                ", patientStreetNumber='" + patientStreetNumber + '\'' +
                ", patientCity='" + patientCity + '\'' +
                ", patientZipCode='" + patientZipCode + '\'' +
                ", patientInsuranceId='" + patientInsuranceId + '\'' +
                ", patientDateOfBirth='" + patientDateOfBirth + '\'' +
                ", patientStatus='" + patientStatus + '\'' +
                ", clinicId='" + clinicId + '\'' +
                ", practitionerId='" + practitionerId + '\'' +
                ", prescriptionDate='" + prescriptionDate + '\'' +
                ", practitionerFirstName='" + practitionerFirstName + '\'' +
                ", practitionerLastName='" + practitionerLastName + '\'' +
                ", practitionerStreetName='" + practitionerStreetName + '\'' +
                ", practitionerStreetNumber='" + practitionerStreetNumber + '\'' +
                ", practitionerCity='" + practitionerCity + '\'' +
                ", practitionerZipCode='" + practitionerZipCode + '\'' +
                ", practitionerNamePrefix='" + practitionerNamePrefix + '\'' +
                ", practitionerPhone='" + practitionerPhone + '\'' +
                ", practitionerFax='" + practitionerFax + '\'' +
                ", isWithPayment=" + isWithPayment +
                ", prescriptionList=" + prescriptionList +
                '}';
    }
}
