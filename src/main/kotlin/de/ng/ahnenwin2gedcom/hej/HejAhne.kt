package de.ng.ahnenwin2gedcom.hej

import de.ng.ahnenwin2gedcom.hej.Geschlecht.Companion.findByCsvValue
import de.ng.ahnenwin2gedcom.helperfunctions.StringFun.isEmpty
import de.ng.ahnenwin2gedcom.logger
import org.apache.commons.csv.CSVRecord
import java.util.*
import kotlin.reflect.KClass
import kotlin.reflect.cast

class HejAhne internal constructor(record: CSVRecord) {

    companion object {
        /**
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
                "Record with Ahnennummer {} has {} values. Cannot handle this amount of values. " +
                        "Please report this error to the developer.",
                record[0],
                numberOfValues
            )
            return false
        }

        private fun splitByDLEDelimiter(text: String): Array<String> {
            var values = text.split(HejDelimiter.DLE.charValue).toTypedArray()
            values = values.sliceArray(1 until values.size)
            return values
        }
    }

    private val properties = mutableMapOf<AhnenProperty, Any>()

    init {
        val applyIndexShift = mustApplyIndexShift(record)
        AhnenProperty.values().forEach { setProperty(it, record, applyIndexShift) }
    }

    private fun setProperty(property: AhnenProperty, record: CSVRecord, applyIndexShift: Boolean) {
        var index = property.columnIndex
        if (applyIndexShift && AhnenProperty.TEXT.columnIndex < index) --index
        if (applyIndexShift && AhnenProperty.TEXT == property) return

        val value = try {
            record[index]
        } catch (ignore: IllegalArgumentException) {
            null
        }

        if (property.required && value == null) {
            logger().error("Die Ahneneigenschaft $property wird benötigt, " +
                    "aber der CSVRecord $record enthält dafür keinen Wert. " +
                    "Der Ahne kann nicht nach Gedcom übertragen werden.")
            return
        }

        if (isEmpty(value)) return

        properties[property] = when(property.datatype) {
            Int::class -> value!!.toInt()
            String::class -> value
            Geschlecht::class -> findByCsvValue(value)
            Array<String>::class -> splitByDLEDelimiter(value!!)
            else -> logger().error("AhnenEigenschaft $property hat den unbehandelten Datentyp " +
                    "${property.datatype.simpleName} für HejAhne mit ID ${record[0]}. " +
                    "Bitte an den Entwickler melden.")
        } ?: return
    }

    fun getString(key: AhnenProperty) = get(key, String::class)

    fun getRequiredInt(key: AhnenProperty): Int {
        if (!key.required) {
            throw IllegalArgumentException("$key is not a required AhnenProperty")
        }
        return get(key, Int::class)!!
    }

    fun getStringArray(key: AhnenProperty) = get(key, Array<String>::class)

    val geschlecht
        get() = get(AhnenProperty.GESCHLECHT, Geschlecht::class)

    private fun <T: Any> get(key: AhnenProperty, requiredDatatype: KClass<T>): T? {
        if (key.datatype != requiredDatatype) {
            throw RuntimeException("Benötigt Datentyp $requiredDatatype, war aber ${key.datatype}." +
                    "Das hätte nicht passieren dürfen. Bitte an den Entwickler melden.")
        }

        if (properties[key] == null) return null

        return try {
            requiredDatatype.cast(properties[key])
        } catch (classCastException: ClassCastException) {
            logger().error("Could not cast property $key with value ${properties[key]} " +
                    "to class ${requiredDatatype.simpleName}", classCastException)
            null
        }
    }

    override fun equals(other: Any?): Boolean {
        return this === other || (other is HejAhne && properties == other.properties)
    }

    override fun hashCode() = Objects.hash(properties)

    override fun toString() = properties.toString()
}
