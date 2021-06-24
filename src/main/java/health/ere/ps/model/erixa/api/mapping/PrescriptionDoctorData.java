package health.ere.ps.model.erixa.api.mapping;

public class PrescriptionDoctorData {

    int userDataId;
    String businessPlaceNumber;
    String doctorNumber;

    public PrescriptionDoctorData() {
    }

    public PrescriptionDoctorData(UserDetails userDetails) {
        userDataId = userDetails.getId();
        businessPlaceNumber = userDetails.getBusinessPlaceNumber();
        doctorNumber = userDetails.getDoctorNumber();
    }

    public int getUserDataId() {
        return userDataId;
    }

    public void setUserDataId(int userDataId) {
        this.userDataId = userDataId;
    }

    public String getBusinessPlaceNumber() {
        return businessPlaceNumber;
    }

    public void setBusinessPlaceNumber(String businessPlaceNumber) {
        this.businessPlaceNumber = businessPlaceNumber;
    }

    public String getDoctorNumber() {
        return doctorNumber;
    }

    public void setDoctorNumber(String doctorNumber) {
        this.doctorNumber = doctorNumber;
    }

}
