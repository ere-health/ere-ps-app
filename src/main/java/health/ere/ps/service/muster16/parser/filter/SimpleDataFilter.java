package health.ere.ps.service.muster16.parser.filter;

import org.apache.commons.lang3.StringUtils;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class SimpleDataFilter implements DataFilter<List<String>>{
    protected List<String> regexTokenExclusionList;

    public SimpleDataFilter(List<String> regexTokenExclusionList) {
        this.regexTokenExclusionList = regexTokenExclusionList;
    }

    @Override
    public List<String> filter(String muster16PdfData) {
        List<String> filteredData = Collections.EMPTY_LIST;

        if (StringUtils.isNotBlank(muster16PdfData)) {
            filteredData =
                    Arrays.stream(muster16PdfData.split("\\r?\\n"))
                    .map(subString -> Arrays.stream(subString.split("\\s+"))
                            .filter(strToken -> regexTokenExclusionList.stream()
                                    .anyMatch(regexToken -> !strToken.matches(regexToken)))
                            .map(strToken -> strToken.trim())
                            .filter(strToken -> strToken.length() > 0 &&
                                    !strToken.matches("\\s+"))
                            .collect(Collectors.joining(" "))).collect(Collectors.toList());
        }

        return filteredData;
    }
}
