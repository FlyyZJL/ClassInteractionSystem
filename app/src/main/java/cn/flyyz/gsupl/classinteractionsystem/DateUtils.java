package cn.flyyz.gsupl.classinteractionsystem;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class DateUtils {
    private static final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.CHINA);
    private static final SimpleDateFormat shortDateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.CHINA);

    public static String formatDate(Date date) {
        if (date == null) return "";
        return dateFormat.format(date);
    }

    public static String formatShortDate(Date date) {
        if (date == null) return "";
        return shortDateFormat.format(date);
    }
}