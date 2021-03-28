package de.ng.ahnenwin2gedcom.csv;

public enum VerbindungsColumn implements CsvColumn {
    VERBINDUNG("Verbind.", Verbindung.class),
    PARTNER("Partner"),
    HOCHZEITSTAG_KIRCHE("HTag k"),
    HOCHZEITSMONAT_KIRCHE("HMonat k"),
    HOCHZEITSJAHR_KIRCHE("HJahr k"),
    HOCHZEITSORT_KIRCHE("HOrt k"),
    TRAUZEUGEN_KIRCHE("Trauz. k"),
    HOCHZEITSTAG_STANDESAMT("HTag s"),
    HOCHZEITSMONAT_STANDESAMT("HMonat s"),
    HOCHZEITSJAHR_STANDESAMT("HJahr s"),
    HOCHZEITSORT_STANDESAMT("HOrt s"),
    TRAUZEUGEN_STANDESAMT("Trauz. s"),
    SCHEIDUNG_TAG("SchTag"),
    SCHEIDUNG_MONAT("SchMonat"),
    SCHEIDUNG_JAHR("SchJahr"),
    SCHEIDUNG_ORT("SchOrt");

    private final String csvValue;
    private final Class<?> datatype;

    VerbindungsColumn(String csvValue) {
        this(csvValue, String.class);
    }

    VerbindungsColumn(String csvValue, Class<?> datatype) {
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
