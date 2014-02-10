package se.vgregion.notifications.util;

import javax.xml.datatype.XMLGregorianCalendar;
import java.text.SimpleDateFormat;
import java.util.Locale;
import java.util.TimeZone;

/**
 * @author Patrik Bergström
 */
public class DateUtil {

    private static final SimpleDateFormat sdf = new SimpleDateFormat("EEE dd/MM HH:mm", new Locale("sv", "SE"));

    static {
        sdf.setTimeZone(TimeZone.getTimeZone("Europe/Stockholm"));
    }

    public static String xmlGregorianCalendarToNiceString(XMLGregorianCalendar calendar) {
        return sdf.format(calendar.toGregorianCalendar().getTime());
    }
}
