package de.ng.ahnenwin2gedcom.gedcom

import de.ng.ahnenwin2gedcom.hej.AhnenProperty
import de.ng.ahnenwin2gedcom.hej.HejAhne

internal data class ParentsRef(val papaId: Int, val mamaId: Int) {
    companion object {
        val NO_PARENTS = ParentsRef(0, 0)
    }

    constructor(hejAhne: HejAhne) :
            this(hejAhne.getRequiredInt(AhnenProperty.VATER),
                hejAhne.getRequiredInt(AhnenProperty.MUTTER))
}
