package health.ere.ps.service.idp.tests;

import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.ParameterContext;
import org.junit.jupiter.api.extension.ParameterResolver;

import java.io.IOException;
import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.stream.Stream;

import health.ere.ps.exception.idp.crypto.IdpCryptoException;
import health.ere.ps.model.idp.crypto.PkiIdentity;
import health.ere.ps.service.idp.crypto.CryptoLoader;

public class PkiKeyResolver implements ParameterResolver {

    @Override
    public boolean supportsParameter(final ParameterContext parameterContext, final ExtensionContext extensionContext) {
        return parameterContext.getParameter().getType() == PkiIdentity.class;
    }

    @Override
    public PkiIdentity resolveParameter(final ParameterContext parameterContext,
        final ExtensionContext extensionContext) {
        return retrieveIdentityFromFileSystem(getFilterValueForParameter(parameterContext));
    }

    private String getFilterValueForParameter(final ParameterContext parameterContext) {
        if (parameterContext.getParameter().isAnnotationPresent(Filename.class)) {
            return parameterContext.getParameter().getAnnotation(Filename.class).value();
        } else {
            return parameterContext.getParameter().getName();
        }
    }

    private PkiIdentity retrieveIdentityFromFileSystem(final String fileFilter) {
        try (final Stream<Path> pathStream = Files.find(Paths.get("src", "test",
                "resources", "certs"), 128,
            (p, a) -> p.toString().endsWith(".p12")
                && p.getFileName().toString().toLowerCase().contains(
                fileFilter.toLowerCase()))) {
            return pathStream.findFirst()
                .map(Path::toFile)
                .map(file -> {
                    try {
                        return FileUtils.readFileToByteArray(file);
                    } catch (final IOException e) {
                        throw new IdpCryptoException(e);
                    }
                })
                .map(bytes -> CryptoLoader.getIdentityFromP12(bytes, "00"))
                .orElseThrow(() -> new IdpCryptoException(
                    "No matching identity found in src/test/resources/certs and filter '" + fileFilter + "'"));
        } catch (final IOException e) {
            throw new IdpCryptoException("Error while querying file system", e);
        }
    }

    @Target(ElementType.PARAMETER)
    @Retention(RetentionPolicy.RUNTIME)
    @Documented
    public @interface Filename {

        String value();
    }
}
