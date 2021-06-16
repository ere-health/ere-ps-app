package health.ere.ps.service.muster16.parser;

import health.ere.ps.model.muster16.MedicationString;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Muster16FormDataParser implements IMuster16FormParser {
    private final String muster16PdfData;
    private String[] muster16PdfDataFields;

    public Muster16FormDataParser(String muster16PdfData) {
        this.muster16PdfData = muster16PdfData;

        if(StringUtils.isNotBlank(muster16PdfData)) {
            muster16PdfDataFields = muster16PdfData.split("\\r?\\n");
        }
    }

    public String parseInsuranceCompany() {
        String insuranceCompany = "";

        if(isDataFieldPresentAtPosition(muster16PdfDataFields, 0)) {
            insuranceCompany = muster16PdfDataFields[0].trim();
        }

        return insuranceCompany;
    }

    public String parseInsuranceCompanyId() {
        String insuranceCompanyId = "";

        return insuranceCompanyId;
    }

    @Override
    public List<String> parsePatientNamePrefix() {
        return new ArrayList<>();
    }

    public String parsePatientFirstName() {
        String patientFirstName = "";

        return patientFirstName;
    }

    public String parsePatientLastName() {
        String patientLastName = "";

        return patientLastName;
    }

    public String parsePatientStreetName() {
        String patientStreetName = "";

        return patientStreetName;
    }

    public String parsePatientStreetNumber() {
        String patientStreetNumber = "";

        return patientStreetNumber;
    }

    public String parsePatientCity() {
        String patientCity = "";

        return patientCity;
    }

    public String parsePatientZipCode() {
        String patientZipCode = "";

        return patientZipCode;
    }

    public String parsePatientDateOfBirth() {
        String patientDateOfBirth = "";

        String[] lineElements;
        boolean found = false;

        for(int i = 0; isDataFieldPresentAtPosition(muster16PdfDataFields, i) &&
                i < muster16PdfDataFields.length; i++) {
            lineElements = muster16PdfDataFields[i].split("\\s");

            if(lineElements != null) {
                for (String lineElement : lineElements) {
                    if (lineElement.trim().matches("\\d\\d\\.\\d\\d\\.\\d\\d")) {
                        patientDateOfBirth = lineElement.trim();
                        found = true;
                        break;
                    }
                }
            }

            if(found) {
                break;
            }
        }

        return patientDateOfBirth;
    }

    public String parseClinicId() {
        String clinicId = "";

        return clinicId;
    }

    public String parseDoctorId() {
        String doctorId = "";

        return doctorId;
    }

    public String parsePrescriptionDate() {
        String prescriptionDate = "";

        return prescriptionDate;
    }
    public List<MedicationString> parsePrescriptionList() {
        List<MedicationString> prescriptionList = Collections.emptyList();

        return prescriptionList;
    }

    public String parsePatientInsuranceId() {
        String patientInsuranceId = "";

        return patientInsuranceId;
    }

    @Override
    public Boolean parseIsWithPayment() {
        Boolean isWithPayment = false;

        return isWithPayment;
    }
}
