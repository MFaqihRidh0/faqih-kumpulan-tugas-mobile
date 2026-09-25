package com.its.tugas4sqlite.util;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class DateUtils {
    private static final SimpleDateFormat DB_DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
    private static final SimpleDateFormat DISPLAY_DATE_FORMAT = new SimpleDateFormat("dd MMM yyyy", new Locale("id", "ID"));

    public static String formatDisplayDate(String dbDate) {
        if (dbDate == null || dbDate.isEmpty()) return "-";
        try {
            Date date = DB_DATE_FORMAT.parse(dbDate);
            if (date != null) {
                return DISPLAY_DATE_FORMAT.format(date);
            }
        } catch (ParseException e) {
            // fallback
        }
        return dbDate;
    }

    public static boolean isOverdue(String dbDate) {
        if (dbDate == null || dbDate.isEmpty()) return false;
        try {
            Date date = DB_DATE_FORMAT.parse(dbDate);
            if (date != null) {
                // Bandingkan dengan hari ini pukul 00:00:00
                Date today = DB_DATE_FORMAT.parse(DB_DATE_FORMAT.format(new Date()));
                return date.before(today);
            }
        } catch (ParseException ignored) {
        }
        return false;
    }

    public static String getTodayDbDate() {
        return DB_DATE_FORMAT.format(new Date());
    }
}
