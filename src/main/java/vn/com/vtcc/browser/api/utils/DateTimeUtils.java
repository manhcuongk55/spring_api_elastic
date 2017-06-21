package vn.com.vtcc.browser.api.utils;


import org.joda.time.DateTime;
import org.joda.time.LocalDateTime;

/**
 * Created by giang on 07/06/2017.
 */
public class DateTimeUtils {
    public static String getTimeNow(String format) {
        LocalDateTime localDateTime = new LocalDateTime();
        return localDateTime.toString();
    }

    public static String getPreviousDate(int previousDate) {
        DateTime lastWeek = new DateTime().minusDays(7);
        return lastWeek.toString();
    }
}
