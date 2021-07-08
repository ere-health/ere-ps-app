package health.ere.ps.model.erixa;

import com.fasterxml.jackson.annotation.JsonFormat;

import java.util.Date;

public class PrescriptionTransferEntry {

    private String firstName;
    private String lastName;
    private String salutation;

    @JsonFormat(pattern = "yyyy-MM-dd")
    private Date birthday;

    private String street;
    private String postcode;
    private String city;
    private String emailAddress;
    private String insuranceType;
    private String healthInsurance;
    private String healthInsuranceNumber;
    private String pzn;
    private boolean autIdem;
    private String dosage;
    private String medicineDescription;
    private boolean extraPaymentNecessary;

    @JsonFormat(pattern = "yyyy-MM-dd'T'hh:mm:ss'Z'")
    private Date creationDateTime;

    @JsonFormat(pattern = "yyyy-MM-dd")
    private Date surgeryDate;

    public PrescriptionTransferEntry() {
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getSalutation() {
        return salutation;
    }

    public void setSalutation(String salutation) {
        this.salutation = salutation;
    }

    public Date getBirthday() {
        return birthday;
    }

    public void setBirthday(Date birthday) {
        this.birthday = birthday;
    }

    public String getStreet() {
        return street;
    }

    public void setStreet(String street) {
        this.street = street;
    }

    public String getPostcode() {
        return postcode;
    }

    public void setPostcode(String postcode) {
        this.postcode = postcode;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getEmailAddress() {
        return emailAddress;
    }

    public void setEmailAddress(String emailAddress) {
        this.emailAddress = emailAddress;
    }

    public String getInsuranceType() {
        return insuranceType;
    }

    public void setInsuranceType(String insuranceType) {
        this.insuranceType = insuranceType;
    }

    public String getHealthInsurance() {
        return healthInsurance;
    }

    public void setHealthInsurance(String healthInsurance) {
        this.healthInsurance = healthInsurance;
    }

    public String getHealthInsuranceNumber() {
        return healthInsuranceNumber;
    }

    public void setHealthInsuranceNumber(String healthInsuranceNumber) {
        this.healthInsuranceNumber = healthInsuranceNumber;
    }
    public String getPzn() {
        return pzn;
    }

    public void setPzn(String pzn) {
        this.pzn = pzn;
    }

    public boolean isAutIdem() {
        return autIdem;
    }

    public void setAutIdem(boolean autIdem) {
        this.autIdem = autIdem;
    }

    public String getDosage() {
        return dosage;
    }

    public void setDosage(String dosage) {
        this.dosage = dosage;
    }

    public String getMedicineDescription() {
        return medicineDescription;
    }

    public void setMedicineDescription(String medicineDescription) {
        this.medicineDescription = medicineDescription;
    }

    public boolean isExtraPaymentNecessary() {
        return extraPaymentNecessary;
    }

    public void setExtraPaymentNecessary(boolean extraPaymentNecessary) {
        this.extraPaymentNecessary = extraPaymentNecessary;
    }

    public Date getCreationDateTime() {
        return creationDateTime;
    }

    public void setCreationDateTime(Date creationDateTime) {
        this.creationDateTime = creationDateTime;
    }

    public Date getSurgeryDate() {
        return surgeryDate;
    }

    public void setSurgeryDate(Date surgeryDate) {
        this.surgeryDate = surgeryDate;
    }
}
