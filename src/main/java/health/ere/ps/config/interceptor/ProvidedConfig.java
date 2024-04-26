package health.ere.ps.config.interceptor;


import jakarta.annotation.Priority;
import jakarta.interceptor.InterceptorBinding;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

@InterceptorBinding
@Target({METHOD, TYPE})
@Retention(RUNTIME)
@Priority(600) // todo: same level as interceptor using it, is this correct?
public @interface ProvidedConfig {

}

