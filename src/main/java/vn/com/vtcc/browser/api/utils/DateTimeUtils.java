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

    public static DateTime getPreviousTime(String timeFormat, int difference) {
        DateTime result = new DateTime();
        switch (timeFormat) {
            case "date" :
                result = new DateTime().minusDays(difference);
                break;
            case "hour" :
                result = new DateTime().minusHours(difference);
                break;
            case "minute" :
                result = new DateTime().minusMinutes(difference);
                break;
        }
        return result;
    }

    public static long convertDateTimeToUnixTimestamp(DateTime time) {
        return time.toDateTime().getMillis();
    }
}
