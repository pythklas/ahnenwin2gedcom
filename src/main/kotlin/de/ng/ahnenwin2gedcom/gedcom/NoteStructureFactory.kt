package de.ng.ahnenwin2gedcom.gedcom

import org.gedcom4j.model.NoteStructure

internal object NoteStructureFactory {
    fun create(vararg notes: String?): NoteStructure? {
        val notesNotNull = notes.filterNotNull()
        if (notesNotNull.isEmpty()) return null
        val structure = NoteStructure()
        structure.getLines(true).addAll(notesNotNull)
        return structure
    }
}
