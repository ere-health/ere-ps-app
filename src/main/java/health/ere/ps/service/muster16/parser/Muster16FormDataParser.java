package health.ere.ps.service.muster16.parser;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.ListUtils;

import health.ere.ps.exception.muster16.parser.Muster16DataParserException;
import health.ere.ps.exception.muster16.parser.extractor.Muster16DataExtractorException;
import health.ere.ps.exception.muster16.parser.formatter.Muster16DataFormatterException;
import health.ere.ps.model.muster16.MedicationString;
import health.ere.ps.service.muster16.parser.extractor.DataExtractor;
import health.ere.ps.service.muster16.parser.filter.DataFilter;
import health.ere.ps.service.muster16.parser.formatter.DataFormatter;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class Muster16FormDataParser implements IMuster16FormParser {
    protected static final int TARGET_PARSED_FIELD_SLOTS = 14;
    protected String muster16PdfData;
    protected List<String> formattedMuster16Data;
    protected InputStream muster16PdfInputStream;
    protected DataExtractor<String> dataExtractor;
    protected DataFilter<String> dataFilter;
    protected DataFormatter<List<String>> dataFormatter;
    protected List<Map<Integer, String>> formDates = Collections.emptyList();
    protected Map<Muster16FormField, Map<Integer, String>> parsedFieldsCache =
            new HashMap<>(TARGET_PARSED_FIELD_SLOTS);

    protected enum Muster16FormField {
        INSURANCE_COMPANY,
        INSURANCE_COMPANY_ID,
        PATIENT_FIRST_NAME,
        PATIENT_LAST_NAME,
        PATIENT_STREET_NAME,
        PATIENT_STREET_NUMBER,
        PATIENT_CITY,
        PATIENT_ZIP_CODE,
        PATIENT_DATE_OF_BIRTH,
        CLINIC_ID,
        DOCTOR_ID,
        PRESCRIPTION_DATE,
        PRESCRIPTION_LIST,
        PATIENT_INSURANCE_ID,
    }

    /**
     * Constructor.
     *
     * You must call the {@link #configureParser()} method directly after instantiating a
     * {@link Muster16FormDataParser} object using this constructor and then call the
     * {@link #setMuster16PdfInputStream(InputStream)}, {@link #setDataExtractor(DataExtractor)},
     * and, {@link #setDataFormatter(DataFormatter)} methods. Calling the
     * {@link #setDataFilter(DataFilter)} method is optional. You can then call the parseXXX
     * methods.
     */
    public Muster16FormDataParser() {

    }

    /**
     * Constructor.
     *
     * You must call the {@link #configureParser()} method directly after instantiating a
     * {@link Muster16FormDataParser} object using this constructor. You can then call the
     * parseXXX methods.
     *
     * @param muster16PdfInputStream input stream of the source Muster 16 PDF.
     * @param dataExtractor extracts data from the source Muster 16 PDF.
     * @param dataFilter filters the extracted data from the source Muster 16 PDF. This is an
     *                   optional parameter.
     * @param dataFormatter formats the extracted and/or filtered Muster 16 PDF data.
     */
    public Muster16FormDataParser(InputStream muster16PdfInputStream,
                                  DataExtractor<String> dataExtractor,
                                  DataFilter<String> dataFilter,
                                  DataFormatter<List<String>> dataFormatter) {
        this.setMuster16PdfInputStream(muster16PdfInputStream);
        this.setDataExtractor(dataExtractor);
        this.setDataFilter(dataFilter);
        this.setDataFormatter(dataFormatter);
    }

    /**
     * This method must be called directly after instantiating a {@link Muster16FormDataParser}
     * object and setting its required data items via is constructor or setter methods.
     */
    public Muster16FormDataParser configureParser() throws Muster16DataParserException,
            Muster16DataExtractorException, Muster16DataFormatterException {
        if(getDataExtractor() != null) {
            Optional<String> extractedData = getDataExtractor().extractData(getMuster16PdfInputStream());

            if(extractedData.isPresent()) {
                muster16PdfData = extractedData.get();

                if(getDataFilter() != null) {
                    muster16PdfData = getDataFilter().filter(muster16PdfData);
                }

                if(getDataFormatter() != null) {
                    Optional<List<String>> formattedData =
                            getDataFormatter().formatData(muster16PdfData);

                    formattedMuster16Data = formattedData.isPresent()? formattedData.get() :
                            Collections.emptyList();
                    if(CollectionUtils.isEmpty(formattedMuster16Data)) {
                        throw new Muster16DataFormatterException(
                                "No formatted Muster 16 data is present.");
                    }
                } else {
                    throw new Muster16DataFormatterException("Muster 16 data formatter object is " +
                            "missing.");
                }
            }
        } else {
            throw new Muster16DataExtractorException("Muster 16 data extractor object is missing.");
        }

        int minLinesToParse = 7;

        if(CollectionUtils.isEmpty(formattedMuster16Data) ||
                formattedMuster16Data.size() < minLinesToParse) {
            throw new Muster16DataParserException(
                    String.format("There should be at least %d lines of extracted muster 16 " +
                    "data to parse.", minLinesToParse));
        }

       formDates = getDates();

        return this;
    }
    
    public String parseInsuranceCompany() {
        String insuranceCompany = getDataFieldAtPosOrDefault(
                (String[]) formattedMuster16Data.toArray(), 0, "").trim();

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

    protected List<Map<Integer, String>> getDates() {
        List<Map<Integer, String>> formDates = new ArrayList<>(2);
        String[] muster16PdfDataFields = (String[]) formattedMuster16Data.toArray();
        String[] lineElements;
        Map<Integer, String> datesMap = new HashMap<>(2);

        for(int i = 0; isDataFieldPresentAtPosition(muster16PdfDataFields, i) &&
                i < muster16PdfDataFields.length; i++) {
            lineElements = muster16PdfDataFields[i].split("\\s");

            if(lineElements != null) {
                for(int j = 0; j < lineElements.length; j++) {
                    if(lineElements[j].trim().matches("\\d\\d\\.\\d\\d\\.\\d\\d")) {
                        datesMap = new HashMap<>();

                        datesMap.put(i, lineElements[j].trim());
                        formDates.add(datesMap);
                    }
                }
            }
        }

        return formDates;
    }

    public String parsePatientDateOfBirth() {
        int index = 0;
        List<Map<Integer, String>> dates = getFormDates();
        String patientDateOfBirth = dates.size() == 2? dates.get(index).get(index) : "";

        return patientDateOfBirth;
    }

    public String parseClinicId() {
        String clinicId = "";
        List<Map<Integer, String>> dates = getFormDates();

        if(dates.size() == 2) {
            int targetIndex = 1;
            int lineIndex = dates.get(targetIndex).keySet().stream().findAny().get();
            String targetLine = formattedMuster16Data.get(lineIndex);
            String[] lineSplit = targetLine.split("\\s+");

            clinicId = lineSplit[0].trim();
        }

        return clinicId;
    }

    public String parseDoctorId() {
        String doctorId = "";

        return doctorId;
    }

    public String parsePrescriptionDate() {
        int index = 1;
        List<Map<Integer, String>> dates = getFormDates();
        String prescriptionDate = dates.size() == 2? dates.get(index).get(index) : "";

        return prescriptionDate;
    }
    public List<MedicationString> parsePrescriptionList() {
        List<MedicationString> prescriptionList = new ArrayList<>(1);

        return prescriptionList;
    }

    public String parsePatientInsuranceId() {
        String patientInsuranceId = "";

        return patientInsuranceId;
    }

    public String getMuster16PdfData() {
        return muster16PdfData;
    }

    public void setMuster16PdfData(String muster16PdfData) {
        this.muster16PdfData = muster16PdfData;
    }

    public DataExtractor<String> getDataExtractor() {
        return dataExtractor;
    }

    public void setDataExtractor(DataExtractor<String> dataExtractor) {
        this.dataExtractor = dataExtractor;
    }

    public DataFilter<String> getDataFilter() {
        return dataFilter;
    }

    public void setDataFilter(DataFilter<String> dataFilter) {
        this.dataFilter = dataFilter;
    }

    public DataFormatter<List<String>> getDataFormatter() {
        return dataFormatter;
    }

    public void setDataFormatter(DataFormatter<List<String>> dataFormatter) {
        this.dataFormatter = dataFormatter;
    }

    public InputStream getMuster16PdfInputStream() {
        return muster16PdfInputStream;
    }

    public void setMuster16PdfInputStream(InputStream muster16PdfInputStream) {
        this.muster16PdfInputStream = muster16PdfInputStream;
    }

    public List<Map<Integer, String>> getFormDates() {
        return ListUtils.emptyIfNull(formDates);
    }
}
