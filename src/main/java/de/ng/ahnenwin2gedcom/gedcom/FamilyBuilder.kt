package de.ng.ahnenwin2gedcom.gedcom

import de.ng.ahnenwin2gedcom.gedcom.DateFormatter.format
import de.ng.ahnenwin2gedcom.gedcom.DateFormatter.formatAsNote
import de.ng.ahnenwin2gedcom.gedcom.DateFormatter.hasInvalidStructure
import de.ng.ahnenwin2gedcom.helperfunctions.StringFun.isEmpty
import de.ng.ahnenwin2gedcom.helperfunctions.StringFun.notEmpty
import org.gedcom4j.model.*
import org.gedcom4j.model.enumerations.FamilyEventType

internal class FamilyBuilder(private val family: Family,
                             private val sourceStore: SourceStore,
                             private val individualStore: IndividualStore
                             ) {
    constructor(sourceStore: SourceStore, individualStore: IndividualStore) : this(Family(), sourceStore, individualStore)

    companion object {
        private enum class SpouseType {
            WIFE, HUSBAND
        }

        val typesWithRelevantSubtypes = setOf(FamilyEventType.MARRIAGE, FamilyEventType.EVENT)

        private fun attachPlace(event: FamilyEvent, placeName: String) {
            val place = Place()
            place.placeName = PlaceFormatter.format(placeName)
            event.place = place
        }

        private fun trauzeugenNote(trauzeugen: String?): String? {
            return if (isEmpty(trauzeugen)) null else "Trauzeugen: $trauzeugen"
        }
    }

    fun xref(xref: String): FamilyBuilder {
        family.xref = xref
        return this
    }

    fun wife(id: Int): FamilyBuilder {
        return spouse(SpouseType.WIFE, id)
    }

    fun husband(id: Int): FamilyBuilder {
        return spouse(SpouseType.HUSBAND, id)
    }

    private fun spouse(spouseType: SpouseType, id: Int): FamilyBuilder {
        if (id == 0) return this
        val spouse = individualStore[XrefFormatter.format(id)] ?: return this
        val spouseRef = individualReference(spouse)
        when (spouseType) {
            SpouseType.WIFE -> family.wife = spouseRef
            SpouseType.HUSBAND -> family.husband = spouseRef
        }
        IndividualBuilder(spouse, sourceStore).spouseToFamilyLink(familySpouse())
        return this
    }

    fun addChildren(ids: Collection<Int>): FamilyBuilder {
        val childRefs = ids.map { XrefFormatter.format(it) }
            .map { individualStore[it] }
            .map { IndividualReference(it) }
        family.getChildren(true).addAll(childRefs)
        for (childRef in childRefs) {
            val individual = childRef.individual
            // set back reference to this family
            IndividualBuilder(individual, sourceStore)
                .childToFamilyLink(familyChild())
        }
        return this
    }

    private fun familyChild(): FamilyChild {
        val familyChild = FamilyChild()
        familyChild.family = family
        return familyChild
    }

    private fun familySpouse(): FamilySpouse {
        val familySpouse = FamilySpouse()
        familySpouse.family = family
        return familySpouse
    }

    fun marriageKirchlich(
        day: String?, month: String?, year: String?,
        placeName: String?, source: String?, trauzeugen: String?
    ): FamilyBuilder {
        setEvent(
            FamilyEventType.MARRIAGE, "Hochzeit kirchlich", day, month, year,
            placeName, source, trauzeugenNote(trauzeugen)
        )
        return this
    }

    fun marriageStandesamtlich(
        day: String?, month: String?, year: String?,
        placeName: String?, source: String?, trauzeugen: String?
    ): FamilyBuilder {
        setEvent(
            FamilyEventType.MARRIAGE, "Hochzeit standesamtlich", day, month, year,
            placeName, source, trauzeugenNote(trauzeugen)
        )
        return this
    }

    fun engagement(
        day: String?, month: String?, year: String?,
        placeName: String?, source: String?, trauzeugen: String?
    ): FamilyBuilder {
        setEvent(
            FamilyEventType.ENGAGEMENT, "Verlobung", day, month, year,
            placeName, source, trauzeugenNote(trauzeugen)
        )
        return this
    }

    fun lebensgemeinschaft(
        day: String?, month: String?, year: String?,
        placeName: String?, source: String?, trauzeugen: String?
    ): FamilyBuilder {
        setEvent(
            FamilyEventType.EVENT, "Lebensgemeinschaft", day, month, year,
            placeName, source, trauzeugenNote(trauzeugen)
        )
        return this
    }

    fun andereBeziehung(
        day: String?, month: String?, year: String?,
        placeName: String?, source: String?, trauzeugen: String?
    ): FamilyBuilder {
        setEvent(
            FamilyEventType.EVENT, "andere Beziehung", day, month, year,
            placeName, source, trauzeugenNote(trauzeugen)
        )
        return this
    }

    fun divorce(day: String?, month: String?, year: String?, placeName: String?, source: String?): FamilyBuilder {
        setEvent(FamilyEventType.DIVORCE, "Scheidung", day, month, year, placeName, source)
        return this
    }

    fun build() = family

    private fun individualReference(individual: Individual) = IndividualReference(individual)

    private fun setEvent(
        type: FamilyEventType, subType: String, day: String?,
        month: String?, year: String?, placeName: String?,
        source: String?, vararg notes: String?
    ) {
        val date = format(day, month, year)
        val notesNotNull = notes.filterNotNull()
        val dateIsWellDefined = date != null
        val placeNameExists = notEmpty(placeName)
        val originalDateHasInvalidStructure = hasInvalidStructure(day, month, year)
        val sourceExists = notEmpty(source)
        val notesExist = notesNotNull.isNotEmpty()
        val atLeastOneEventAttributeIsWellDefined = (dateIsWellDefined
                || placeNameExists || originalDateHasInvalidStructure || sourceExists || notesExist)

        if (!atLeastOneEventAttributeIsWellDefined) return

        val event = FamilyEvent()
        event.type = type

        if (dateIsWellDefined) event.setDate(date)
        if (placeNameExists) attachPlace(event, placeName!!)
        if (originalDateHasInvalidStructure) attachDateNotes(event, subType, day, month, year)
        if (sourceExists) attachSource(event, source!!)
        if (notesExist) {
            for (note in notesNotNull) attachNote(event, note)
        }
        if (typesWithRelevantSubtypes.contains(type)) event.setSubType(subType)

        family.getEvents(true).add(event)
    }

    private fun attachDateNotes(event: FamilyEvent, notePrefix: String, day: String?, month: String?, year: String?) {
        attachNote(event, formatAsNote(notePrefix, day, month, year))
    }

    private fun attachSource(event: FamilyEvent, sourceTitle: String) {
        val citation = CitationWithSource()
        val source = sourceStore.createSource(sourceTitle)
        citation.source = source
        event.getCitations(true).add(citation)
    }

    private fun attachNote(event: FamilyEvent, note: String) {
        event.getNoteStructures(true).add(NoteStructureFactory.create(note))
    }
}
