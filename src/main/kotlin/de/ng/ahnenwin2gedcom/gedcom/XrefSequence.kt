package de.ng.ahnenwin2gedcom.gedcom

internal class XrefSequence {
    private var sequence = 0
    fun next(): String {
        return XrefFormatter.format(sequence++)
    }
}
