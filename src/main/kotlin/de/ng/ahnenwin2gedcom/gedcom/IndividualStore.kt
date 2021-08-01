package de.ng.ahnenwin2gedcom.gedcom

import de.ng.ahnenwin2gedcom.logger
import org.gedcom4j.model.Individual

internal class IndividualStore {
    private val store = mutableMapOf<String, Individual>()
    fun add(individual: Individual) {
        val xref = individual.xref
        if (xref == null) {
            logger().error("Individual $individual hat xref null. Bitte melden Sie den Fehler beim Entwickler.")
            return
        }
        if (store[xref] != null) {
            logger().error("Individual $individual mit xref $xref wurde bereits hinzugefuegt." +
                    " Bitte melden Sie den Fehler beim Entwickler.")
            return
        }
        store[xref] = individual
    }

    operator fun get(xref: String) = store[xref]

    val all
        get() = store
}
