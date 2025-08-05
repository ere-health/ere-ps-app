package health.ere.ps.service.health.check;

import health.ere.ps.config.RuntimeConfig;
import io.quarkus.runtime.StartupEvent;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.Observes;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.InputStream;
import java.util.Map;
import java.util.Properties;

@ApplicationScoped
public class GitCheck implements Check {

    private static final Logger log = LoggerFactory.getLogger(GitCheck.class.getName());

    private static final String GIT_COMMIT_ID = "git-commit";
    private static final String BUILD_TIME = "build-time";

    private static final Properties properties = new Properties();
    static {
        try (InputStream inputStream = GitCheck.class.getResourceAsStream("/git.properties")) {
            properties.load(inputStream);
        } catch (Exception e) {
            log.warn("Error while loading git.properties:" + e.getMessage());
        }
    }

    void onStart(@Observes StartupEvent ev) {
        log.info("{}: {}", BUILD_TIME, properties.getProperty("git.build.time"));
        log.info("{}: {}", GIT_COMMIT_ID, properties.getProperty("git.commit.id"));
    }

    @Override
    public String getName() {
        return GIT_CHECK;
    }

    @Override
    public Status getStatus(RuntimeConfig runtimeConfig) {
        return Status.Up200;
    }

    @Override
    public Map<String, String> getData(RuntimeConfig runtimeConfig) {
        return Map.of(
            BUILD_TIME, properties.getProperty("git.build.time"),
            GIT_COMMIT_ID, properties.getProperty("git.commit.id")
        );
    }
}
