package de.ng.ahnenwin2gedcom.gedcom;

import org.gedcom4j.model.NoteRecord;

import java.util.HashMap;
import java.util.Map;

class NoteStore {
    private final XrefSequence noteSequence = new XrefSequence();
    private final Map<String, NoteRecord> store = new HashMap<>();

    NoteRecord createNote(String... notes) {
        String xref = noteSequence.next();
        NoteRecord noteRecord = new NoteRecord(xref);
        for (var note : notes) noteRecord.getLines(true).add(note);
        store.put(xref, noteRecord);
        return noteRecord;
    }

    Map<String, NoteRecord> getAll() {
        return store;
    }
}
