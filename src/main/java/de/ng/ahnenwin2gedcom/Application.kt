package de.ng.ahnenwin2gedcom

import de.ng.ahnenwin2gedcom.gedcom.GedcomFactory
import de.ng.ahnenwin2gedcom.hej.HejReader
import org.gedcom4j.exception.GedcomWriterException
import org.gedcom4j.writer.GedcomWriter
import java.io.File
import java.io.IOException
import java.nio.file.Path

object Application {

    @JvmStatic
    fun main(args: Array<String>) {
        LoggingInitializer.initLoggerWithLoggingPropertiesFile()
        mapAllHejFilesToGedcomInCurrentDir()
    }

    private fun mapAllHejFilesToGedcomInCurrentDir() {
        val currentDir = File(".")
        val files = currentDir.listFiles { file -> file.isFile } ?: return
        files.filter { it.name.endsWith(".hej") }
            .map { it.path }
            .forEach { mapHejToGedcomFile(it) }
    }

    private fun mapHejToGedcomFile(path: String) {
        val pathWithoutHejSuffix = path.replace("^(.+)\\.hej$".toRegex(), "$1")
        val gedcomPath = "$pathWithoutHejSuffix.ged"
        val gedcomFileName = Path.of(gedcomPath).fileName.toString()
        try {
            val hej = HejReader.read(path)
            val gedcom = GedcomFactory.create(hej, gedcomFileName)
            val gedcomWriter = GedcomWriter(gedcom)
            gedcomWriter.write(gedcomPath)
        } catch (e: IOException) {
            log(e)
        } catch (e: GedcomWriterException) {
            log(e)
        }
    }
}
