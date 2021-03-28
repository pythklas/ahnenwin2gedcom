package de.ng.ahnenwin2gedcom.hej

import de.ng.ahnenwin2gedcom.hej.Geschlecht.Companion.findByCsvValue
import de.ng.ahnenwin2gedcom.helperfunctions.StringFun.notEmpty
import de.ng.ahnenwin2gedcom.logger
import org.apache.commons.csv.CSVRecord
import java.util.*

class HejAhne internal constructor(record: CSVRecord) {

    companion object {
        /**
         * TODO Refactor an fachlich besseren Ort?
         * Ein record aus der hej-Datei kann 50 oder 51 Werte haben.
         * Falls sie nur 50 Werte hat, wird nach aktuellem Kenntnisstand das Text-Feld ausgelassen.
         * Deswegen muss ab dem Text-Feld fuer jedes Feld vom Index aus dem Enum [AhnenProperty] 1 abgezogen werden.
         * Diese Methode ermittelt, ob der Index-Shift beim Befüllen der properties angewandt werden muss.
         */
        private fun mustApplyIndexShift(record: CSVRecord): Boolean {
            val numberOfValues = record.toList().count()
            if (numberOfValues == 50) return true
            if (numberOfValues == 51) return false
            logger().error(
                "Record with Ahnennummer {} has {} values. Cannot handle this amount of values. Please report this error to the developer.",
                record[0],
                numberOfValues
            )
            return false
        }
    }

    private val properties = mutableMapOf<AhnenProperty, Any?>()

    init {
        val applyIndexShift = mustApplyIndexShift(record)
        for (property in AhnenProperty.values()) {
            setProperty(property, record, applyIndexShift)
        }
    }

    private fun setProperty(property: AhnenProperty, record: CSVRecord, applyIndexShift: Boolean) {
        var index = property.columnIndex
        if (applyIndexShift && AhnenProperty.TEXT.columnIndex < index) --index
        if (applyIndexShift && AhnenProperty.TEXT == property) return
        val value: String? = try {
            record[index]
        } catch (ignore: IllegalArgumentException) {
            null
        }
        if (property.datatype == Int::class.java) {
            checkNotNull(value) { "AhnenProperty datatype was int, but provided value was null." }
            properties[property] = value.toInt()
            return
        }
        if (property.datatype == String::class.java) {
            if (notEmpty(value)) properties[property] = value
            return
        }
        if (property.datatype == Geschlecht::class.java) {
            if (notEmpty(value)) properties[property] = findByCsvValue(value)
            return
        }
        if (property.datatype == Array<String>::class.java) {
            if (notEmpty(value)) {
                var values = value!!.split(HejDelimiter.DLE.charValue.toString().toRegex()).toTypedArray()
                values = values.copyOfRange(1, values.size)
                properties[property] = values
            }
            return
        }
        logger().error(
            "AhnenProperty {} has unhandled datatype {} for HejAhne with id {}.",
            property, property.datatype.simpleName, record[0]
        )
    }

    fun getStringArray(key: AhnenProperty): Array<String?> {
        return get(key, Array<String?>::class.java)
    }

    fun getString(key: AhnenProperty): String {
        return get(key, String::class.java)
    }

    val geschlecht: Geschlecht
        get() = get(AhnenProperty.GESCHLECHT, Geschlecht::class.java)

    fun getInt(key: AhnenProperty): Int {
        if (key.datatype == Int::class.javaPrimitiveType) {
            return properties[key] as Int
        }
        throw RuntimeException("Requires datatype ${Int::class.java}, but was ${key.datatype}")
    }

    private operator fun <T> get(key: AhnenProperty, requiredDatatype: Class<T>): T {
        if (key.datatype == requiredDatatype) {
            return requiredDatatype.cast(properties[key])
        }
        throw RuntimeException("Requires datatype $requiredDatatype, but was ${key.datatype}")
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || javaClass != other.javaClass) return false
        val hejAhne = other as HejAhne
        return properties == hejAhne.properties
    }

    override fun hashCode(): Int {
        return Objects.hash(properties)
    }
}
