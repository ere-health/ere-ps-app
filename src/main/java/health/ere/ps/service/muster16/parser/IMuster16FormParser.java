package health.ere.ps.service.muster16.parser;

import java.util.List;

import health.ere.ps.model.muster16.MedicationString;

public interface IMuster16FormParser {
    public String parseInsuranceCompany();
    public String parseInsuranceCompanyId();
    public String parsePatientNamePrefix();
    public String parsePatientFirstName();
    public String parsePatientLastName();
    public String parsePatientStreetName();
    public String parsePatientStreetNumber();
    public String parsePatientCity();
    public String parsePatientZipCode();
    public String parsePatientDateOfBirth();
    public String parseClinicId();
    public String parseDoctorId() ;
    public String parsePrescriptionDate();
    public List<MedicationString> parsePrescriptionList();
    public String parsePatientInsuranceId();

    default boolean isDataFieldPresentAtPosition(String[] muster16PdfDataFields,
                                                 int fieldPosition) {
        return muster16PdfDataFields != null && muster16PdfDataFields.length > 0 &&
                fieldPosition >= 0 && muster16PdfDataFields.length > fieldPosition;
    }

    default String getDataFieldAtPosOrDefault(String[] muster16PdfDataFields,
                                              int fieldPosition, String defaultValue) {
        return isDataFieldPresentAtPosition(muster16PdfDataFields, fieldPosition)?
                muster16PdfDataFields[fieldPosition] : defaultValue;
    }
}
