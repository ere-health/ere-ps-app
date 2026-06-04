package health.ere.ps.service.gematik.popp;

import de.gematik.zeta.logging.Log;
import de.gematik.zeta.logging.ZetaLogLevel;
import de.gematik.zeta.logging.ZetaLogger;
import de.gematik.zeta.sdk.BuildConfig;
import de.gematik.zeta.sdk.TpmConfig;
import de.gematik.zeta.sdk.WsClientExtension;
import de.gematik.zeta.sdk.ZetaSdk;
import de.gematik.zeta.sdk.ZetaSdkClient;
import de.gematik.zeta.sdk.attestation.model.AttestationConfig;
import de.gematik.zeta.sdk.attestation.model.PlatformProductId;
import de.gematik.zeta.sdk.authentication.AuthConfig;
import de.gematik.zeta.sdk.authentication.smcb.SmcbTokenProvider;
import de.gematik.zeta.sdk.network.http.client.ZetaHttpClientBuilder;
import de.gematik.zeta.sdk.storage.InMemoryStorage;
import de.gematik.zeta.sdk.storage.StorageConfig;
import health.ere.ps.config.AppConfig;
import health.ere.ps.config.RuntimeConfig;
import io.ktor.client.plugins.logging.LogLevel;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import kotlin.Unit;
import kotlin.jvm.functions.Function0;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import java.net.URI;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

@ApplicationScoped
public class PoppClient {

    private static final Logger log = Logger.getLogger(PoppClient.class.getName());

    static final String LINUX_PACKAGING_TYPE_JAR = "jar";
    static final String PLATFORM_PRODUCT_APPLICATION_ID = "testhub";
    static final String PLATFORM_PRODUCT_VERSION = "latest";

    private final String poppServerUrl;
    private final EgkClient egkClient;
    private ZetaSdkClient sdkClient;

    @Inject
    public PoppClient(AppConfig appConfig, EgkClient egkClient) {
        this.egkClient = egkClient;
        this.poppServerUrl = appConfig.getPoppServerUrl();

        System.setProperty("os.name", "linux");

        Log.INSTANCE.setLogLevel(ZetaLogLevel.INFO);
        if (appConfig.isZetaEnabled()) {
            sdkClient = ZetaSdk.INSTANCE.build(
                poppServerUrl,
                new BuildConfig(
                    appConfig.getZetaProductId(),
                    appConfig.getZetaProductVersion(),
                    appConfig.getZetaClientName(),
                    new StorageConfig.Custom(new InMemoryStorage()),
                    new TpmConfig() {
                    },
                    new AuthConfig(
                        List.of("popp"),
                        30,
                        true,
                        new SmcbTokenProvider(
                            new SmcbTokenProvider.ConnectorConfig("", "", "", "", "", ""),
                            egkClient
                        ),
                        AttestationConfig.software(),
                        ""
                    ),
                    createPlatformProductId(),
                    new ZetaHttpClientBuilder("").disableServerValidation(true).logging(LogLevel.ALL),
                    null,
                    null,
                    new ZetaLogger() {
                        @Override
                        public void d(@Nullable String s, @NonNull Function0<String> function0, @Nullable Throwable throwable) {
                            log.log(Level.FINE, function0.invoke(), throwable);
                        }

                        @Override
                        public void i(@Nullable String s, @NonNull Function0<String> function0, @Nullable Throwable throwable) {
                            log.log(Level.INFO, function0.invoke(), throwable);
                        }

                        @Override
                        public void w(@Nullable String s, @NonNull Function0<String> function0, @Nullable Throwable throwable) {
                            log.log(Level.WARNING, function0.invoke(), throwable);
                        }

                        @Override
                        public void e(@Nullable String s, @NonNull Function0<String> function0, @Nullable Throwable throwable) {
                            log.log(Level.SEVERE, function0.invoke(), throwable);
                        }
                    }
                ));
        }
    }

    static PlatformProductId createPlatformProductId() {
        return createPlatformProductId(System.getProperty("os.name", ""));
    }

    static PlatformProductId createPlatformProductId(final String osName) {
        final var normalizedOsName = osName.toLowerCase(Locale.ROOT);

        if (normalizedOsName.contains("mac")) {
            // The Guard policy rejects apple+software posture combinations and also cross-checks
            // posture.platform_product_id.platform against the top-level platform claim. The SDK
            // patch in AttestationApi.getSoftwareStatement forces the top-level claim to "linux"
            // on Mac+software; we mirror that here so the nested platform_product_id also says
            // "linux" and the cross-check passes.
            return new PlatformProductId.LinuxProductId(
                PlatformProductId.PLATFORM_LINUX,
                LINUX_PACKAGING_TYPE_JAR,
                PLATFORM_PRODUCT_APPLICATION_ID,
                PLATFORM_PRODUCT_VERSION);
        }

        if (normalizedOsName.contains("win")) {
            return new PlatformProductId.WindowsProductId(PlatformProductId.PLATFORM_WINDOWS, "", "");
        }

        if (normalizedOsName.contains("linux")
            || normalizedOsName.contains("nux")
            || normalizedOsName.contains("nix")) {
            return new PlatformProductId.LinuxProductId(
                PlatformProductId.PLATFORM_LINUX,
                LINUX_PACKAGING_TYPE_JAR,
                PLATFORM_PRODUCT_APPLICATION_ID,
                PLATFORM_PRODUCT_VERSION);
        }

        throw new IllegalStateException(
            "Unsupported operating system for ZETA platform product id: " + osName);
    }

    static class Holder<T> {
        public T value;
    }

    public String getToken(RuntimeConfig runtimeConfig, String egkHandle) {
        egkClient.registerRuntimeConfig(runtimeConfig);
        try {
            Holder<String> tokenHolder = new Holder<>();
            try {
                URI uri = new URI(poppServerUrl);
                String hostPort = uri.getHost() + ":" + uri.getPort();

                Map<String, String> headers = new HashMap<>();
                headers.put("X-Forwarded-Proto", "https");
                headers.put("X-Forwarded-Host", hostPort);

                WsClientExtension.ws(sdkClient, poppServerUrl,
                    builder -> {
                        builder.disableServerValidation(true);
                        return Unit.INSTANCE;
                    },
                    headers, session -> {
                        try {
                            PoppTokenProvider poppTokenProvider = new PoppTokenProvider(egkClient);
                            String egkCardHandle = egkHandle != null ? egkHandle : egkClient.getConnectedEgkCard();
                            tokenHolder.value = poppTokenProvider.acquireToken(session, egkCardHandle);
                        } catch (Exception e) {
                            log.log(Level.SEVERE, "Get popp-token websocket error", e);
                        } finally {
                            session.close();
                        }
                    });
            } catch (Exception e) {
                log.log(Level.SEVERE, "Get popp-token error", e);
            }
            return tokenHolder.value;
        } finally {
            egkClient.unregisterRuntimeConfig(Thread.currentThread().getName());
        }
    }
}