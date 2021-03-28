package de.ng.ahnenwin2gedcom.gedcom

import de.ng.ahnenwin2gedcom.hej.BeziehungsProperty
import de.ng.ahnenwin2gedcom.hej.Geschlecht
import de.ng.ahnenwin2gedcom.hej.HejBeziehung
import de.ng.ahnenwin2gedcom.logger
import org.gedcom4j.model.Family
import org.gedcom4j.model.Individual
import org.gedcom4j.model.StringWithCustomFacts

internal class FamilyStore(private val sourceStore: SourceStore, private val individualStore: IndividualStore) {

    companion object {
        private data class HusbandWifeRef(val husbandId: Int, val wifeId: Int)
    }

    private val familySequence = XrefSequence()
    private val store = mutableMapOf<PartnerPair, Family>()

    /**
     * This method needs to be called first.
     */
    fun createFamilyIfNotExists(parentsRef: ParentsRef): Family? {
        val partnerPair = PartnerPair(parentsRef)
        if (partnerPair == PartnerPair.NO_PARENTS) {
            return null
        }
        return store[partnerPair] ?:
            store.putIfAbsent(partnerPair,
                FamilyBuilder(sourceStore, individualStore)
                .xref(familySequence.next())
                .husband(parentsRef.papaId)
                .wife(parentsRef.mamaId)
                .build())
    }

    /**
     * This method needs to be called after [createFamilyIfNotExists]
     * has been called for all children with parents.
     */
    fun createFamilyIfNotExists(hejBeziehung: HejBeziehung): Family? {
        val partnerPair = PartnerPair(hejBeziehung)
        if (partnerPair == PartnerPair.NO_PARENTS) {
            logger().error("Die hejBeziehung $hejBeziehung hat nicht alle Eltern als Partner angegeben." +
                    " Das hätte nicht passieren dürfen." +
                    " Bitte an den Entwickler melden.")
            return null
        }
        val family = store[partnerPair]
        if (family != null) return family
        val husbandWifeRef = husbandWifeRef(hejBeziehung) ?: return null
        return store.putIfAbsent(partnerPair,
            FamilyBuilder(sourceStore, individualStore)
            .xref(familySequence.next())
            .husband(husbandWifeRef.husbandId)
            .wife(husbandWifeRef.wifeId)
            .build())
    }

    private fun husbandWifeRef(hejBeziehung: HejBeziehung): HusbandWifeRef? {
        val partner1Id = hejBeziehung.getInt(BeziehungsProperty.PARTNER_1)
        val partner2Id = hejBeziehung.getInt(BeziehungsProperty.PARTNER_2)
        val partner1Xref = XrefFormatter.format(partner1Id)
        val partner2Xref = XrefFormatter.format(partner2Id)
        val partner1 = individualStore[partner1Xref]
        val partner2 = individualStore[partner2Xref]

        if (partner1Id == 0 && partner2Id != 0) {
            return husbandWifeRef(partner2)
        }

        if (partner2Id == 0 && partner1Id != 0) {
            return husbandWifeRef(partner1)
        }

        if (partner1 == null || partner2 == null) {
            logger().error("Es wurden noch keine Gedcom-Individuals für Partner1 (ID: $partner1Xref) oder" +
                    " Partner2 (ID: $partner2Xref) angelegt. Ihre Beziehung kann nicht übertragen werden." +
                    " Bitte melden Sie das Problem an den Entwicklas.")
            return null
        }

        var sex1 = partner1.sex?.value
        var sex2 = partner2.sex?.value

        if (sex1 == null && sex2 == null) {
            sex1 = SexMapper.toGedcomSex(Geschlecht.MAENNLICH)
            sex2 = SexMapper.toGedcomSex(Geschlecht.WEIBLICH)
            partner1.sex = StringWithCustomFacts(sex1)
            partner2.sex = StringWithCustomFacts(sex2)
            logger().error("Die Partner $partner1 (ID $partner1Id) und $partner2 (ID $partner2Id) haben unbekanntes Geschlecht." +
                    " Um die Beziehung nach Gedcom zu übertragen, wird ihnen willkürlich ein Geschlecht zugeordnet." +
                    " $partner1 wird als männlich, $partner2 als weiblich übertragen." +
                    " Das Problem sollte im Ziel-Ahnenprogramm behoben werden.")
        }

        if (sex1 == null) sex1 = SexMapper.toGedcomSex(SexMapper.toHejGeschlecht(sex2).opposite())
        if (sex2 == null) sex2 = SexMapper.toGedcomSex(SexMapper.toHejGeschlecht(sex1).opposite())

        if (sex1 == sex2) {
            sex1 = SexMapper.toGedcomSex(SexMapper.toHejGeschlecht(sex2).opposite())
            partner1.sex = StringWithCustomFacts(sex1)
            logger().error("Die Partner $partner1 (ID $partner1Id) und $partner2 (ID $partner2Id)" +
                    " haben das gleiche Geschlecht ($sex2). Um die Beziehung nach Gedcom zu übertragen, " +
                    " wurde das Geschlecht von $partner1 zu $sex1 geändert." +
                    " Das Problem sollte im Ziel-Ahnenprogramm behoben werden.")
        }

        return when (SexMapper.toHejGeschlecht(sex1)) {
            Geschlecht.MAENNLICH -> HusbandWifeRef(partner1Id, partner2Id)
            Geschlecht.WEIBLICH -> HusbandWifeRef(partner2Id, partner1Id)
            Geschlecht.UNBEKANNT -> {
                logger().error("Geschlecht von $partner1 ist ${Geschlecht.UNBEKANNT}." +
                        " Das hätte nicht passieren dürfen." +
                        " Bitte melden Sie das Problem an den Entwickler." )
                null
            }
        }
    }

    private fun husbandWifeRef(individual: Individual?): HusbandWifeRef? {
        if (individual == null) {
            logger().error("Es wurde versucht eine Family für ein nicht existierendes Individual anzulegen." +
                    " Das hätte nicht passieren dürfen." +
                    " Bitte melden Sie das Problem an den Entwickler.")
            return null
        }
        val id = XrefFormatter.deformat(individual.xref)
        if (id == null || id == 0) {
            logger().error("Es wurde versucht, ein Individual mit ID 0 zu erzeugen." +
                    " Das hätte nicht passieren dürfen." +
                    " Bitte melden Sie das Problem an den Entwickler.")
            return null
        }
        val sex = individual.sex?.value
        return when (SexMapper.toHejGeschlecht(sex)) {
            Geschlecht.MAENNLICH -> HusbandWifeRef(id, 0)
            Geschlecht.WEIBLICH -> HusbandWifeRef(0, id)
            Geschlecht.UNBEKANNT -> {
                logger().error("Das Geschlecht von $individual ist unbekannt." +
                        " Das hätte nicht passierne dürfen." +
                        " Bitte melden Sie das Problem an den Entwickler.")
                null
            }
        }
    }

    operator fun get(hejBeziehung: HejBeziehung): Family? {
        return store[PartnerPair(hejBeziehung)]
    }

    operator fun get(parentsRef: ParentsRef): Family? {
        return store[PartnerPair(parentsRef.papaId, parentsRef.mamaId)]
    }

    val allWithXrefKey: Map<String, Family>
        get() = store.values.associateBy { it.xref }
}
