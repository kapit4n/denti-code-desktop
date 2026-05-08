package com.denticode.desktop.ui.common;

import java.math.BigDecimal;
import java.text.NumberFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.util.Locale;

public final class Formatters {

    private Formatters() {}

    public static String date(LocalDate d) {
        return d == null ? "" : d.format(DateTimeFormatter.ISO_LOCAL_DATE);
    }

    public static String dateTime(LocalDateTime d, Locale locale) {
        if (d == null) return "";
        return d.format(DateTimeFormatter.ofLocalizedDateTime(FormatStyle.MEDIUM, FormatStyle.SHORT)
                .withLocale(locale == null ? Locale.ENGLISH : locale));
    }

    public static String currency(BigDecimal amount, Locale locale) {
        if (amount == null) return "";
        NumberFormat fmt = NumberFormat.getCurrencyInstance(locale == null ? Locale.US : locale);
        return fmt.format(amount.doubleValue());
    }
}
