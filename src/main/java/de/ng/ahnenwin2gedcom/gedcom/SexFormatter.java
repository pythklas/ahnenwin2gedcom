package de.ng.ahnenwin2gedcom.gedcom;

import de.ng.ahnenwin2gedcom.csv.Geschlecht;

class SexFormatter {
    static String format(Geschlecht geschlecht) {
        return switch (geschlecht) {
            case MAENNLICH -> "M";
            case WEIBLICH -> "F";
            case UNBEKANNT -> null;
        };
    }
}
