package de.ng.ahnenwin2gedcom.gedcom;

class XrefFormatter {
    static String format(int id) {
        return String.format("@%d@", id);
    }
}
