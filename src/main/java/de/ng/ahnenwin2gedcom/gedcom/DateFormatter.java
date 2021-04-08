package de.ng.ahnenwin2gedcom.gedcom;

import de.ng.ahnenwin2gedcom.helperfunctions.StringFun;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.time.format.DateTimeParseException;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.function.Predicate;

class DateFormatter {
    private static final DateTimeFormatter dateTimeFormatter = new DateTimeFormatterBuilder()
            .parseCaseInsensitive()
            .appendPattern("dd MMM yyyy")
            .toFormatter()
            .withLocale(Locale.ENGLISH);

    private static final Predicate<Integer> isInMonthRange = month -> 0 < month && month < 13;

    static String format(String day, String month, String year) {
        String date = null;
        String formattedYear = formatYear(year);
        String formattedMonth = formatMonth(month);
        String formattedDay = formatDay(day);
        if (formattedYear != null) {
            date = formattedYear;
            if (formattedMonth != null) {
                date = formattedMonth + " " + date;
                if (formattedDay != null && canParseDate(formattedDay, formattedMonth, formattedYear)) {
                    date = formattedDay + " " + date;
                }
            }
        }
        return date;
    }

    static boolean hasInvalidStructure(String day, String month, String year) {
        String formattedDay = DateFormatter.formatDay(day);
        String formattedMonth = DateFormatter.formatMonth(month);
        String formattedYear = DateFormatter.formatYear(year);
        boolean dayDefinedAndInvalid = StringFun.notEmpty(day) && formattedDay == null;
        boolean monthDefinedAndInvalid = StringFun.notEmpty(month) && formattedMonth == null;
        boolean yearDefinedAndInvalid = StringFun.notEmpty(year) && formattedYear == null;
        boolean dayDefinedButNotYearOrMonth = StringFun.notEmpty(day) && (formattedMonth == null || formattedYear == null);
        boolean monthDefinedButNotYear = StringFun.notEmpty(month) && formattedYear == null;
        return dayDefinedAndInvalid
                || monthDefinedAndInvalid
                || yearDefinedAndInvalid
                || dayDefinedButNotYearOrMonth
                || monthDefinedButNotYear;
    }

    private static boolean canParseDate(String day, String month, String year) {
        String dateString = String.format("%s %s %s", day, month, year);
        try {
            LocalDate.parse(dateString, dateTimeFormatter);
        } catch (DateTimeParseException ignore) {
            return false;
        }
        return true;
    }

    static String formatDay(String day) {
        return parseIntegerValue(day)
                .map(intDay -> String.format("%02d", intDay))
                .orElse(null);
    }

    static String formatMonth(String monthNumber) {
        return parseIntegerValue(monthNumber)
                .filter(isInMonthRange)
                .map(intMonth -> DateFormat.MONTH_ABBREVIATIONS[intMonth - 1])
                .orElse(null);
    }

    static String formatYear(String year) {
        return parseIntegerValue(year)
                .map(intYear -> String.format("%04d", intYear))
                .orElse(null);
    }

    static String formatAsNote(String notePrefix, String day, String month, String year) {
        List<String> notes = new LinkedList<>();
        if (StringFun.notEmpty(day)) notes.add("Tag: " + day);
        if (StringFun.notEmpty(month)) notes.add("Monat: " + month);
        if (StringFun.notEmpty(year)) notes.add("Jahr: " + year);
        return notePrefix + ": " + String.join(", ", notes);
    }

    private static Optional<Integer> parseIntegerValue(String value) {
        if (StringFun.isEmpty(value)) return Optional.empty();
        Integer intValue;
        try {
            intValue = Integer.parseInt(value);
        }
        catch (NumberFormatException ignore) {
            intValue = null;
        }
        return Optional.ofNullable(intValue);
    }
}
