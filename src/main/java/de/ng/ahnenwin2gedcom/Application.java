package de.ng.ahnenwin2gedcom;

import de.ng.ahnenwin2gedcom.csv.CsvAhne;
import de.ng.ahnenwin2gedcom.csv.CsvReader;
import de.ng.ahnenwin2gedcom.gedcom.GedcomFactory;
import org.gedcom4j.exception.GedcomWriterException;
import org.gedcom4j.model.Gedcom;
import org.gedcom4j.writer.GedcomWriter;

import java.io.IOException;
import java.util.Set;

public class Application {

    public static void main(String[] args) throws IOException, GedcomWriterException {
        Set<CsvAhne> ahnen = CsvReader.read("./src/main/resources/csv/ahnen5niklashatzweiverbindungen.txt");
        Gedcom gedcom = GedcomFactory.create(ahnen);
        GedcomWriter gedcomWriter = new GedcomWriter(gedcom);
        gedcomWriter.write("./tmp/test.ged");
    }


}
