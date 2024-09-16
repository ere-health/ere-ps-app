package health.ere.ps.retry;

public interface RetryAction<T> {

    T execute();
}
