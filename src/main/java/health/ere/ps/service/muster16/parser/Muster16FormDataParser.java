package health.ere.ps.service.muster16.parser;

import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.List;

public class Muster16FormDataParser {
    private String muster16PdfData;
    private String[] muster16PdfDataFields;

    public Muster16FormDataParser(String muster16PdfData) {
        this.muster16PdfData = muster16PdfData;

        if(StringUtils.isNotBlank(muster16PdfData)) {
            muster16PdfDataFields = muster16PdfData.split("\\r?\\n");
        }
    }
    
    public String parseInsuranceCompany() {
        String insuranceCompany = muster16PdfDataFields[0].trim();
        
        return insuranceCompany;
    }

    public String parseInsuranceCompanyId() {
        String insuranceCompanyId = "";

        return insuranceCompanyId;
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
        boolean found =false;

        for(int i = 0; i < muster16PdfDataFields.length; i++) {
            lineElements = muster16PdfDataFields[i].split("\\s+");

            if(lineElements != null) {
                for(int j = 0; j < lineElements.length; j++) {
                    if(lineElements[j].trim().matches("\\d\\d\\.\\d\\d\\.\\d\\d")) {
                        patientDateOfBirth = lineElements[j].trim();
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
    public List<String> parsePrescriptionList() {
        List<String> prescriptionList = new ArrayList<>(1);

        return prescriptionList;
    }

    public String parsePatientInsuranceId() {
        String patientInsuranceId = "";

        return patientInsuranceId;
    }
}
