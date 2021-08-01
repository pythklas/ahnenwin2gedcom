package de.ng.ahnenwin2gedcom.gedcom

import de.ng.ahnenwin2gedcom.hej.*
import org.gedcom4j.model.Family
import java.util.function.Consumer

internal class FamilyMapper(private val sourceStore: SourceStore,
                            private val familyStore: FamilyStore,
                            private val individualStore: IndividualStore
                            ) {

    companion object {
        private fun addKirchlicheHochzeit(familyBuilder: FamilyBuilder, beziehung: HejBeziehung) {
            familyBuilder.marriageKirchlich(
                beziehung.getString(BeziehungsProperty.HOCHZEIT_TAG_KIRCHE),
                beziehung.getString(BeziehungsProperty.HOCHZEIT_MONAT_KIRCHE),
                beziehung.getString(BeziehungsProperty.HOCHZEIT_JAHR_KIRCHE),
                beziehung.getString(BeziehungsProperty.HOCHZEIT_ORT_KIRCHE),
                beziehung.getString(BeziehungsProperty.HOCHZEIT_QUELLE_KIRCHE),
                beziehung.getString(BeziehungsProperty.HOCHZEIT_TRAUZEUGEN_KIRCHE)
            )
        }

        private fun addStandesamtlicheHochzeit(familyBuilder: FamilyBuilder, beziehung: HejBeziehung) {
            familyBuilder.marriageStandesamtlich(
                beziehung.getString(BeziehungsProperty.HOCHZEIT_TAG_STANDESAMT),
                beziehung.getString(BeziehungsProperty.HOCHZEIT_MONAT_STANDESAMT),
                beziehung.getString(BeziehungsProperty.HOCHZEIT_JAHR_STANDESAMT),
                beziehung.getString(BeziehungsProperty.HOCHZEIT_ORT_STANDESAMT),
                beziehung.getString(BeziehungsProperty.HOCHZEIT_QUELLE_STANDESAMT),
                beziehung.getString(BeziehungsProperty.HOCHZEIT_TRAUZEUGEN_STANDESAMT)
            )
        }

        private fun addEngagement(familyBuilder: FamilyBuilder, beziehung: HejBeziehung) {
            familyBuilder.engagement(
                beziehung.getString(BeziehungsProperty.HOCHZEIT_TAG_KIRCHE),
                beziehung.getString(BeziehungsProperty.HOCHZEIT_MONAT_KIRCHE),
                beziehung.getString(BeziehungsProperty.HOCHZEIT_JAHR_KIRCHE),
                beziehung.getString(BeziehungsProperty.HOCHZEIT_ORT_KIRCHE),
                beziehung.getString(BeziehungsProperty.HOCHZEIT_QUELLE_KIRCHE),
                beziehung.getString(BeziehungsProperty.HOCHZEIT_TRAUZEUGEN_KIRCHE)
            )
        }

        private fun addAndereBeziehung(familyBuilder: FamilyBuilder, beziehung: HejBeziehung) {
            familyBuilder.andereBeziehung(
                beziehung.getString(BeziehungsProperty.HOCHZEIT_TAG_KIRCHE),
                beziehung.getString(BeziehungsProperty.HOCHZEIT_MONAT_KIRCHE),
                beziehung.getString(BeziehungsProperty.HOCHZEIT_JAHR_KIRCHE),
                beziehung.getString(BeziehungsProperty.HOCHZEIT_ORT_KIRCHE),
                beziehung.getString(BeziehungsProperty.HOCHZEIT_QUELLE_KIRCHE),
                beziehung.getString(BeziehungsProperty.HOCHZEIT_TRAUZEUGEN_KIRCHE)
            )
        }

        private fun addLebensgemeinschaft(familyBuilder: FamilyBuilder, beziehung: HejBeziehung) {
            familyBuilder.lebensgemeinschaft(
                beziehung.getString(BeziehungsProperty.HOCHZEIT_TAG_KIRCHE),
                beziehung.getString(BeziehungsProperty.HOCHZEIT_MONAT_KIRCHE),
                beziehung.getString(BeziehungsProperty.HOCHZEIT_JAHR_KIRCHE),
                beziehung.getString(BeziehungsProperty.HOCHZEIT_ORT_KIRCHE),
                beziehung.getString(BeziehungsProperty.HOCHZEIT_QUELLE_KIRCHE),
                beziehung.getString(BeziehungsProperty.HOCHZEIT_TRAUZEUGEN_KIRCHE)
            )
        }

        private fun addDivorce(familyBuilder: FamilyBuilder, beziehung: HejBeziehung) {
            familyBuilder.divorce(
                beziehung.getString(BeziehungsProperty.SCHEIDUNG_TAG),
                beziehung.getString(BeziehungsProperty.SCHEIDUNG_MONAT),
                beziehung.getString(BeziehungsProperty.SCHEIDUNG_JAHR),
                beziehung.getString(BeziehungsProperty.SCHEIDUNG_ORT),
                beziehung.getString(BeziehungsProperty.SCHEIDUNG_QUELLE)
            )
        }
    }

    fun map(hej: HejData): Map<String, Family> {
        createFamiliesFromParentsRef(hej.ahnen)
        createFamiliesFromBeziehungen(hej.beziehungen)
        addAsChildrenToFamilies(hej.ahnen)
        addFamilyEvents(hej)
        return familyStore.allWithXrefKey
    }

    private fun createFamiliesFromBeziehungen(beziehungen: Set<HejBeziehung>) {
        for (beziehung in beziehungen) createFamilyFromHejBeziehung(beziehung)
    }

    private fun createFamilyFromHejBeziehung(beziehung: HejBeziehung) {
        familyStore.createFamilyIfNotExists(beziehung)
    }

    private fun createFamiliesFromParentsRef(ahnen: Collection<HejAhne>) {
        ahnen.forEach { createFamilyFromParentsRef(it) }
    }

    private fun createFamilyFromParentsRef(ahne: HejAhne) {
        familyStore.createFamilyIfNotExists(ParentsRef(ahne))
    }

    private fun addFamilyEvents(hej: HejData) {
        hej.beziehungen.groupBy { PartnerPair(it) }
            .entries
            .filter { it.key != PartnerPair.NO_PARENTS }
            .forEach { addFamilyEvents(it.value.first()) }
    }

    private fun addFamilyEvents(beziehung: HejBeziehung) {
        val family = familyStore[beziehung] ?: return
        addFamilyEvents(family, beziehung)
    }

    private fun addFamilyEvents(family: Family, beziehung: HejBeziehung) {
        val verbindung = beziehung.verbindung
        val familyBuilder = FamilyBuilder(family, sourceStore, individualStore)
        addDivorce(familyBuilder, beziehung)
        when (verbindung) {
            Verbindung.EHESCHLIESSUNG -> {
                addKirchlicheHochzeit(familyBuilder, beziehung)
                addStandesamtlicheHochzeit(familyBuilder, beziehung)
            }
            Verbindung.VERLOBUNG -> addEngagement(familyBuilder, beziehung)
            Verbindung.ANDERE_BEZIEHUNG -> addAndereBeziehung(familyBuilder, beziehung)
            Verbindung.LEBENSGEMEINSCHAFT -> addLebensgemeinschaft(familyBuilder, beziehung)
            Verbindung.LEER, null -> {}
        }
    }

    private fun addAsChildrenToFamilies(ahnen: Collection<HejAhne>) {
        ahnen.groupBy { ParentsRef(it) }
            .entries
            .filter { it.key != ParentsRef.NO_PARENTS }
            .forEach { addChildren(it) }
    }

    private fun addChildren(children: Map.Entry<ParentsRef, List<HejAhne>>) {
        addChildren(children.key, children.value)
    }

    private fun addChildren(parentsRef: ParentsRef, children: List<HejAhne>) {
        val family = familyStore[parentsRef] ?: return
        val childIds = children.map { it.getRequiredInt(AhnenProperty.NUMMER) }
        FamilyBuilder(family, sourceStore, individualStore).addChildren(childIds)
    }
}
