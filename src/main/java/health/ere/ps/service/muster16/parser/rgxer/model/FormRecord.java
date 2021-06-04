package health.ere.ps.service.muster16.parser.rgxer.model;

public class FormRecord {

    private final String name;
    private final String code;

    public FormRecord(String name, String code) {
        this.name = name;
        this.code = code;
    }

    public String getName() {
        return name;
    }

    public String getCode() {
        return code;
    }
}
