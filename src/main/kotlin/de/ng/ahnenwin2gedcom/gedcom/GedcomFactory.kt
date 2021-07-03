package de.ng.ahnenwin2gedcom.gedcom

import de.ng.ahnenwin2gedcom.ApplicationProperties
import de.ng.ahnenwin2gedcom.hej.HejData
import org.gedcom4j.model.*

object GedcomFactory {
    fun create(hej: HejData, fileName: String): Gedcom {
        val gedcom = Gedcom()
        val sources = SourceStore()
        val individualStore = IndividualStore()
        val familyStore = FamilyStore(sources, individualStore)
        val familyMapper = FamilyMapper(sources, familyStore, individualStore)
        val individualMapper = IndividualMapper(sources, individualStore)

        // TODO mapper betreiben jetzt zu viel Magie. Sollte refactored werden, so dass transparent wird,
        //  dass für die familiy mappings die individuals bereits gemappt sein müssen.
        gedcom.individuals.putAll(individualMapper.map(hej.ahnen))
        gedcom.families.putAll(familyMapper.map(hej))
        val submitter = submitter()
        gedcom.header = header(fileName, submitter)
        gedcom.sources.putAll(sources.all)
        gedcom.submitters[submitter.xref] = submitter
        return gedcom
    }

    private fun submitter(): Submitter {
        val submitter = Submitter()
        submitter.setName("AHN2GED")
        submitter.xref = XrefFormatter.format(1)
        return submitter
    }

    private fun header(fileName: String, submitter: Submitter): Header {
        val header = Header()
        val characterSet = CharacterSet()
        characterSet.setCharacterSetName("UTF-8")
        val version = GedcomVersion()
        version.setVersionNumber("5.5.1")
        version.setGedcomForm("LINEAGE-LINKED")
        header.setFileName(fileName)
        val sourceSystem = SourceSystem()
        sourceSystem.setProductName("ahnenwin2gedcom")
        sourceSystem.setVersionNum(ApplicationProperties.version)
        sourceSystem.systemId = "AHN2GED"
        val submitterReference = SubmitterReference()
        submitterReference.submitter = submitter
        header.submitterReference = submitterReference
        header.setDestinationSystem("ANY")
        header.sourceSystem = sourceSystem
        header.characterSet = characterSet
        header.gedcomVersion = version
        header.setPlaceHierarchy("AhnenwinOrt")
        val noteStructure = NoteStructureFactory.create("created by ahnenwin2gedcom")
        header.getNoteStructures(true).add(noteStructure)
        return header
    }
}
