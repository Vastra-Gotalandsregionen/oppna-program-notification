package se.vgregion.notifications.util;

import org.junit.Test;

import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;

import static org.junit.Assert.assertEquals;

/**
 * @author Patrik Bergström
 */
public class DateUtilTest {

    @Test
    public void testXmlGregorianCalendarToNiceString() throws Exception {
        XMLGregorianCalendar xmlGregorianCalendar = DatatypeFactory.newInstance().newXMLGregorianCalendar();

        xmlGregorianCalendar.setYear(2013);
        xmlGregorianCalendar.setMonth(10);
        xmlGregorianCalendar.setDay(24);
        xmlGregorianCalendar.setHour(16);
        xmlGregorianCalendar.setMinute(30);

        String s = DateUtil.xmlGregorianCalendarToNiceString(xmlGregorianCalendar);

        assertEquals("to 24/10 16:30", s);
    }
}
