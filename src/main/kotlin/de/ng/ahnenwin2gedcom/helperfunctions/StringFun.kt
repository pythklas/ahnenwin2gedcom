package de.ng.ahnenwin2gedcom.helperfunctions

object StringFun {
    fun notEmpty(value: String?): Boolean {
        return !isEmpty(value)
    }

    fun isEmpty(value: String?): Boolean {
        return value == null || value.trim() == ""
    }
}
