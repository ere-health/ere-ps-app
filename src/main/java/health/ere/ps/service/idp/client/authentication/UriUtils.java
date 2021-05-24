package health.ere.ps.service.idp.client.authentication;


import java.net.URI;
import java.net.URISyntaxException;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import health.ere.ps.exception.idp.IdpException;

public class UriUtils {

    private UriUtils() {

    }

    public static Optional<String> extractParameterValueOptional(final String uri,
                                                                 final String parameterName)
            throws IdpException {
        try {
            return Stream.of(new URI(uri).getQuery().split("&"))
                .filter(str -> str.startsWith(parameterName + "="))
                .map(str -> str.replace(parameterName + "=", ""))
                .findAny();
        } catch (final URISyntaxException e) {
            throw new IdpException(e);
        }
    }

    public static Map<String, String> extractParameterMap(final String uri) throws IdpException {
        try {
            return Stream.of(new URI(uri).getQuery().split("&"))
                .filter(param -> param.contains("="))
                .map(param -> param.split("="))
                .collect(Collectors.toMap(array -> array[0], array -> array[1]));
        } catch (final URISyntaxException e) {
            throw new IdpException(e);
        }
    }

    public static String extractParameterValue(final String uri, final String param) throws IdpException {
        return extractParameterValueOptional(uri, param)
            .orElseThrow(() -> new RuntimeException("Could not find '" + param +
                    "' parameter in '" + uri + "'"));
    }
}
