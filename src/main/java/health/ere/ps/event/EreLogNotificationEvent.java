package health.ere.ps.event;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

public class EreLogNotificationEvent implements Serializable {
    private List<String> systemContextList;
    private String simpleLogMessage;
    private String status;
    private String logMessage;
    private List<String> logMessageDetails;

    private Map<String, String> bundleInfo;

    public EreLogNotificationEvent(List<String> systemContextList, String simpleLogMessage,
                                   String status, String logMessage,
                                   List<String> logMessageDetails, Map<String, String> bundleInfo) {
        this.systemContextList = systemContextList;
        this.simpleLogMessage = simpleLogMessage;
        this.status = status;
        this.logMessage = logMessage;
        this.logMessageDetails = logMessageDetails;
        this.bundleInfo = bundleInfo;
    }

    public EreLogNotificationEvent() {

    }

    @Override
    public String toString() {
        ObjectMapper mapper = new ObjectMapper();
        String json = "{}";

        try {
            json = mapper.writeValueAsString( this );
        } catch (JsonProcessingException e) {

        }

        return json;
    }


    public List<String> getSystemContextList() {
        return systemContextList;
    }

    public String getSimpleLogMessage() {
        return simpleLogMessage;
    }

    public String getStatus() {
        return status;
    }

    public String getLogMessage() {
        return logMessage;
    }

    public List<String> getLogMessageDetails() {
        return logMessageDetails;
    }

    public Map<String, String> getBundleInfo() {
        return bundleInfo;
    }
}
