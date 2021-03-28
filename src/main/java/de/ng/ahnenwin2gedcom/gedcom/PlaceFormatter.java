package de.ng.ahnenwin2gedcom.gedcom;

class PlaceFormatter {
    static String format(String place) {
        return place.replaceAll(",", ";");
    }
}
