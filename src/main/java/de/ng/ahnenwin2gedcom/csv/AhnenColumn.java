package de.ng.ahnenwin2gedcom.csv;

public enum AhnenColumn implements CsvColumn {
    NUMMER("Nr", int.class),
    VATER("Vater", int.class),
    MUTTER("Mutter", int.class),
    NACHNAME("Name"),
    VORNAMEN("Vornamen"),
    RUFNAME("Rufname"),
    GESCHLECHT("Geschlecht", Geschlecht.class),
    RELIGION("Religion"),
    BERUF("Beruf"),
    GEBURTSTAG("GTag"),
    GEBURTSMONAT("GMonat"),
    GEBURTSJAHR("GJahr"),
    GEBURTSORT("GOrt"),
    TAUFTAG("TTag"),
    TAUFMONAT("TMonat"),
    TAUFJAHR("TJahr"),
    TAUFORT("TOrt"),
    TAUFPATE("TPat"),
    LEBENSORT("LebOrt"),
    STERBETAG("STag"),
    STERBEMONAT("SMonat"),
    STERBEJAHR("SJahr"),
    STERBEORT("SOrt"),
    TODESURSACHE("TU"),
    BEERDIGUNG_TAG("BTag"),
    BEERDIGUNG_MONAT("BMonat"),
    BEERDIGUNG_JAHR("BJahr"),
    BEERDIGUNG_ORT("BOrt"),
    QUELLE("Qu"),
    GEBURT_QUELLE("GQu"),
    TAUFE_QUELLE("TQu"),
    STERBEQUELLE("SQu"),
    BEERDIGUNG_QUELLE("BQu"),
    LEBT("lebt"),
    SCHREIBWEISE("Schreibw"),
    ID("ID"),
    ADOPT("Adopt"),
    HAUSNUMMER("Hausn"),
    ADRESSE_1("Adr1"),
    ADRESSE_2("Adr2"),
    PLZ("PLZ"),
    ADRESSE_ORT("AdrOrt"),
    ADRESSE_ZUSATZ("AdrZus"),
    STERBEALTER("StAlter");

    private final String csvValue;
    private final Class<?> datatype;

    AhnenColumn(String csvValue) {
        this(csvValue, String.class);
    }

    AhnenColumn(String csvValue, Class<?> datatype) {
        this.csvValue = csvValue;
        this.datatype = datatype;
    }

    public String getCsvValue() {
        return csvValue;
    }

    Class<?> getDatatype() {
        return datatype;
    }
}
