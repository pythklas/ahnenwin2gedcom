package de.ng.ahnenwin2gedcom.gedcom;

import de.ng.ahnenwin2gedcom.csv.Geschlecht;
import de.ng.ahnenwin2gedcom.helperfunctions.StringFun;
import org.gedcom4j.model.*;
import org.gedcom4j.model.enumerations.IndividualAttributeType;
import org.gedcom4j.model.enumerations.IndividualEventType;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;

class IndividualBuilder {
    private final Individual individual = new Individual();
    private final NoteStore noteStore;
    private final SourceStore sourceStore;

    IndividualBuilder(NoteStore noteStore, SourceStore sourceStore) {
        this.noteStore = noteStore;
        this.sourceStore = sourceStore;
    }

    /**
     * Constructor for creating dummy individuals only.
     */
    private IndividualBuilder() {
        noteStore = new NoteStore();
        sourceStore = new SourceStore();
    }

    static Individual buildDummy(int id) {
        return new IndividualBuilder().xref(id).build();
    }

    IndividualBuilder xref(int id) {
        this.individual.setXref(XrefFormatter.format(id));
        return this;
    }

    IndividualBuilder name(String givenName, String surname, String nickname, String schreibweise) {
        PersonalName name = new PersonalName();
        if (StringFun.notEmpty(givenName)) {
            name.setGivenName(givenName);
        }
        if (StringFun.notEmpty(surname)) {
            name.setSurname(surname);
        }
        if (StringFun.notEmpty(nickname)) {
            name.setNickname(nickname);
        }
        if (StringFun.notEmpty(schreibweise)) {
            String note = String.format("Schrweibweise: %s", schreibweise);
            var noteStructure = createNote(note);
            name.getNoteStructures(true).add(noteStructure);
        }
        if (StringFun.notEmpty(givenName) || StringFun.notEmpty(surname)) {
            name.setBasic(String.format("%s /%s/", givenName, surname));
            individual.getNames(true).add(name);
        }
        return this;
    }

    IndividualBuilder aliveAsNote(String alive) {
        addNote("Lebt", alive);
        return this;
    }

    IndividualBuilder adoptedAsNote(String adopted) {
        //TODO: man kann family child Beziehungen erstellen und da ein adopted-Flag setzen.
        addNote("Adoptiert", adopted);
        return this;
    }

    IndividualBuilder address(String address1, String address2, String address3, String postalCode, String city, String hausnummer) {
        boolean address1Exists = StringFun.notEmpty(address1);
        boolean address2Exists = StringFun.notEmpty(address2);
        boolean address3Exists = StringFun.notEmpty(address3);
        boolean postalCodeExists = StringFun.notEmpty(postalCode);
        boolean cityExists = StringFun.notEmpty(city);
        boolean hausnummerExists = StringFun.notEmpty(hausnummer);
        boolean someAddressAttributeIsWellDefined = address1Exists ||
                address2Exists || address3Exists || postalCodeExists || cityExists || hausnummerExists;

        if (!someAddressAttributeIsWellDefined) return this;

        Address address = new Address();

        if (address1Exists) address.setAddr1(address1);
        if (address2Exists) address.setAddr2(address2);
        if (address3Exists) address.setAddr3(address3);
        if (postalCodeExists) address.setPostalCode(postalCode);
        if (cityExists) address.setCity(city);

        if (hausnummerExists) {
            address.getLines(true).add(String.format("Hausnummer: %s", hausnummer));
        }

        individual.setAddress(address);

        return this;
    }

    IndividualBuilder sex(Geschlecht geschlecht) {
        individual.setSex(SexFormatter.format(geschlecht));
        return this;
    }

    IndividualBuilder religion(String religion) {
        setAttribute(IndividualAttributeType.RELIGIOUS_AFFILIATION, religion);
        return this;
    }

    IndividualBuilder occupation(String occupation) {
        setAttribute(IndividualAttributeType.OCCUPATION, occupation);
        return this;
    }

    IndividualBuilder birth(String day, String month, String year, String place, String source) {
        setEvent(IndividualEventType.BIRTH, "Geburtsdatum", day, month, year, place, source, null);
        return this;
    }

    IndividualBuilder baptism(String day, String month, String year, String godParent, String place, String source) {
        String note = StringFun.notEmpty(godParent) ? String.format("Taufpate: %s", godParent) : null;
        setEvent(IndividualEventType.BAPTISM, "Taufdatum", day, month, year, place, source, null, note);
        return this;
    }

    IndividualBuilder death(String day, String month, String year, String place, String source, String causeOfDeath) {
        setEvent(IndividualEventType.DEATH, "Sterbedatum", day, month, year, place, source, causeOfDeath);
        return this;
    }

    IndividualBuilder burial(String day, String month, String year, String place, String source) {
        setEvent(IndividualEventType.BURIAL, "Beerdigungsdatum", day, month, year, place, source, null);
        return this;
    }

    IndividualBuilder lebensortAsNote(String lebensort) {
        addNote("Lebensort", lebensort);
        return this;
    }

    IndividualBuilder source(String source) {
        if (StringFun.isEmpty(source)) return this;

        CitationWithSource citation = new CitationWithSource();
        citation.setSource(sourceStore.createSource(source));
        individual.getCitations(true).add(citation);

        return this;
    }

    Individual build() {
        return individual;
    }

    private void setEvent(IndividualEventType type, String notePrefix, String day, String month, String year,
                          String placeName, String source, String cause, String... notes) {

        String date = DateFormatter.format(day, month, year);
        String[] nonNullNotes = Arrays.stream(notes)
                .filter(Objects::nonNull)
                .toArray(String[]::new);

        boolean dateIsWellDefined = date != null;
        boolean placeNameExists = StringFun.notEmpty(placeName);
        boolean originalDateHasInvalidStructure = DateFormatter.hasInvalidStructure(day, month, year);
        boolean sourceExists = StringFun.notEmpty(source);
        boolean causeExists = StringFun.notEmpty(cause);
        boolean notesNotEmpty = !List.of(nonNullNotes).isEmpty();
        boolean someEventAttributeIsWellDefined = dateIsWellDefined ||
                placeNameExists || originalDateHasInvalidStructure || sourceExists || notesNotEmpty;

        if (!someEventAttributeIsWellDefined) return;

        IndividualEvent event = new IndividualEvent();
        event.setType(type);

        if (dateIsWellDefined) event.setDate(date);
        if (placeNameExists) attachPlace(event, placeName);
        if (originalDateHasInvalidStructure) attachDateNotes(event, notePrefix, day, month, year);
        if (sourceExists) attachCitation(event, source);
        if (causeExists) event.setCause(cause);

        attachNotes(event, nonNullNotes);
        individual.getEvents(true).add(event);
    }

    private static void attachPlace(IndividualEvent event, String placeName) {
        Place place = new Place();
        place.setPlaceName(PlaceFormatter.format(placeName));
        event.setPlace(place);
    }

    private void attachDateNotes(IndividualEvent event, String notePrefix, String day, String month, String year) {
        attachNote(event, DateFormatter.formatAsNote(notePrefix, day, month, year));
    }

    private void attachNotes(IndividualEvent event, String... notes) {
        for (var note : notes) attachNote(event, note);
    }

    private void attachCitation(IndividualEvent event, String source) {
        CitationWithSource citation = new CitationWithSource();
        citation.setSource(sourceStore.createSource(source));
        event.getCitations(true).add(citation);
    }

    private void attachNote(IndividualEvent event, String note) {
        event.getNoteStructures(true).add(createNote(note));
    }

    private void addNote(String notePrefix, String note) {
        if (StringFun.isEmpty(note)) return;

        var noteStructure = createNote(String.format("%s: %s", notePrefix, note));
        individual.getNoteStructures(true).add(noteStructure);
    }

    private NoteStructure createNote(String note) {
        NoteStructure noteStructure = new NoteStructure();
        NoteRecord record = noteStore.createNote(note);
        noteStructure.setNoteReference(record);
        return noteStructure;
    }

    private void setAttribute(IndividualAttributeType type, String description) {
        if (StringFun.isEmpty(description)) return;

        var attribute = new IndividualAttribute();
        attribute.setType(type);
        attribute.setDescription(description);
        individual.getAttributes(true).add(attribute);
    }
}
