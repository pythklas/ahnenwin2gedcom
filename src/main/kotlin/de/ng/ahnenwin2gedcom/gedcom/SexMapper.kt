package de.ng.ahnenwin2gedcom.gedcom

import de.ng.ahnenwin2gedcom.hej.Geschlecht

internal object SexMapper {
    fun toGedcomSex(geschlecht: Geschlecht?): String? {
        return when(geschlecht) {
            Geschlecht.MAENNLICH -> "M"
            Geschlecht.WEIBLICH -> "F"
            else -> null
        }
    }

    fun toHejGeschlecht(gedcomSex: String?): Geschlecht {
        return when(gedcomSex) {
            "M" -> Geschlecht.MAENNLICH
            "F" -> Geschlecht.WEIBLICH
            else -> Geschlecht.UNBEKANNT
        }
    }
}
