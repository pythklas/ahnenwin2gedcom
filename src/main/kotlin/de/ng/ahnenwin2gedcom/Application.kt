package de.ng.ahnenwin2gedcom

import de.ng.ahnenwin2gedcom.gedcom.GedcomFactory
import de.ng.ahnenwin2gedcom.hej.HejReader
import org.gedcom4j.exception.GedcomWriterException
import org.gedcom4j.exception.GedcomWriterVersionDataMismatchException
import org.gedcom4j.writer.GedcomWriter
import java.io.File
import java.nio.file.Path

fun main() {
    initLoggerWithLoggingPropertiesFile()
    mapAllHejFilesToGedcomInCurrentDir()
}

val logger = logger("Application")

private fun mapAllHejFilesToGedcomInCurrentDir() {
    val currentDir = File(".")
    val files = currentDir.listFiles { file -> file.isFile } ?: return
    files.filter { it.name.endsWith(".hej") }
        .map { it.path }
        .forEach { mapHejToGedcomFile(it) }
}

private fun mapHejToGedcomFile(hejFilePath: String) {
    val pathWithoutHejSuffix = hejFilePath.replace("^(.+)\\.hej$".toRegex(), "$1")
    val gedcomPath = "$pathWithoutHejSuffix.ged"
    val gedcomFileName = Path.of(gedcomPath).fileName.toString()
    val hej = HejReader.read(hejFilePath)
    val gedcom = GedcomFactory.create(hej, gedcomFileName)
    val gedcomWriter = GedcomWriter(gedcom)
    try {
        gedcomWriter.write(gedcomPath)
    } catch (e: Exception) {
        when (e) {
            is GedcomWriterVersionDataMismatchException,
            is GedcomWriterException -> logger.error(gedcomWriter.validator.results.toString())

            else -> throw (e)
        }
    }

}
