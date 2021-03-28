package de.ng.ahnenwin2gedcom.csv;

import java.util.Arrays;

public enum Verbindung {
    ANDERE_BEZIEHUNG("andere Beziehung"),
    EHESCHLIESSUNG("Eheschliessung"),
    LEBENSGEMEINSCHAFT("Lebensgemeinschaft"),
    VERLOBUNG("Verlobung"),
    LEER("");

    private final String csvValue;

    Verbindung(String csvValue) {
        this.csvValue = csvValue;
    }

    static Verbindung findByCsvValue(String csvValue) {
        String trimmedCsvValue = csvValue == null ? "" : csvValue.trim();
        return Arrays.stream(values())
                .filter(verbindung -> trimmedCsvValue.equals(verbindung.csvValue))
                .findAny()
                .orElse(Verbindung.LEER);
    }
}
