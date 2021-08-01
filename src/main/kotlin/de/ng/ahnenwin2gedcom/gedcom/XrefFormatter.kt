package de.ng.ahnenwin2gedcom.gedcom

import de.ng.ahnenwin2gedcom.log
import de.ng.ahnenwin2gedcom.logger

internal object XrefFormatter {

    fun format(id: Int): String {
        return "@$id@"
    }

    fun deformat(formattedXref: String?): Int? {
        if (formattedXref == null) return null
        val deformatted = formattedXref.replace("^@(.+)@$".toRegex(), "$1")
        return try {
            deformatted.toInt()
        } catch (e: NumberFormatException) {
            logger().error("Tried to deformat individual xref $formattedXref but got NumberFormatException.", e)
            null
        }
    }
}
