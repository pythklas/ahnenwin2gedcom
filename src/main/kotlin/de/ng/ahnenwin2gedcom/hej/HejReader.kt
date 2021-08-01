package de.ng.ahnenwin2gedcom.hej

import de.ng.ahnenwin2gedcom.helperfunctions.ListFun.splitWhereTrue
import org.apache.commons.csv.CSVFormat
import org.apache.commons.csv.CSVRecord
import java.io.FileReader
import java.nio.charset.Charset

object HejReader {

    private val heyRowDelimiters = arrayOf("mrg", "adop", "ortv", "quellv")

    fun read(path: String): HejData {
        val reader = FileReader(path, Charset.forName("windows-1252"))
        val records = CSVFormat.TDF
            .withQuote(null)
            .withDelimiter(HejDelimiter.SI.charValue)
            .parse(reader)
            .toList()
            .filterNotNull()
        val splitByRowDelimiters = splitWhereTrue(records) { isHejRowDelimiter(it) }
        val hejAhnen = splitByRowDelimiters[0].map { HejAhne(it) }.toSet()
        val hejBeziehungen = splitByRowDelimiters[1].map { HejBeziehung(it) }.toSet()
        return HejData(hejAhnen, hejBeziehungen)
    }

    private fun isHejRowDelimiter(record: CSVRecord): Boolean {
        return heyRowDelimiters.contains(record[0])
    }
}
