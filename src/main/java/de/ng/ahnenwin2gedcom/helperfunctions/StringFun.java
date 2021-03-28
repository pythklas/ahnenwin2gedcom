package de.ng.ahnenwin2gedcom.helperfunctions;

public class StringFun {
    public static boolean notEmpty(String value) {
        return !isEmpty(value);
    }

    public static boolean isEmpty(String value) {
        return value == null || value.trim().equals("");
    }
}
