package de.ng.ahnenwin2gedcom.gedcom

import de.ng.ahnenwin2gedcom.hej.BeziehungsProperty
import de.ng.ahnenwin2gedcom.hej.HejBeziehung
import java.util.*

internal data class PartnerPair(val partner1: Int, val partner2: Int) {

    companion object {
        val NO_PARENTS = PartnerPair(0, 0)
    }

    constructor(hejBeziehung: HejBeziehung) :
            this(hejBeziehung.getRequiredInt(BeziehungsProperty.PARTNER_1),
                hejBeziehung.getRequiredInt(BeziehungsProperty.PARTNER_2))

    constructor(parentsRef: ParentsRef) : this(parentsRef.papaId, parentsRef.mamaId)

    override fun equals(other: Any?): Boolean {
        if (other === this) return true
        if (other !is PartnerPair) return false
        return setOf(partner1, partner2) == setOf(other.partner1, other.partner2)
    }

    override fun hashCode(): Int {
        return Objects.hashCode(setOf(partner1, partner2))
    }
}
