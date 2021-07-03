package de.ng.ahnenwin2gedcom.hej

enum class Geschlecht(private vararg val csvValues: String) {
    MAENNLICH("m", "M"),
    WEIBLICH("w", "W"),
    UNBEKANNT;

    internal companion object {
        @JvmStatic
        fun findByCsvValue(csvValue: String?): Geschlecht {
            return values().find { csvValue?.trim() in it.csvValues} ?: UNBEKANNT
        }
    }

    fun opposite(): Geschlecht {
        return when(this) {
            MAENNLICH -> WEIBLICH
            WEIBLICH -> MAENNLICH
            UNBEKANNT -> UNBEKANNT
        }
    }

}
