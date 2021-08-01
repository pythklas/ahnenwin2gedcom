package de.ng.ahnenwin2gedcom.helperfunctions

object StringFun {
    fun notEmpty(value: String?) = !isEmpty(value)

    fun isEmpty(value: String?) = value == null || value.trim() == ""
}
