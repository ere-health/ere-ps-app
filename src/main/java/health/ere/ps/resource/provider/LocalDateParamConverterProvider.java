package health.ere.ps.resource.provider;

import javax.ws.rs.ext.ParamConverter;
import javax.ws.rs.ext.ParamConverterProvider;
import javax.ws.rs.ext.Provider;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.time.LocalDate;
import java.util.Objects;

@Provider
public class LocalDateParamConverterProvider implements ParamConverterProvider {
    @Override
    public <T> ParamConverter<T> getConverter(Class<T> aClass, Type type, Annotation[] annotations) {
        if (LocalDate.class.equals(aClass)) {
            return new ParamConverter<T>() {
                @Override
                @SuppressWarnings("unchecked")
                public T fromString(String s) {
                    return (T) LocalDate.parse(s);
                }

                @Override
                public String toString(T t) {
                    return Objects.toString(t);
                }
            };
        } else {
            return null;
        }
    }
}
