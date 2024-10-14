package health.ere.ps.service.cetp.mapper;

import javax.xml.datatype.XMLGregorianCalendar;
import java.util.Date;

@SuppressWarnings("unused")
public class MapperUtils {

    public static Date calendarToDate(XMLGregorianCalendar calendar) {
        return calendar.toGregorianCalendar().getTime();
    }
}
