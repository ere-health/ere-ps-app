package health.ere.ps.service.muster16.parser;

import health.ere.ps.model.muster16.MedicationString;
import org.apache.commons.lang3.StringUtils;

import java.util.*;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * @Deprecated Use Muster16SvgRegexParser instead
 * Has been replaced by the more efficient Muster16SvgRegexParser but is still up-to-date so far
 */
@Deprecated
public class Muster16SvgExtractorParser implements IMuster16FormParser {

    private static final Logger log = Logger.getLogger(Muster16SvgExtractorParser.class.getName());

    private static final Pattern ONLY_NUMBERS = Pattern.compile(".*?(\\d+).*?", Pattern.DOTALL);
    private static final Pattern PZN_MATCH = Pattern.compile("PZN(\\d+)");
    private static final Pattern MEDICATION_LINE = Pattern.compile("(.*)(N\\d)(.*)(PZN ?)(\\d+)");
    private final Map<String, String> mappedFields;
    private final String[] patientNameAndAddressInfo;
    private final String[] practitionerNameAndAddressInfo;
    private List<String> prescriptionInfo;

    public Muster16SvgExtractorParser(Map<String, String> mappedFields) {
        this.mappedFields = mappedFields;

        patientNameAndAddressInfo = getMappedFields().getOrDefault(
                "nameAndAddress", "").split("\\n");

        practitionerNameAndAddressInfo = getMappedFields().getOrDefault("practitionerText", "")
                .split("\\n");

        prescriptionInfo = Arrays.stream(getMappedFields().getOrDefault(
                "medication", "").split("\\n"))
                .filter(s -> !s.contains("-  -  -  -"))
                .filter(s -> !s.contains("********"))
                .collect(Collectors.toList());
        mergePZNWithNameLine();
    }

    private List<String> merge(final List<String> list, final int index) {
        if (list.isEmpty()) {
            return list;
        } else if (index < 0) {
            return list;
        } else if (index + 1 >= list.size()) {
            return list;
        } else {
            final List<String> result = new ArrayList<>(list);
            result.set(index, list.get(index) + " " + list.get(index + 1));
            result.remove(index + 1);
            return result;
        }
    }

    private void mergePZNWithNameLine() {
        List<Integer> linesToMerge = new ArrayList<>();
        for (int i = 0; i < prescriptionInfo.size(); i++) {
            Matcher m = PZN_MATCH.matcher(prescriptionInfo.get(i));
            if (m.matches()) {
                linesToMerge.add(i - 1);
            }
        }
        for (int i : linesToMerge) {
            prescriptionInfo = merge(prescriptionInfo, i);
        }
    }

    private MedicationString parseMedication(String name) {
        String dosage = null, pzn = null;
        Matcher m = MEDICATION_LINE.matcher(name);
        if (m.matches()) {
            name = m.group(1);
            dosage = m.group(3);
            pzn = m.group(5);
        }

        return new MedicationString(name, null, null, dosage, null, pzn);
    }

    @Override
    public String parseInsuranceCompany() {
        return getMappedFields().getOrDefault("insurance", "");
    }

    @Override
    public String parseInsuranceCompanyId() {
        String payorId = getMappedFields().getOrDefault("payor", "");
        Matcher m = ONLY_NUMBERS.matcher(payorId);
        if (m.matches()) {
            payorId = m.group(1);
        }
        return payorId;
    }

    @Override
    public List<String> parsePatientNamePrefix() {
        String firstName = getDataFieldAtPosOrDefault(patientNameAndAddressInfo, 1, "").trim();
        if (firstName.contains(".")) {
            return List.of(firstName.split(" ")[0]);
        }
        return Collections.emptyList();
    }

    @Override
    public String parsePatientFirstName() {
        String firstName = getDataFieldAtPosOrDefault(patientNameAndAddressInfo, 1, "").trim();
        if (firstName.contains(".")) {
            return firstName.split("\\.")[1].trim();
        } else {
            return firstName;
        }
    }

    @Override
    public String parsePatientLastName() {
        return getDataFieldAtPosOrDefault(patientNameAndAddressInfo, 0, "").trim();
    }

    @Override
    public String parsePatientStreetName() {
        String[] streetName = getDataFieldAtPosOrDefault(patientNameAndAddressInfo,
                2, "").split("\\d+");
        return getDataFieldAtPosOrDefault(streetName, 0, "").trim();
    }

    @Override
    public String parsePatientStreetNumber() {
        String[] streetNumber = getDataFieldAtPosOrDefault(patientNameAndAddressInfo,
                2, "").split("[a-zA-Z]+");
        return getDataFieldAtPosOrDefault(streetNumber, streetNumber.length - 1, "")
                .replace(".", "").trim();
    }

    @Override
    public String parsePatientCity() {
        String[] cityData = getDataFieldAtPosOrDefault(patientNameAndAddressInfo,
                3, "").split("\\d+");
        String extractedCityField =
                Arrays.stream(cityData).map(String::trim)
                        .collect(Collectors.toList()).get(1);
        return StringUtils.defaultString(extractedCityField).trim();
    }

    @Override
    public String parsePatientZipCode() {
        String[] zipCodeData = getDataFieldAtPosOrDefault(patientNameAndAddressInfo,
                3, "").split("[a-zA-Z]+");
        String extractedZipCodeField =
                Arrays.stream(zipCodeData).map(field -> field.trim()).collect(
                        Collectors.joining(" "));
        return StringUtils.defaultString(extractedZipCodeField).trim();
    }

    @Override
    public String parsePatientDateOfBirth() {
        return getMappedFields().getOrDefault("birthdate", "").trim();
    }

    @Override
    public String parsePatientStatus() {
        return getMappedFields().getOrDefault("status", "").trim();
    }

    @Override
    public String parseClinicId() {
        String clinicId = getMappedFields().getOrDefault("locationNumber", "");
        Matcher m = ONLY_NUMBERS.matcher(clinicId);
        if (m.matches()) {
            clinicId = m.group(1);
        }
        return clinicId;
    }

    @Override
    public String parseDoctorId() {
        String doctorId = getMappedFields().getOrDefault("practitionerNumber", "");
        Matcher m = ONLY_NUMBERS.matcher(doctorId);
        if (m.matches()) {
            doctorId = m.group(1);
        }
        return doctorId;
    }

    @Override
    public String parsePrescriptionDate() {
        return getMappedFields().getOrDefault("date", "");
    }

    @Override
    public List<MedicationString> parsePrescriptionList() {
        if (prescriptionInfo != null) {
            return prescriptionInfo.stream().map(String::trim)
                    .filter(StringUtils::isNotBlank)
                    .map(this::parseMedication)
                    .collect(Collectors.toList());
        } else {
            return new ArrayList<>();
        }
    }

    @Override
    public String parsePatientInsuranceId() {
        String patientInsuranceId = getMappedFields().getOrDefault("insuranceNumber", "");
        if (patientInsuranceId != null && "".equals(patientInsuranceId.trim())) {
            log.warning("No patientInsuranceId found using A123456789");
            patientInsuranceId = "A123456789";
        }
        return patientInsuranceId;
    }

    @Override
    public Boolean parseIsWithPayment() {
        return mappedFields.get("withPayment").trim().equals("X");
    }

    @Override
    public String parsePractitionerFirstName() {
        String firstName = getDataFieldAtPosOrDefault(practitionerNameAndAddressInfo, 0, "")
                .split(" ")[0].trim();
        if (firstName.contains(".")) {
            return firstName.split("\\.")[1].trim();
        } else {
            return firstName;
        }
    }

    @Override
    public String parsePractitionerLastName() {
        return getDataFieldAtPosOrDefault(practitionerNameAndAddressInfo, 0, "")
                .split(" ")[1].trim();
    }

    @Override
    public String parsePractitionerNamePrefix() {
        String firstName = getDataFieldAtPosOrDefault(practitionerNameAndAddressInfo, 0, "")
                .split(" ")[0].trim();
        if (firstName.contains(".")) {
            return firstName.split(" ")[0];
        }
        return "";
    }

    @Override
    public String parsePractitionerStreetName() {
        String[] streetName = getDataFieldAtPosOrDefault(practitionerNameAndAddressInfo,
                1, "").split("\\d+");
        return getDataFieldAtPosOrDefault(streetName, 0, "").trim();
    }

    @Override
    public String parsePractitionerStreetNumber() {
        String[] streetNumber = getDataFieldAtPosOrDefault(practitionerNameAndAddressInfo,
                1, "").split("[a-zA-Z]+");
        return getDataFieldAtPosOrDefault(streetNumber, streetNumber.length - 1, "")
                .replace(".", "").trim();
    }

    @Override
    public String parsePractitionerCity() {
        return getDataFieldAtPosOrDefault(practitionerNameAndAddressInfo,
                2, "").split(" ")[1].trim();
    }

    @Override
    public String parsePractitionerZipCode() {
        return getDataFieldAtPosOrDefault(practitionerNameAndAddressInfo,
                2, "").split(" ")[0].trim();
    }

    @Override
    public String parsePractitionerPhoneNumber() {
        return getDataFieldAtPosOrDefault(practitionerNameAndAddressInfo, 3, "").trim();
    }

    @Override
    public String parsePractitionerFaxNumber() {
        return getDataFieldAtPosOrDefault(practitionerNameAndAddressInfo, 4, "")
                .split("Fax:")[1].trim();
    }

    public Map<String, String> getMappedFields() {
        return mappedFields;
    }

    @Override
    public String parsePractitionerQualification() {
        return "00";
    }
}
