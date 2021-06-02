package health.ere.ps.service.muster16.parser;

import java.util.List;

import health.ere.ps.model.muster16.MedicationString;

public interface IMuster16FormParser {
    String parseInsuranceCompany();

    String parseInsuranceCompanyId();

    String parsePatientFirstName();

    String parsePatientLastName();

    String parsePatientStreetName();

    String parsePatientStreetNumber();

    String parsePatientCity();

    String parsePatientZipCode();

    String parsePatientDateOfBirth();

    String parseClinicId();

    String parseDoctorId();

    String parsePrescriptionDate();

    List<MedicationString> parsePrescriptionList();

    String parsePatientInsuranceId();

    default boolean isDataFieldPresentAtPosition(String[] muster16PdfDataFields,
                                                 int fieldPosition) {
        return muster16PdfDataFields != null && muster16PdfDataFields.length > 0 &&
                fieldPosition >= 0 && muster16PdfDataFields.length > fieldPosition;
    }

    default String getDataFieldAtPosOrDefault(String[] muster16PdfDataFields,
                                              int fieldPosition, String defaultValue) {
        return isDataFieldPresentAtPosition(muster16PdfDataFields, fieldPosition) ?
                muster16PdfDataFields[fieldPosition] : defaultValue;
    }

    default String parsePatientNameTitle() {
        return "";
    }

    default String parsePatientCountryCode() {
        return "";
    }
}
