package de.ng.ahnenwin2gedcom.gedcom

internal object PlaceFormatter {
    fun format(place: String?) = place?.replace(",".toRegex(), ";")
}
