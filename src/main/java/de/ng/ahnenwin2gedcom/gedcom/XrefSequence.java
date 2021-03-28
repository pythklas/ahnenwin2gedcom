package de.ng.ahnenwin2gedcom.gedcom;

class XrefSequence {
    private int sequence = 0;

    String next() {
        return XrefFormatter.format(sequence++);
    }
}
