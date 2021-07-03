package de.ng.ahnenwin2gedcom.hej

import de.ng.ahnenwin2gedcom.hej.Verbindung.Companion.findByCsvValue
import de.ng.ahnenwin2gedcom.helperfunctions.StringFun.notEmpty
import de.ng.ahnenwin2gedcom.logger
import org.apache.commons.csv.CSVRecord
import java.util.*

class HejBeziehung internal constructor(record: CSVRecord) {

    private val properties = mutableMapOf<BeziehungsProperty, Any?>()

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
        if (property.datatype == Int::class.javaPrimitiveType) {
            checkNotNull(value) { "Property datatype was int, but provided value was null." }
            properties[property] = value.toInt()
            return
        }
        if (property.datatype == String::class.java) {
            if (notEmpty(value)) properties[property] = value
            return
        }
        if (property.datatype == Verbindung::class.java) {
            if (notEmpty(value)) properties[property] = findByCsvValue(value)
            return
        }
        logger().error("BeziehungsProperty $property has unhandled datatype ${property.datatype.simpleName}" +
                " for HejBeziehung with ids ${record[0]} and ${record[1]}.")
    }

    fun getString(key: BeziehungsProperty): String? {
        return get(key, String::class.java)
    }

    val verbindung: Verbindung?
        get() = get(BeziehungsProperty.VERBINDUNG, Verbindung::class.java)

    fun getInt(key: BeziehungsProperty): Int {
        if (key.datatype == Int::class.javaPrimitiveType) {
            return properties[key] as Int
        }
        throw RuntimeException("Requires datatype ${Int::class.java}, but was ${key.datatype}")
    }

    private operator fun <T> get(key: BeziehungsProperty, requiredDatatype: Class<T>): T? {
        if (key.datatype == requiredDatatype) {
            return requiredDatatype.cast(properties[key])
        }
        throw RuntimeException(
            String.format(
                "Requires datatype %s, but was %s",
                requiredDatatype,
                key.datatype
            )
        )
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is HejBeziehung) return false
        return properties == other.properties
    }

    override fun hashCode(): Int {
        return Objects.hash(properties)
    }

    override fun toString(): String {
        return properties.toString()
    }
}
