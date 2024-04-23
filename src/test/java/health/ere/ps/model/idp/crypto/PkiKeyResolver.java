package health.ere.ps.model.idp.crypto;

import com.diffplug.common.base.Errors;
import com.diffplug.common.base.Throwing;
import health.ere.ps.exception.idp.crypto.IdpCryptoException;
import health.ere.ps.service.idp.crypto.CryptoLoader;
import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.ParameterContext;
import org.junit.jupiter.api.extension.ParameterResolver;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.lang.annotation.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.stream.Stream;

public class PkiKeyResolver implements ParameterResolver {

    @Override
    public boolean supportsParameter(final ParameterContext parameterContext, final ExtensionContext extensionContext) {
        return parameterContext.getParameter().getType() == PkiIdentity.class;
    }

    @Override
    public PkiIdentity resolveParameter(final ParameterContext parameterContext,
                                        final ExtensionContext extensionContext) {
        try {
            return retrieveIdentityFromFileSystem(getFilterValueForParameter(parameterContext));
        } catch (IdpCryptoException e) {
            throw new IllegalArgumentException(e);
        }
    }

    private String getFilterValueForParameter(final ParameterContext parameterContext) {
        if (parameterContext.getParameter().isAnnotationPresent(Filename.class)) {
            return parameterContext.getParameter().getAnnotation(Filename.class).value();
        } else {
            return parameterContext.getParameter().getName();
        }
    }

    private PkiIdentity retrieveIdentityFromFileSystem(final String fileFilter)
            throws IdpCryptoException {
        try (Stream<Path> pathStream = Files.find(Paths.get(".", "..", "src", "test",
                "resources", "certs"), 128,
                (p, a) -> p.toString().endsWith(".p12")
                        && p.getFileName().toString().toLowerCase().contains(
                        fileFilter.toLowerCase()))) {

            return pathStream.findFirst()
                    .map(Path::toFile)
                    .map(Errors.rethrow().wrap((Throwing.Function<Object, byte[]>) file -> {
                        try {
                            return FileUtils.readFileToByteArray((File) file);
                        } catch (final IOException e) {
                            throw new IdpCryptoException(e);
                        }
                    }))
                    .map(Errors.rethrow().wrap((Throwing.Function<Object, PkiIdentity>) bytes ->
                            CryptoLoader.getIdentityFromP12(new ByteArrayInputStream((byte[]) bytes),
                                    "00")))
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
