package de.ng.ahnenwin2gedcom.gedcom

import de.ng.ahnenwin2gedcom.helperfunctions.StringFun.notEmpty
import java.time.LocalDate
import java.time.format.DateTimeFormatterBuilder
import java.time.format.DateTimeParseException
import java.util.*

internal object DateFormatter {

    private val dateTimeFormatter = DateTimeFormatterBuilder()
        .parseCaseInsensitive()
        .appendPattern("d MMM y")
        .toFormatter()
        .withLocale(Locale.ENGLISH)

    @JvmStatic
    fun format(day: String?, month: String?, year: String?): String? {
        var date: String? = null
        val formattedYear = year?.let { formatYear(it) }
        val formattedMonth = month?.let { formatMonth(it) }
        val formattedDay = day?.let { formatDay(it) }
        if (formattedYear != null) {
            date = formattedYear
            if (formattedMonth != null) {
                date = "$formattedMonth $date"
                if (formattedDay != null && canParseDate(formattedDay, formattedMonth, formattedYear)) {
                    date = "$formattedDay $date"
                }
            }
        }
        return date
    }

    @JvmStatic
    fun hasInvalidStructure(day: String?, month: String?, year: String?): Boolean {
        val formattedDay = day?.let { formatDay(it) }
        val formattedMonth = month?.let { formatMonth(it) }
        val formattedYear = year?.let { formatYear(it) }
        val dayDefinedAndInvalid = notEmpty(day) && formattedDay == null
        val monthDefinedAndInvalid = notEmpty(month) && formattedMonth == null
        val yearDefinedAndInvalid = notEmpty(year) && formattedYear == null
        val dayDefinedButNotYearOrMonth = notEmpty(day) && (formattedMonth == null || formattedYear == null)
        val monthDefinedButNotYear = notEmpty(month) && formattedYear == null
        return (dayDefinedAndInvalid
                || monthDefinedAndInvalid
                || yearDefinedAndInvalid
                || dayDefinedButNotYearOrMonth
                || monthDefinedButNotYear)
    }

    private fun canParseDate(day: String, month: String, year: String): Boolean {
        val dateString = "$day $month $year"
        try {
            LocalDate.parse(dateString, dateTimeFormatter)
        } catch (ignore: DateTimeParseException) {
            return false
        }
        return true
    }

    private fun formatDay(day: String): String? {
        return parseIntegerValue(day)?.toString()
    }

    private fun formatMonth(monthNumber: String): String? {
        val monthInt = parseIntegerValue(monthNumber).takeIf { it in 1..12 } ?: return null
        return DateFormat.MONTH_ABBREVIATIONS[monthInt - 1]
    }

    private fun formatYear(year: String): String? {
        return parseIntegerValue(year)?.toString()
    }

    @JvmStatic
    fun formatAsNote(notePrefix: String, day: String?, month: String?, year: String?): String {
        val notes: MutableList<String> = LinkedList()
        if (notEmpty(day)) notes.add("Tag: $day")
        if (notEmpty(month)) notes.add("Monat: $month")
        if (notEmpty(year)) notes.add("Jahr: $year")
        return notePrefix + ": " + java.lang.String.join(", ", notes)
    }

    private fun parseIntegerValue(value: String): Int? {
        return try {
            value.toInt()
        } catch (ignore: NumberFormatException) {
            null
        }
    }
}
