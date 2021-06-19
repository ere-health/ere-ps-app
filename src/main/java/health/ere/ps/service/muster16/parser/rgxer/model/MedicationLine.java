package health.ere.ps.service.muster16.parser.rgxer.model;

import java.util.*;
import java.util.stream.Collectors;

public class MedicationLine {

    private String value;
    private final List<String> sequence;

    public MedicationLine(String line) {
        value = line;
        String[] tokens = line.split(" ");
        sequence = Arrays.stream(tokens).collect(Collectors.toList());
    }

    public String getValue() {
        return value;
    }

    public void merge(MedicationLine other) {
        value = String.format("%s %s", value, other.value);
        sequence.addAll(other.sequence);
    }

    public boolean contains(String token) {
        return this.sequence.contains(token);
    }

    public List<String> getSequence() {
        return sequence;
    }
}
