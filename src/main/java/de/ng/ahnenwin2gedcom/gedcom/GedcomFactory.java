package de.ng.ahnenwin2gedcom.gedcom;

import de.ng.ahnenwin2gedcom.csv.CsvAhne;
import org.gedcom4j.model.*;

import java.util.Collection;

public class GedcomFactory {

    public static Gedcom create(Collection<CsvAhne> csvAhnen) {
        Gedcom gedcom = new Gedcom();
        NoteStore notes = new NoteStore();
        SourceStore sources = new SourceStore();
        Mapper map = new Mapper(notes, sources);

        gedcom.getIndividuals().putAll(map.toIndividuals(csvAhnen));
        gedcom.getFamilies().putAll(map.toFamilies(csvAhnen));
        gedcom.setHeader(header(notes));
        gedcom.getNotes().putAll(notes.getAll());
        gedcom.getSources().putAll(sources.getAll());

        return gedcom;
    }

    private static Header header(NoteStore notes) {
        Header header = new Header();
        CharacterSet characterSet = new CharacterSet();
        characterSet.setCharacterSetName("UTF-8");
        GedcomVersion version = new GedcomVersion();
        version.setVersionNumber("5.5.1");
        version.setGedcomForm("LINEAGE-LINKED");
        header.setFileName("test.ged");
        SourceSystem sourceSystem = new SourceSystem();
        sourceSystem.setProductName("ahnenwin2gedcom");
        sourceSystem.setVersionNum("1.0-SNAPSHOT");
        sourceSystem.setSystemId("@AHN2GED@");
        header.setSourceSystem(sourceSystem);
        header.setCharacterSet(characterSet);
        header.setGedcomVersion(version);
        header.setPlaceHierarchy("AhnenwinOrt");
        NoteRecord note = notes.createNote("created by ahnenwin2gedcom");
        NoteStructure noteStructure = new NoteStructure();
        noteStructure.setNoteReference(note);
        header.getNoteStructures(true).add(noteStructure);
        return header;
    }
}
