package de.ng.ahnenwin2gedcom.gedcom

internal object PlaceFormatter {
    fun format(place: String?): String? {
        return place?.replace(",".toRegex(), ";")
    }
}
