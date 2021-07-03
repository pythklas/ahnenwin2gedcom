package de.ng.ahnenwin2gedcom.helperfunctions

object StringFun {
    @JvmStatic
    fun notEmpty(value: String?): Boolean {
        return !isEmpty(value)
    }

    @JvmStatic
    fun isEmpty(value: String?): Boolean {
        return value == null || value.trim() == ""
    }
}
