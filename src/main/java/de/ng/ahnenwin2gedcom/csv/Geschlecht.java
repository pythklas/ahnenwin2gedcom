package de.ng.ahnenwin2gedcom.csv;

import java.util.Arrays;

public enum Geschlecht {
    MAENNLICH("m", "M"),
    WEIBLICH("w", "W"),
    UNBEKANNT();
    private final String[] csvValues;

    Geschlecht(String... csvValues) {
        this.csvValues = csvValues;
    }

    static Geschlecht findByCsvValue(String csvValue) {
        String trimmedCsvValue = csvValue == null ? "" : csvValue.trim();
        return Arrays.stream(values())
                .filter(geschlecht -> Arrays.asList(geschlecht.csvValues).contains(trimmedCsvValue))
                .findAny()
                .orElse(Geschlecht.UNBEKANNT);
    }
}
