package health.ere.ps.service.muster16.parser;

import health.ere.ps.model.muster16.MedicationString;

import java.util.List;

public interface IMuster16FormParser {
    String parseInsuranceCompany();
    String parseInsuranceCompanyId();
    List<String> parsePatientNamePrefix();
    String parsePatientFirstName();
    String parsePatientLastName();
    String parsePatientStreetName();
    String parsePatientStreetNumber();
    String parsePatientCity();
    String parsePatientZipCode();
    String parsePatientDateOfBirth();
    String parsePatientStatus();
    String parseClinicId();
    String parseDoctorId() ;
    String parsePrescriptionDate();
    List<MedicationString> parsePrescriptionList();
    String parsePatientInsuranceId();
    Boolean parseIsWithPayment();
    String parsePractitionerFirstName();
    String parsePractitionerLastName();
    String parsePractitionerNamePrefix();
    String parsePractitionerStreetName();
    String parsePractitionerStreetNumber();
    String parsePractitionerCity();
    String parsePractitionerZipCode();
    String parsePractitionerPhoneNumber();
    String parsePractitionerFaxNumber();

    default boolean isDataFieldPresentAtPosition(String[] muster16PdfDataFields,
                                                 int fieldPosition) {
        return muster16PdfDataFields != null && fieldPosition >= 0 && muster16PdfDataFields.length > fieldPosition;
    }

    default String getDataFieldAtPosOrDefault(String[] muster16PdfDataFields,
                                              int fieldPosition, String defaultValue) {
        return isDataFieldPresentAtPosition(muster16PdfDataFields, fieldPosition)?
                muster16PdfDataFields[fieldPosition] : defaultValue;
    }
    String parsePractitionerQualification();


}
