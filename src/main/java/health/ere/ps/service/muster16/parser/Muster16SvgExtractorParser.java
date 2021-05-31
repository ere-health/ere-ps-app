package health.ere.ps.service.muster16.parser;

import org.apache.commons.lang3.StringUtils;

import health.ere.ps.model.muster16.MedicationString;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class Muster16SvgExtractorParser implements IMuster16FormParser {

    private static final Logger log = Logger.getLogger(Muster16SvgExtractorParser.class.getName());

    private static final Pattern ONLY_NUMBERS = Pattern.compile(".*?(\\d+).*?", Pattern.DOTALL);

    private Map<String, String> mappedFields;
    private String[] nameAndAddressInfo;
    private List<String> prescriptionInfo;

    private static final Pattern PZN_MATCH = Pattern.compile("PZN(\\d+)");

    public Muster16SvgExtractorParser(Map<String,String> mappedFields)  {
        this.mappedFields = mappedFields;

        nameAndAddressInfo = getMappedFields().getOrDefault(
                "nameAndAddress", "").split("\\n");

        prescriptionInfo = Arrays.asList(getMappedFields().getOrDefault(
                "medication", "").split("\\n"))
                .stream()
                    .filter(s -> !s.contains("-  -  -  -"))
                    .filter(s -> !s.contains("********"))
                .collect(Collectors.toList());
        mergePZNWithNameLine();
    }

    public void mergePZNWithNameLine() {
        List<Integer> linesToMerge = new ArrayList<>();
        for(int i=0;i<prescriptionInfo.size();i++) {
            Matcher m = PZN_MATCH.matcher(prescriptionInfo.get(i));
            if(m.matches()) {
                linesToMerge.add(i-1);
            }
        }
        for(int i : linesToMerge) {
            prescriptionInfo = merge(prescriptionInfo, i);
        }
    }

    public static List<String> merge(final List<String> list, final int index) {
        if (list.isEmpty()) {
            // throw new IndexOutOfBoundsException("Cannot merge empty list");
            return list;
        } else if (index < 0) {
            // throw new IndexOutOfBoundsException("Cannot merge negative entry");
            return list;
        } else if (index + 1 >= list.size()) {
            // throw new IndexOutOfBoundsException("Cannot merge last element");
            return list;
        } else {
            final List<String> result = new ArrayList<String>(list);
            result.set(index, list.get(index) + " " + list.get(index + 1));
            result.remove(index + 1);
            return result;
        }
    }

    @Override
    public String parseInsuranceCompany() {
        return getMappedFields().getOrDefault("insurance", "");
    }

    @Override
    public String parseInsuranceCompanyId() {
        String payorId = getMappedFields().getOrDefault("payor", "");
        Matcher m = ONLY_NUMBERS.matcher(payorId);
        if(m.matches()) {
            payorId = m.group(1);
        }
        return payorId;
    }

    @Override
    public String parsePatientNamePrefix() {
        return "";
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
        String clinicId = getMappedFields().getOrDefault("locationNumber", "");
        Matcher m = ONLY_NUMBERS.matcher(clinicId);
        if(m.matches()) {
            clinicId = m.group(1);
        }
        return clinicId;
    }

    @Override
    public String parseDoctorId() {
        String doctorId = getMappedFields().getOrDefault("practitionerNumber", "");
        Matcher m = ONLY_NUMBERS.matcher(doctorId);
        if(m.matches()) {
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
        if(prescriptionInfo != null) {
            List<MedicationString> extractedMedicationFields =
                    prescriptionInfo.stream().map(med -> med.trim())
                            .filter(med -> StringUtils.isNotBlank(med))
                            .map(s -> new MedicationString(s))
                            .collect(Collectors.toList());
            return extractedMedicationFields;
        } else {
            return new ArrayList<>();
        }
    }

    @Override
    public String parsePatientInsuranceId() {
        String patientInsuranceId = getMappedFields().getOrDefault("insuranceNumber", "");
        if(patientInsuranceId != null && "".equals(patientInsuranceId.trim())) {
            log.warning("No patientInsuranceId found using A123456789");
            patientInsuranceId = "A123456789";
        }
        return patientInsuranceId;
    }

    public Map<String, String> getMappedFields() {
        return mappedFields;
    }
}
