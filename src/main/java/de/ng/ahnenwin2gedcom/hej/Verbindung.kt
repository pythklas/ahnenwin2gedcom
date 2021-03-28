package de.ng.ahnenwin2gedcom.hej

enum class Verbindung(private val csvValue: String) {
    ANDERE_BEZIEHUNG("andere Beziehung"),
    EHESCHLIESSUNG("Eheschliessung"),
    LEBENSGEMEINSCHAFT("Lebensgemeinschaft"),
    VERLOBUNG("Verlobung"),
    LEER("");

    internal companion object {
        @JvmStatic
        fun findByCsvValue(csvValue: String?): Verbindung {
            val trimmedCsvValue = csvValue?.trim () ?: ""
            return values().firstOrNull{ it.csvValue == trimmedCsvValue } ?: LEER
        }
    }
}
