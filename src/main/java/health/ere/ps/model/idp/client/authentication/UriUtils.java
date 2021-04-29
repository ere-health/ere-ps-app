package health.ere.ps.model.idp.client.authentication;


import java.net.URI;
import java.net.URISyntaxException;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import health.ere.ps.exception.idp.IdpRuntimeException;

public class UriUtils {

    private UriUtils() {

    }

    public static Optional<String> extractParameterValueOptional(final String uri, final String parameterName) {
        try {
            return Stream.of(new URI(uri).getQuery().split("&"))
                .filter(str -> str.startsWith(parameterName + "="))
                .map(str -> str.replace(parameterName + "=", ""))
                .findAny();
        } catch (final URISyntaxException e) {
            throw new IdpRuntimeException(e);
        }
    }

    public static Map<String, String> extractParameterMap(final String uri) {
        try {
            return Stream.of(new URI(uri).getQuery().split("&"))
                .filter(param -> param.contains("="))
                .map(param -> param.split("="))
                .collect(Collectors.toMap(array -> array[0], array -> array[1]));
        } catch (final URISyntaxException e) {
            throw new IdpRuntimeException(e);
        }
    }

    public static String extractParameterValue(final String uri, final String param) {
        return extractParameterValueOptional(uri, param)
            .orElseThrow(() -> new RuntimeException("Could not find '" + param + "' parameter in '" + uri + "'"));
    }
}
