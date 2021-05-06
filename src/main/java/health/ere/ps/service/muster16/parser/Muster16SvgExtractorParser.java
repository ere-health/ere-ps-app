package health.ere.ps.service.muster16.parser;

import org.apache.commons.lang3.StringUtils;

import java.io.InputStream;
import java.net.URISyntaxException;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import health.ere.ps.service.extractor.SVGExtractor;

public class Muster16SvgExtractorParser implements IMuster16FormParser {
    private SVGExtractor svgExtractor;
    private Map<String, String> mappedFields;
    private String[] nameAndAddressInfo;
    private String[] prescriptionInfo;

    public Muster16SvgExtractorParser(InputStream muster16PdfFile) throws URISyntaxException {
        svgExtractor = new SVGExtractor(getClass().getResource(
                "/svg-extract-templates/Muster-16-Template.svg").toURI(), false);

        mappedFields = svgExtractor.extract(muster16PdfFile);

        nameAndAddressInfo = getMappedFields().getOrDefault(
                "nameAndAddress", "").split("\\n");

        prescriptionInfo = getMappedFields().getOrDefault(
                "medication", "").split("\\n");
    }

    @Override
    public String parseInsuranceCompany() {
        return getMappedFields().getOrDefault("insurance", "");
    }

    @Override
    public String parseInsuranceCompanyId() {
        return getMappedFields().getOrDefault("payor", "");
    }

    @Override
    public String parsePatientFirstName() {
        return getDataFieldAtPosOrDefault(nameAndAddressInfo, 1, "");
    }

    @Override
    public String parsePatientLastName() {
         return getDataFieldAtPosOrDefault(nameAndAddressInfo, 0, "");
    }

    @Override
    public String parsePatientStreetName() {
        String[] streetName = getDataFieldAtPosOrDefault(nameAndAddressInfo,
                2, "").split("\\d+");
        return getDataFieldAtPosOrDefault(streetName, 0, "").trim();
    }

    @Override
    public String parsePatientStreetNumber() {
        String[] streetNumber = getDataFieldAtPosOrDefault(nameAndAddressInfo,
                2, "").split("[a-zA-Z]+");
        return getDataFieldAtPosOrDefault(streetNumber, 0, "").trim();
    }

    @Override
    public String parsePatientCity() {
        String[] cityData = getDataFieldAtPosOrDefault(nameAndAddressInfo,
                3, "").split("\\d+");
        String extractedCityField =
                Arrays.stream(cityData).map(field -> field.trim()).collect(
                        Collectors.joining(" "));
        return StringUtils.defaultString(extractedCityField);
    }

    @Override
    public String parsePatientZipCode() {
        String[] zipCodeData = getDataFieldAtPosOrDefault(nameAndAddressInfo,
                3, "").split("[a-zA-Z]+");
        String extractedZipCodeField =
                Arrays.stream(zipCodeData).map(field -> field.trim()).collect(
                        Collectors.joining(" "));
        return StringUtils.defaultString(extractedZipCodeField);
    }

    @Override
    public String parsePatientDateOfBirth() {
        return getMappedFields().getOrDefault("birthdate", "");
    }

    @Override
    public String parseClinicId() {
        return getMappedFields().getOrDefault("locationNumber", "");
    }

    @Override
    public String parseDoctorId() {
        return getMappedFields().getOrDefault("practitionerNumber", "");
    }

    @Override
    public String parsePrescriptionDate() {
        return getMappedFields().getOrDefault("date", "");
    }

    @Override
    public List<String> parsePrescriptionList() {
        List<String> extractedMedicationFields =
                Arrays.stream(prescriptionInfo).map(med -> med.trim())
                        .filter(med -> StringUtils.isNotBlank(med)).collect(
                        Collectors.toList());
        return extractedMedicationFields;
    }

    @Override
    public String parsePatientInsuranceId() {
        return getMappedFields().getOrDefault("insuranceNumber", "");
    }

    public Map<String, String> getMappedFields() {
        return mappedFields;
    }
}
