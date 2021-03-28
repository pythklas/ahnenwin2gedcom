package de.ng.ahnenwin2gedcom.gedcom;

import de.ng.ahnenwin2gedcom.helperfunctions.StringFun;
import org.gedcom4j.model.*;
import org.gedcom4j.model.enumerations.FamilyEventType;

import java.util.*;
import java.util.stream.Collectors;

class FamilyBuilder {
    private static final Set<FamilyEventType> typesWithRelevantSubtypes =
            Set.of(FamilyEventType.MARRIAGE, FamilyEventType.EVENT);

    private final Family family;
    private final SourceStore sourceStore; //TODO: ausbauen oder ueberpruefen ob hier noch gebraucht...
    private final NoteStore noteStore;

    FamilyBuilder(Family family, SourceStore sourceStore, NoteStore noteStore) {
        this.sourceStore = sourceStore;
        this.noteStore = noteStore;
        this.family = family;
    }

    FamilyBuilder(SourceStore sourceStore, NoteStore noteStore) {
        this(new Family(), sourceStore, noteStore);
    }

    FamilyBuilder xref(String xref) {
        family.setXref(xref);
        return this;
    }

    FamilyBuilder wife(int id) {
        if (id != 0) family.setWife(individualReference(id));
        return this;
    }

    FamilyBuilder husband(int id) {
        if (id != 0) family.setHusband(individualReference(id));
        return this;
    }

    FamilyBuilder addChildren(Collection<Integer> ids) {
        List<IndividualReference> childRefs = ids.stream()
                .filter(Objects::nonNull)
                .map(FamilyBuilder::individualReference)
                .collect(Collectors.toList());
        family.getChildren(true).addAll(childRefs);
        return this;
    }

    FamilyBuilder marriageKirchlich(String day, String month, String year, String placeName) {
        setEvent(FamilyEventType.MARRIAGE, "Hochzeit kirchlich", day, month, year, placeName);
        return this;
    }

    FamilyBuilder marriageStandesamtlich(String day, String month, String year, String placeName) {
        setEvent(FamilyEventType.MARRIAGE, "Hochzeit standesamtlich", day, month, year, placeName);
        return this;
    }

    FamilyBuilder engagement(String day, String month, String year, String placeName) {
        setEvent(FamilyEventType.ENGAGEMENT, "Verlobung", day, month, year, placeName);
        return this;
    }

    FamilyBuilder lebensgemeinschaft(String day, String month, String year, String placeName) {
        setEvent(FamilyEventType.EVENT, "Lebensgemeinschaft", day, month, year, placeName);
        return this;
    }

    FamilyBuilder andereBeziehung(String day, String month, String year, String placeName) {
        setEvent(FamilyEventType.EVENT, "andere Beziehung", day, month, year, placeName);
        return this;
    }

    FamilyBuilder divorce(String day, String month, String year, String placeName) {
        setEvent(FamilyEventType.DIVORCE, "Scheidung", day, month, year, placeName);
        return this;
    }

    Family build() {
        return family;
    }

    private static IndividualReference individualReference(int id) {
        Individual individual = IndividualBuilder.buildDummy(id);
        return new IndividualReference(individual);
    }

    private void setEvent(FamilyEventType type, String subType, String day, String month, String year, String placeName) {
        String date = DateFormatter.format(day, month, year);
        boolean dateIsWellDefined = date != null;
        boolean placeNameExists = StringFun.notEmpty(placeName);
        boolean originalDateHasInvalidStructure = DateFormatter.hasInvalidStructure(day, month, year);
        if (dateIsWellDefined || placeNameExists || originalDateHasInvalidStructure) {
            FamilyEvent event = new FamilyEvent();
            event.setType(type);
            if (dateIsWellDefined) {
                event.setDate(date);
            }
            if (placeNameExists) {
                attachPlace(event, placeName);
            }
            if (originalDateHasInvalidStructure) {
                attachDateNotes(event, subType, day, month, year);
            }
            if (typesWithRelevantSubtypes.contains(type)) {
                event.setSubType(subType);
            }
            family.getEvents(true).add(event);
        }
    }

    private static void attachPlace(FamilyEvent event, String placeName) {
        Place place = new Place();
        place.setPlaceName(PlaceFormatter.format(placeName));
        event.setPlace(place);
    }

    private void attachDateNotes(FamilyEvent event, String notePrefix, String day, String month, String year) {
        // TODO: Code duplication loswerden
        List<String> notes = new LinkedList<>();
        if (StringFun.notEmpty(day)) notes.add("Tag: " + day);
        if (StringFun.notEmpty(month)) notes.add("Monat: " + month);
        if (StringFun.notEmpty(year)) notes.add("Jahr: " + year);
        String note = notePrefix + ": " + String.join(", ", notes);
        attachNote(event, note);
    }

    private void attachNote(FamilyEvent event, String note) {
        event.getNoteStructures(true).add(createNote(note));
    }

    private NoteStructure createNote(String note) {
        NoteStructure noteStructure = new NoteStructure();
        NoteRecord record = noteStore.createNote(note);
        noteStructure.setNoteReference(record);
        return noteStructure;
    }
}
