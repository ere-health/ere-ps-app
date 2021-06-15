package health.ere.ps.service.muster16.parser.rgxer.formatter;

import java.util.function.Function;

public class FormattingChain {

    private String load;


    private FormattingChain(String raw) {
        load = raw;
    }

    public static FormattingChain format(String s) {
        return new FormattingChain(s);
    }

    public FormattingChain apply(Function<String, String> function) {
        load = function.apply(load);
        return this;
    }

    public String get() {
        return load;
    }
}
