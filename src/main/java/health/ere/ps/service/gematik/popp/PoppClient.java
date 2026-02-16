package health.ere.ps.service.gematik.popp;

import de.gematik.zeta.sdk.BuildConfig;
import de.gematik.zeta.sdk.StorageConfig;
import de.gematik.zeta.sdk.TpmConfig;
import de.gematik.zeta.sdk.WsClientExtension;
import de.gematik.zeta.sdk.ZetaSdkClient;
import de.gematik.zeta.sdk.attestation.model.ClientSelfAssessment;
import de.gematik.zeta.sdk.attestation.model.PlatformProductId;
import de.gematik.zeta.sdk.authentication.AuthConfig;
import de.gematik.zeta.sdk.authentication.smcb.SmcbTokenProvider;
import de.gematik.zeta.sdk.network.http.client.ZetaHttpClientBuilder;
import de.gematik.zeta.sdk.storage.InMemoryStorage;
import health.ere.ps.config.AppConfig;
import health.ere.ps.config.RuntimeConfig;
import health.ere.ps.zeta.ZetaEreSdk;
import io.ktor.client.plugins.logging.LogLevel;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import kotlin.Unit;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

@ApplicationScoped
public class PoppClient {

    private static final Logger log = Logger.getLogger(PoppClient.class.getName());

    private final String poppServerUrl;
    private final KonnektorClient konnektorClient;
    private ZetaSdkClient sdkClient;

    @Inject
    public PoppClient(AppConfig appConfig, KonnektorClient konnektorClient) {
        this.konnektorClient = konnektorClient;
        this.poppServerUrl = appConfig.getPoppServerUrl();

        if (appConfig.isZetaEnabled()) {
            sdkClient = ZetaEreSdk.INSTANCE.build(
                appConfig.getZetaAuthServerUrl(),
                new BuildConfig(
                    appConfig.getZetaProductId(),
                    appConfig.getZetaProductVersion(),
                    appConfig.getZetaClientName(),
                    new StorageConfig(new InMemoryStorage(), ""),
                    new TpmConfig() {
                    },
                    new AuthConfig(
                        List.of("zero:audience"),
                        30,
                        true,
                        new SmcbTokenProvider(
                            new SmcbTokenProvider.ConnectorConfig("", "", "", "", "", ""),
                            konnektorClient
                        )
                    ),
                    new ClientSelfAssessment(
                        appConfig.getZetaAssessmentName(),
                        appConfig.getZetaAssessmentClientId(),
                        appConfig.getZetaAssessmentManufacturerId(),
                        appConfig.getZetaAssessmentManufacturerName(),
                        appConfig.getZetaAssessmentOwnerMail(),
                        0,
                        new PlatformProductId.AppleProductId("apple", "macos", List.of("bundleX"))
                    ),
                    new ZetaHttpClientBuilder("").disableServerValidation(true).logging(LogLevel.ALL, System.out::println),
                    null,
                    null
                ));
        }
    }

    static class Holder<T> {
        public T value;
    }

    private static String extractHost(String url) {
        try {
            return new URI(url).getHost();
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }
    }

    public String getToken(RuntimeConfig runtimeConfig) {
        konnektorClient.registerRuntimeConfig(runtimeConfig);
        try {
            Holder<String> tokenHolder = new Holder<>();
            try {
                Map<String, String> headers = new HashMap<>();

                String wsUrl = poppServerUrl.replace("https://", "wss://").replace("http://", "ws://") + "/ws/";
                String host = extractHost(poppServerUrl);

                WsClientExtension.ws(sdkClient, wsUrl,
                    builder -> {
                        builder.disableServerValidation(true);
                        return Unit.INSTANCE;
                    },
                    headers, session -> {
                        try {
                            PoppTokenProvider poppTokenProvider = new PoppTokenProvider(konnektorClient);
                            tokenHolder.value = poppTokenProvider.acquireToken(session, host);
                        } catch (Exception e) {
                            ZetaEreSdk.clear();
                            log.log(Level.SEVERE, "Get popp-token error", e);
                        } finally {
                            session.close();
                        }
                    });
            } catch (Exception e) {
                log.log(Level.SEVERE, "Get popp-token error", e);
            }
            return tokenHolder.value;
        } finally {
            konnektorClient.unregisterRuntimeConfig();
        }
    }
}