package de.ng.ahnenwin2gedcom.hej

import de.ng.ahnenwin2gedcom.hej.Verbindung.Companion.findByCsvValue
import de.ng.ahnenwin2gedcom.helperfunctions.StringFun.isEmpty
import de.ng.ahnenwin2gedcom.logger
import org.apache.commons.csv.CSVRecord
import java.util.*
import kotlin.reflect.KClass
import kotlin.reflect.cast

class HejBeziehung internal constructor(record: CSVRecord) {

    private val properties = mutableMapOf<BeziehungsProperty, Any>()

    init {
        for (property in BeziehungsProperty.values()) {
            setProperty(property, record)
        }
    }

    private fun setProperty(property: BeziehungsProperty, record: CSVRecord) {
        val value: String? = try {
            record[property.columnIndex]
        } catch (ignore: IllegalArgumentException) {
            null
        }

        if (property.required && value == null) {
            logger().error("Die Beziehungseigenschaft $property wird benötigt, " +
                    "aber der CSVRecord $record enthält dafür keinen Wert. " +
                    "Die Beziehung kann nicht nach Gedcom übertragen werden.")
            return
        }

        if (isEmpty(value)) return

        properties[property] = when (property.datatype) {
            Int::class -> value?.toInt()
            String::class -> value
            Verbindung::class -> findByCsvValue(value)
            else -> logger().error("AhnenEigenschaft $property hat den unbehandelten Datentyp " +
                    "${property.datatype.simpleName} für HejAhne mit ID ${record[0]}. " +
                    "Bitte an den Entwickler melden.")
        } ?: return
    }

    fun getString(key: BeziehungsProperty) = get(key, String::class)
    fun getRequiredInt(key: BeziehungsProperty): Int {
        if (!key.required) {
            throw IllegalArgumentException("$key is not a required BeziehungsProperty.")
        }
        return get(key, Int::class)!!
    }

    val verbindung
        get() = get(BeziehungsProperty.VERBINDUNG, Verbindung::class)

    private fun <T : Any> get(key: BeziehungsProperty, requiredDatatype: KClass<T>): T? {
        if (key.datatype == requiredDatatype) {
            try {
                return requiredDatatype.cast(properties[key])
            } catch (e: ClassCastException) {
                if (properties[key] == null) return null
                logger().error("Could not cast property $key with value ${properties[key]} " +
                        "to class ${requiredDatatype.simpleName}", e)
                throw e
            }
        }
        throw RuntimeException("Benötigt Datentyp $requiredDatatype, war aber ${key.datatype}." +
                "Das hätte nicht passieren dürfen. Bitte an den Entwickler melden.")
    }

    override fun equals(other: Any?): Boolean {
        return this === other || (other is HejBeziehung && properties == other.properties)
    }

    override fun hashCode(): Int {
        return Objects.hash(properties)
    }

    override fun toString(): String {
        return properties.toString()
    }
}
