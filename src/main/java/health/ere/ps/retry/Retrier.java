package health.ere.ps.retry;

import org.jboss.logmanager.Level;

import java.util.List;
import java.util.Objects;
import java.util.concurrent.TimeUnit;
import java.util.function.Predicate;
import java.util.logging.Logger;

public class Retrier {

    private static final Logger log = Logger.getLogger(Retrier.class.getName());

    private Retrier() {
    }

    public static <T> T callAndRetry(
        List<Integer> retrySeconds,
        int retryPeriodMs,
        RetryAction<T> action,
        Predicate<T> predicate
    ) {
        T result = safeExecute(action);
        if (result != null && predicate.test(result)) {
            return result;
        }
        List<Integer> retries = retrySeconds.stream().filter(Objects::nonNull).sorted().toList();
        if (!retries.isEmpty()) {
            int k = 0;
            long start = System.currentTimeMillis();
            while (result == null || !predicate.test(result)) {
                Integer timeoutSec = retries.get(k++);
                if (k >= retries.size()) {
                    k = retries.size() - 1;
                }
                long delta = System.currentTimeMillis() - start;
                if (delta + timeoutSec * 1000 > retryPeriodMs) {
                    break;
                }
                try {
                    TimeUnit.SECONDS.sleep(timeoutSec);
                } catch (InterruptedException ignored) {
                }
                result = safeExecute(action);
            }
        }
        return result;
    }

    private static <T> T safeExecute(RetryAction<T> action) {
        try {
            return action.execute();
        } catch (Throwable t) {
            log.log(Level.SEVERE, "Error while executing retryable action", t);
        }
        return null;
    }
}
