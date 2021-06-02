package health.ere.ps.service.muster16.parser;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.ListUtils;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import health.ere.ps.exception.muster16.parser.Muster16DataParserException;
import health.ere.ps.exception.muster16.parser.extractor.Muster16DataExtractorException;
import health.ere.ps.exception.muster16.parser.formatter.Muster16DataFormatterException;
import health.ere.ps.model.muster16.MedicationString;
import health.ere.ps.service.muster16.parser.extractor.DataExtractor;
import health.ere.ps.service.muster16.parser.filter.DataFilter;
import health.ere.ps.service.muster16.parser.formatter.DataFormatter;

public class Muster16FormDataParser implements IMuster16FormParser {
    protected static final int TARGET_PARSED_FIELD_SLOTS = 16;
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
        PATIENT_NAME_TITLE,
        PATIENT_FIRST_NAME,
        PATIENT_LAST_NAME,
        PATIENT_COUNTRY_CODE,
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
     * <p>
     * You must call the {@link #configureParser()} method directly after instantiating a {@link
     * Muster16FormDataParser} object using this constructor and then call the {@link
     * #setMuster16PdfInputStream(InputStream)}, {@link #setDataExtractor(DataExtractor)}, and,
     * {@link #setDataFormatter(DataFormatter)} methods. Calling the {@link
     * #setDataFilter(DataFilter)} method is optional. You can then call the parseXXX methods.
     */
    public Muster16FormDataParser() {

    }

    /**
     * Constructor.
     * <p>
     * You must call the {@link #configureParser()} method directly after instantiating a {@link
     * Muster16FormDataParser} object using this constructor. You can then call the parseXXX
     * methods.
     *
     * @param muster16PdfInputStream input stream of the source Muster 16 PDF.
     * @param dataExtractor          extracts data from the source Muster 16 PDF.
     * @param dataFilter             filters the extracted data from the source Muster 16 PDF. This
     *                               is an optional parameter.
     * @param dataFormatter          formats the extracted and/or filtered Muster 16 PDF data.
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
        if (getDataExtractor() != null) {
            Optional<String> extractedData = getDataExtractor().extractData(getMuster16PdfInputStream());

            if (extractedData.isPresent()) {
                muster16PdfData = extractedData.get();

                if (getDataFilter() != null) {
                    muster16PdfData = getDataFilter().filter(muster16PdfData);
                }

                if (getDataFormatter() != null) {
                    Optional<List<String>> formattedData =
                            getDataFormatter().formatData(muster16PdfData);

                    formattedMuster16Data = formattedData.isPresent() ? formattedData.get() :
                            Collections.emptyList();
                    if (CollectionUtils.isEmpty(formattedMuster16Data)) {
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

        if (CollectionUtils.isEmpty(formattedMuster16Data) ||
                formattedMuster16Data.size() < minLinesToParse) {
            throw new Muster16DataParserException(
                    String.format("There should be at least %d lines of extracted muster 16 " +
                            "data to parse.", minLinesToParse));
        }

        parseDates();

        return this;
    }

    @Override
    public String parseInsuranceCompany() {
        if (getParsedFieldsCacheValue(Muster16FormField.INSURANCE_COMPANY).isPresent()) {
            return getParsedFieldsCacheValue(
                    Muster16FormField.INSURANCE_COMPANY).get().values().stream().findAny().get();
        }

        String insuranceCompany = getDataFieldAtPosOrDefault(
                (String[]) formattedMuster16Data.toArray(), 0, "").trim();

        parsedFieldsCache.put(Muster16FormField.INSURANCE_COMPANY, Map.of(0, insuranceCompany));

        return insuranceCompany;
    }

    @Override
    public String parseInsuranceCompanyId() {
        parseInsuranceCompanyIdAndPatientInsuranceId();

        if (getParsedFieldsCacheValue(Muster16FormField.INSURANCE_COMPANY_ID).isPresent()) {
            return getParsedFieldsCacheValue(
                    Muster16FormField.INSURANCE_COMPANY_ID).get().values().stream().findAny().get();
        }

        return "";
    }

    protected void parsePatientName() {
        if (getParsedFieldsCacheValue(Muster16FormField.PATIENT_FIRST_NAME).isPresent() &&
                getParsedFieldsCacheValue(Muster16FormField.PATIENT_LAST_NAME).isPresent()) {
                return;
        }

        parseInsuranceCompany();

        Map<Integer, String> insuranceCompanyLineMap =
                getParsedFieldsCacheValue(Muster16FormField.INSURANCE_COMPANY).orElse(
                        Collections.emptyMap());

        List<Map<Integer, String>> dates = getFormDates();

        if (dates.size() == 2 && MapUtils.isNotEmpty(insuranceCompanyLineMap)) {
            int targetMapIndex = 1;
            int targetInsuranceCompanyLineIndex =
                    insuranceCompanyLineMap.keySet().stream().findAny().get();
            int targetLineIndex = dates.get(targetMapIndex).keySet().stream().findAny().get() - 4;
            int targetLinesCount = (targetLineIndex - targetInsuranceCompanyLineIndex);
            String[] nameComponents;

            // Capture name components
            for(int i = 1; i <= targetLinesCount; i++) {
                nameComponents = formattedMuster16Data.get(i).split("\\s+,?");

                if(i == 1) {
                    if(ArrayUtils.isNotEmpty(nameComponents) && nameComponents.length >= 2 &&
                            targetLinesCount == 1) {
                        parsedFieldsCache.put(Muster16FormField.PATIENT_LAST_NAME,
                                Map.of(i, nameComponents[0].trim()));
                        parsedFieldsCache.put(Muster16FormField.PATIENT_FIRST_NAME,
                                Map.of(i, nameComponents[1].trim()));
                        break;
                    } else if(ArrayUtils.isNotEmpty(nameComponents) && nameComponents.length >= 1 &&
                            targetLinesCount == 2) {
                        parsedFieldsCache.put(Muster16FormField.PATIENT_LAST_NAME,
                                Map.of(i, nameComponents[0].trim()));
                    }

                } else if(i == 2) {
                    if(ArrayUtils.isNotEmpty(nameComponents) && nameComponents.length >= 2) {
                        parsedFieldsCache.put(Muster16FormField.PATIENT_NAME_TITLE,
                                Map.of(i, nameComponents[0].trim()));
                        parsedFieldsCache.put(Muster16FormField.PATIENT_FIRST_NAME,
                                Map.of(i, nameComponents[1].trim()));
                    } else if(ArrayUtils.isNotEmpty(nameComponents) && nameComponents.length >= 1) {
                        parsedFieldsCache.put(Muster16FormField.PATIENT_FIRST_NAME,
                                Map.of(i, nameComponents[0].trim()));
                    }
                    break;
                }
            }
        }
    }

    @Override
    public String parsePatientNameTitle() {
        parsePatientName();

        if(getParsedFieldsCacheValue(Muster16FormField.PATIENT_NAME_TITLE).isPresent()) {
            return getParsedFieldsCacheValue(
                    Muster16FormField.PATIENT_NAME_TITLE).get().values().stream().findAny().get();
        }

        return "";
    }

    @Override
    public String parsePatientFirstName() {
        parsePatientName();

        if(getParsedFieldsCacheValue(Muster16FormField.PATIENT_FIRST_NAME).isPresent()) {
            return getParsedFieldsCacheValue(
                    Muster16FormField.PATIENT_FIRST_NAME).get().values().stream().findAny().get();
        }

        return "";
    }

    @Override
    public String parsePatientLastName() {
        parsePatientName();

        if(getParsedFieldsCacheValue(Muster16FormField.PATIENT_LAST_NAME).isPresent()) {
            return getParsedFieldsCacheValue(
                    Muster16FormField.PATIENT_LAST_NAME).get().values().stream().findAny().get();
        }

        return "";
    }

    protected void parsePatientAddress() {
        if (getParsedFieldsCacheValue(Muster16FormField.PATIENT_CITY).isPresent() &&
                getParsedFieldsCacheValue(Muster16FormField.PATIENT_STREET_NAME).isPresent() &&
                getParsedFieldsCacheValue(Muster16FormField.PATIENT_STREET_NUMBER).isPresent() &&
                getParsedFieldsCacheValue(Muster16FormField.PATIENT_ZIP_CODE).isPresent()) {
            return;
        }

        String zipCode = "";
        String streetName = "";
        String streetNumber = "";
        String city = "";
        String countryCode = "";
        List<Map<Integer, String>> dates = getFormDates();

        if (dates.size() == 2) {
            int targetMapIndex = 1;
            int targetLineIndex = dates.get(targetMapIndex).keySet().stream().findAny().get() - 2;

            String countryCodeZipAndCityLine = formattedMuster16Data.get(targetLineIndex);

            // Parse country code, city and zip code.
            if (StringUtils.isNotBlank(countryCodeZipAndCityLine)) {
                String[] lineSplits = countryCodeZipAndCityLine.split("\\s+,?");

                if (ArrayUtils.isNotEmpty(lineSplits) && lineSplits.length >= 2) {
                    countryCode = lineSplits.length >= 3? lineSplits[0].trim() : "";
                    city = lineSplits[lineSplits.length - 1].trim();
                    city = !NumberUtils.isDigits(city) ? city : "";
                    zipCode = Arrays.stream(lineSplits).filter(
                            token -> token.trim().matches(
                                    "\\d\\d\\d\\d\\d")).findFirst().orElse("");

                    if (StringUtils.isNotBlank(countryCode)) {
                        parsedFieldsCache.put(Muster16FormField.PATIENT_COUNTRY_CODE,
                                Map.of(targetLineIndex, countryCode));
                    }

                    if (StringUtils.isNotBlank(city)) {
                        parsedFieldsCache.put(Muster16FormField.PATIENT_CITY,
                                Map.of(targetLineIndex, city));
                    }

                    if (StringUtils.isNotBlank(zipCode)) {
                        parsedFieldsCache.put(Muster16FormField.PATIENT_ZIP_CODE,
                                Map.of(targetLineIndex, zipCode));
                    }
                }
            }

            // Parse street name and number.
            --targetLineIndex;

            String streetNameAndNumberLine = formattedMuster16Data.get(targetLineIndex);

            if (StringUtils.isNotBlank(streetNameAndNumberLine)) {
                String[] lineSplits = streetNameAndNumberLine.split("\\s+");

                if (ArrayUtils.isNotEmpty(lineSplits) && lineSplits.length >= 2) {
                    streetNumber = lineSplits[lineSplits.length - 1].trim();
                    streetNumber =
                            Arrays.stream(streetNumber.split("")).anyMatch(c -> NumberUtils.isDigits(c)) ?
                                    streetNumber : "";
                    streetName = Arrays.stream(ArrayUtils.subarray(lineSplits, 0,
                            lineSplits.length - 2)).collect(
                            Collectors.joining(" ")).trim();

                    if (StringUtils.isNotBlank(streetNumber)) {
                        parsedFieldsCache.put(Muster16FormField.PATIENT_STREET_NUMBER,
                                Map.of(targetLineIndex, streetNumber));
                    }

                    if (StringUtils.isNotBlank(streetName)) {
                        parsedFieldsCache.put(Muster16FormField.PATIENT_STREET_NAME,
                                Map.of(targetLineIndex, streetName));
                    }
                }
            }

        }
    }

    @Override
    public String parsePatientStreetName() {
        parsePatientAddress();

        if (getParsedFieldsCacheValue(Muster16FormField.PATIENT_STREET_NAME).isPresent()) {
            return getParsedFieldsCacheValue(
                    Muster16FormField.PATIENT_STREET_NAME).get().values().stream().findAny().get();
        }

        return "";
    }

    @Override
    public String parsePatientStreetNumber() {
        parsePatientAddress();

        if (getParsedFieldsCacheValue(Muster16FormField.PATIENT_STREET_NUMBER).isPresent()) {
            return getParsedFieldsCacheValue(
                    Muster16FormField.PATIENT_STREET_NUMBER).get().values().stream().findAny().get();
        }

        return "";
    }

    @Override
    public String parsePatientCountryCode() {
        parsePatientAddress();

        if (getParsedFieldsCacheValue(Muster16FormField.PATIENT_COUNTRY_CODE).isPresent()) {
            return getParsedFieldsCacheValue(
                    Muster16FormField.PATIENT_COUNTRY_CODE).get().values().stream().findAny().get();
        }

        return "";
    }

    @Override
    public String parsePatientCity() {
        parsePatientAddress();

        if (getParsedFieldsCacheValue(Muster16FormField.PATIENT_CITY).isPresent()) {
            return getParsedFieldsCacheValue(
                    Muster16FormField.PATIENT_CITY).get().values().stream().findAny().get();
        }

        return "";
    }

    @Override
    public String parsePatientZipCode() {
        parsePatientAddress();

        if (getParsedFieldsCacheValue(Muster16FormField.PATIENT_ZIP_CODE).isPresent()) {
            return getParsedFieldsCacheValue(
                    Muster16FormField.PATIENT_ZIP_CODE).get().values().stream().findAny().get();
        }

        return "";
    }

    protected List<Map<Integer, String>> parseDates() throws Muster16DataParserException {
        if (CollectionUtils.isNotEmpty(formDates)) {
            return formDates;
        }

        List<Map<Integer, String>> dates = new ArrayList<>(2);
        String[] muster16PdfDataFields = (String[]) formattedMuster16Data.toArray();
        String[] lineElements;
        Map<Integer, String> datesMap = new HashMap<>(2);

        for (int i = 0; isDataFieldPresentAtPosition(muster16PdfDataFields, i) &&
                i < muster16PdfDataFields.length; i++) {
            lineElements = muster16PdfDataFields[i].split("\\s+");

            if (lineElements != null) {
                for (int j = 0; j < lineElements.length; j++) {
                    if (lineElements[j].trim().matches("\\d\\d\\.\\d\\d\\.\\d\\d | \\d\\d\\" +
                                    ".\\d\\d\\.\\d\\d\\d\\d")) {
                        datesMap = new HashMap<>();

                        datesMap.put(i, lineElements[j].trim());
                        dates.add(datesMap);
                    }
                }
            }
        }

        if (dates.size() == 2) {
            formDates = dates;
        } else {
            throw new Muster16DataParserException(
                    String.format("Wrong number of muster 16 form dates. Expected %d but got " +
                            "found %d", 2, dates.size()));
        }

        return dates;
    }

    @Override
    public String parsePatientDateOfBirth() {
        int index = 0;
        List<Map<Integer, String>> dates = getFormDates();
        String patientDateOfBirth = dates.size() == 2 ? dates.get(index).get(index) : "";

        return patientDateOfBirth;
    }

    protected Optional<Map<Integer, String>> getParsedFieldsCacheValue(Muster16FormField cacheKey) {
        return Optional.ofNullable(parsedFieldsCache.get(cacheKey));
    }

    protected void parseDoctorIdAndClinicId() {
        if(getParsedFieldsCacheValue(Muster16FormField.CLINIC_ID).isPresent() &&
            getParsedFieldsCacheValue(Muster16FormField.DOCTOR_ID).isPresent()) {
            return;
        }

        String doctorId = "";
        String clinicId = "";
        List<Map<Integer, String>> dates = getFormDates();

        if (dates.size() == 2) {
            int targetMapIndex = 1;
            int targetLineIndex = dates.get(targetMapIndex).keySet().stream().findAny().get();
            String targetLine = formattedMuster16Data.get(targetLineIndex);
            String[] lineSplit = targetLine.split("\\s+");

            if (ArrayUtils.isNotEmpty(lineSplit) && lineSplit.length >= 2) {
                clinicId = lineSplit[0].trim();
                doctorId = lineSplit[1].trim();

                parsedFieldsCache.put(Muster16FormField.CLINIC_ID,
                        Map.of(targetLineIndex, clinicId));

                parsedFieldsCache.put(Muster16FormField.DOCTOR_ID,
                        Map.of(targetLineIndex, doctorId));
            }
        }
    }

    @Override
    public String parseClinicId() {
        parseDoctorIdAndClinicId();

        if (getParsedFieldsCacheValue(Muster16FormField.CLINIC_ID).isPresent()) {
            return getParsedFieldsCacheValue(
                    Muster16FormField.CLINIC_ID).get().values().stream().findAny().get();
        }

        return "";
    }

    @Override
    public String parseDoctorId() {
        parseDoctorIdAndClinicId();
        if (getParsedFieldsCacheValue(Muster16FormField.DOCTOR_ID).isPresent()) {
            return getParsedFieldsCacheValue(
                    Muster16FormField.DOCTOR_ID).get().values().stream().findAny().get();
        }



        return "";
    }

    @Override
    public String parsePrescriptionDate() {
        if (getParsedFieldsCacheValue(Muster16FormField.PRESCRIPTION_DATE).isPresent()) {
            return getParsedFieldsCacheValue(
                    Muster16FormField.PRESCRIPTION_DATE).get().values().stream().findAny().get();
        }

        String prescriptionDate = "";
        List<Map<Integer, String>> dates = getFormDates();

        if (dates.size() == 2) {
            int targetMapIndex = 1;
            int targetLineIndex = dates.get(targetMapIndex).keySet().stream().findAny().get();

            prescriptionDate = dates.get(targetMapIndex).values().stream().findAny().get();

            if (StringUtils.isNotBlank(prescriptionDate)) {
                parsedFieldsCache.put(Muster16FormField.PRESCRIPTION_DATE,
                        Map.of(targetLineIndex, prescriptionDate));
            }
        }

        return prescriptionDate;
    }

    @Override
    public List<MedicationString> parsePrescriptionList() {
        List<MedicationString> prescriptionList = new ArrayList<>(1);

        return prescriptionList;
    }

    protected void parseInsuranceCompanyIdAndPatientInsuranceId() {
        if (getParsedFieldsCacheValue(Muster16FormField.INSURANCE_COMPANY_ID).isPresent() &&
                getParsedFieldsCacheValue(Muster16FormField.PATIENT_INSURANCE_ID).isPresent()) {
            return;
        }

        String insuranceCompanyId = "";
        String patientInsuranceId = "";
        List<Map<Integer, String>> dates = getFormDates();

        if (dates.size() == 2) {
            int targetMapIndex = 1;
            int targetLineIndex = dates.get(targetMapIndex).keySet().stream().findAny().get() - 1;

            String targetLine = formattedMuster16Data.get(targetLineIndex);

            if (StringUtils.isNotBlank(targetLine)) {
                String[] lineSplits = targetLine.split("\\s+");

                if (ArrayUtils.isNotEmpty(lineSplits) && lineSplits.length >= 2) {
                    insuranceCompanyId = lineSplits[0].trim();
                    patientInsuranceId = lineSplits[1].trim();

                    if (StringUtils.isNotBlank(patientInsuranceId)) {
                        parsedFieldsCache.put(Muster16FormField.INSURANCE_COMPANY_ID,
                                Map.of(targetLineIndex, insuranceCompanyId));
                    }

                    if (StringUtils.isNotBlank(patientInsuranceId)) {
                        parsedFieldsCache.put(Muster16FormField.PATIENT_INSURANCE_ID,
                                Map.of(targetLineIndex, patientInsuranceId));
                    }
                }
            }
        }
    }

    @Override
    public String parsePatientInsuranceId() {
        parseInsuranceCompanyIdAndPatientInsuranceId();

        if (getParsedFieldsCacheValue(Muster16FormField.PATIENT_INSURANCE_ID).isPresent()) {
            return getParsedFieldsCacheValue(
                    Muster16FormField.PATIENT_INSURANCE_ID).get().values().stream().findAny().get();
        }

        return "";
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
