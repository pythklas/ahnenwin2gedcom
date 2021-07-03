package de.ng.ahnenwin2gedcom.gedcom

import de.ng.ahnenwin2gedcom.gedcom.DateFormatter.format
import de.ng.ahnenwin2gedcom.gedcom.DateFormatter.formatAsNote
import de.ng.ahnenwin2gedcom.gedcom.DateFormatter.hasInvalidStructure
import de.ng.ahnenwin2gedcom.hej.Geschlecht
import de.ng.ahnenwin2gedcom.helperfunctions.StringFun.isEmpty
import de.ng.ahnenwin2gedcom.helperfunctions.StringFun.notEmpty
import org.gedcom4j.model.*
import org.gedcom4j.model.enumerations.IndividualAttributeType
import org.gedcom4j.model.enumerations.IndividualEventType

internal class IndividualBuilder(private val individual: Individual, private val sourceStore: SourceStore) {
    constructor(sourceStore: SourceStore) : this(Individual(), sourceStore)

    companion object {
        private fun attachPlace(event: IndividualEvent, placeName: String) {
            val place = Place()
            place.placeName = PlaceFormatter.format(placeName)
            event.place = place
        }
    }

    fun xref(id: Int): IndividualBuilder {
        individual.xref = XrefFormatter.format(id)
        return this
    }

    fun name(givenName: String?, surname: String?, nickname: String?, schreibweise: String?): IndividualBuilder {
        val name = PersonalName()

        if (notEmpty(givenName)) name.setGivenName(givenName)
        if (notEmpty(surname)) name.setSurname(surname)
        if (notEmpty(nickname)) name.setNickname(nickname)

        if (notEmpty(schreibweise)) {
            val noteStructure = NoteStructureFactory.create("Schreibweise: $schreibweise")
            name.getNoteStructures(true).add(noteStructure)
        }

        if (notEmpty(givenName) || notEmpty(surname)) {
            name.basic = "$givenName /$surname/"
            individual.getNames(true).add(name)
        }

        return this
    }

    fun aliveAsNote(alive: String?): IndividualBuilder {
        addNote("Lebt", alive)
        return this
    }

    fun hofnameAsNote(hofname: String?): IndividualBuilder {
        addNote("Hofname", hofname)
        return this
    }

    fun phoneNumber(phoneNumber: String?): IndividualBuilder {
        addNote("Telefon", phoneNumber)
        return this
    }

    fun childToFamilyLink(familyWhereChild: FamilyChild?): IndividualBuilder {
        if (familyWhereChild == null) return this
        individual.getFamiliesWhereChild(true).add(familyWhereChild)
        return this
    }

    fun spouseToFamilyLink(familySpouse: FamilySpouse?): IndividualBuilder {
        if (familySpouse == null) return this
        individual.getFamiliesWhereSpouse(true).add(familySpouse)
        return this
    }

    fun address(
        address1: String?, address2: String?, addresszusatz: String?,
        postalCode: String?, city: String?
    ): IndividualBuilder {
        val address1Exists = notEmpty(address1)
        val address2Exists = notEmpty(address2)
        val addresszusatzExists = notEmpty(addresszusatz)
        val postalCodeExists = notEmpty(postalCode)
        val cityExists = notEmpty(city)
        val someAddressAttributeIsWellDefined = address1Exists ||
                address2Exists || addresszusatzExists || postalCodeExists || cityExists

        if (!someAddressAttributeIsWellDefined) return this

        val addressAsNote = mutableListOf<String>()

        if (address1Exists) addressAsNote.add("Adresse 1: $address1")
        if (address2Exists) addressAsNote.add("Adresse 2: $address2")
        if (addresszusatzExists) addressAsNote.add("Adresszusatz: $addresszusatz")
        if (postalCodeExists) addressAsNote.add("PLZ: $postalCode")
        if (cityExists) addressAsNote.add("Ort: $city")

        addResidence(addressAsNote)

        return this
    }

    fun sex(geschlecht: Geschlecht?): IndividualBuilder {
        if (geschlecht == null || geschlecht == Geschlecht.UNBEKANNT) return this
        individual.setSex(SexMapper.toGedcomSex(geschlecht))
        return this
    }

    fun religion(religion: String?): IndividualBuilder {
        addAttribute(IndividualAttributeType.RELIGIOUS_AFFILIATION, religion)
        return this
    }

    fun occupation(occupation: String?): IndividualBuilder {
        addAttribute(IndividualAttributeType.OCCUPATION, occupation)
        return this
    }

    fun birth(day: String?, month: String?, year: String?, place: String?, source: String?): IndividualBuilder {
        setEvent(IndividualEventType.BIRTH, "Geburtsdatum", day, month, year, place, source, null)
        return this
    }

    fun baptism(
        day: String?, month: String?, year: String?,
        godParent: String?, place: String?, source: String?
    ): IndividualBuilder {
        val note = if (notEmpty(godParent)) "Taufpate: $godParent" else null
        setEvent(IndividualEventType.BAPTISM, "Taufdatum", day, month, year, place, source, null, note)
        return this
    }

    fun death(
        day: String?, month: String?, year: String?,
        place: String?, source: String?, causeOfDeath: String?
    ): IndividualBuilder {
        setEvent(IndividualEventType.DEATH, "Sterbedatum", day, month, year, place, source, causeOfDeath)
        return this
    }

    fun burial(day: String?, month: String?, year: String?, place: String?, source: String?): IndividualBuilder {
        setEvent(IndividualEventType.BURIAL, "Beerdigungsdatum", day, month, year, place, source, null)
        return this
    }

    fun residence(placeName: String?): IndividualBuilder {
        addWohnorte(placeName)
        return this
    }

    fun source(source: String?): IndividualBuilder {
        if (isEmpty(source)) return this
        val citation = CitationWithSource()
        citation.source = sourceStore.createSource(source!!)
        individual.getCitations(true).add(citation)
        return this
    }

    fun textAsNote(text: Array<String?>?): IndividualBuilder {
        if (text == null) return this
        val textNotNull = text.filterNotNull().toTypedArray()
        if (textNotNull.isEmpty()) return this
        val note = NoteStructureFactory.create(*textNotNull)
        individual.getNoteStructures(true).add(note)
        return this
    }

    fun build(): Individual {
        return individual
    }

    private fun setEvent(
        type: IndividualEventType, notePrefix: String, day: String?, month: String?, year: String?,
        placeName: String?, source: String?, cause: String?, vararg notes: String?
    ) {
        val notNullNotes = notes.filterNotNull().toTypedArray()
        val date = format(day, month, year)
        val dateIsWellDefined = date != null
        val placeNameExists = notEmpty(placeName)
        val originalDateHasInvalidStructure = hasInvalidStructure(day, month, year)
        val sourceExists = notEmpty(source)
        val causeExists = notEmpty(cause)
        val notesNotEmpty = notNullNotes.isNotEmpty()
        val someEventAttributeIsWellDefined = dateIsWellDefined ||
                placeNameExists || originalDateHasInvalidStructure || sourceExists || notesNotEmpty

        if (!someEventAttributeIsWellDefined) return

        val event = IndividualEvent()
        event.type = type

        if (dateIsWellDefined) event.setDate(date)
        if (placeNameExists) attachPlace(event, placeName!!)
        if (originalDateHasInvalidStructure) attachDateNotes(event, notePrefix, day, month, year)
        if (sourceExists) attachCitation(event, source!!)
        if (causeExists) event.setCause(cause)
        attachNotes(event, *notNullNotes)
        individual.getEvents(true).add(event)
    }

    private fun attachDateNotes(event: IndividualEvent, notePrefix: String, day: String?, month: String?, year: String?) {
        attachNote(event, formatAsNote(notePrefix, day, month, year))
    }

    private fun attachNotes(event: IndividualEvent, vararg notes: String) {
        for (note in notes) attachNote(event, note)
    }

    private fun attachCitation(event: IndividualEvent, source: String) {
        val citation = CitationWithSource()
        citation.source = sourceStore.createSource(source)
        event.getCitations(true).add(citation)
    }

    private fun attachNote(event: IndividualEvent, note: String) {
        event.getNoteStructures(true).add(NoteStructureFactory.create(note))
    }

    private fun addNote(notePrefix: String, note: String?) {
        if (isEmpty(note)) return
        val noteStructure = NoteStructureFactory.create("$notePrefix: $note")
        individual.getNoteStructures(true).add(noteStructure)
    }

    private fun addAttribute(type: IndividualAttributeType, description: String?) {
        if (isEmpty(description)) return
        val attribute = IndividualAttribute()
        attribute.type = type
        attribute.setDescription(description)
        individual.getAttributes(true).add(attribute)
    }

    private fun addWohnorte(placeName: String?) {
        if (isEmpty(placeName)) return
        val attribute = IndividualAttribute()
        attribute.type = IndividualAttributeType.RESIDENCE
        val place = Place()
        place.placeName = PlaceFormatter.format(placeName)
        attribute.place = place
        attribute.setSubType("Wohnorte")
        individual.getAttributes(true).add(attribute)
    }

    private fun addResidence(addressAsNote: List<String>?) {
        if (addressAsNote == null) return
        val attribute = IndividualAttribute()
        attribute.type = IndividualAttributeType.RESIDENCE
        val noteStructure = NoteStructureFactory.create(*addressAsNote.toTypedArray())
        attribute.getNoteStructures(true).add(noteStructure)
        attribute.setSubType("Adresse")
        individual.getAttributes(true).add(attribute)
    }
}
