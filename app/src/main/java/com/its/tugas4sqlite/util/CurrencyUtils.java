package com.its.tugas4sqlite.util;

import java.text.NumberFormat;
import java.util.Locale;

public class CurrencyUtils {
    private static final Locale INDONESIA = new Locale("id", "ID");

    public static String formatRupiah(double amount) {
        NumberFormat format = NumberFormat.getCurrencyInstance(INDONESIA);
        format.setMaximumFractionDigits(0);
        return format.format(amount);
    }
}
