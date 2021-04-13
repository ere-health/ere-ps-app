package com.erehealth.ps.service.muster16.parser;

import java.util.ArrayList;
import java.util.List;

public class Muster16FormDataParser {
    private String muster16PdfData;

    public Muster16FormDataParser(String muster16PdfData) {
        this.muster16PdfData = muster16PdfData;
    }
    
    public String parseInsuranceCompany() {
        String insuranceCompany = "";
        
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
