package health.ere.ps.retry;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class RetrierTest {

    @Test
    public void actionWasExecutedCorrectNumberOfTimes() {
        RetryAction<Boolean> action = mock(RetryAction.class);
        when(action.execute()).thenReturn(false);
        long start = System.currentTimeMillis();
        Retrier.callAndRetry(List.of(1,1,2), 5000, action, bool -> bool);
        long delta = System.currentTimeMillis() - start;
        System.out.println("Took " + delta + "ms");
        verify(action, times(4)).execute();
    }
}