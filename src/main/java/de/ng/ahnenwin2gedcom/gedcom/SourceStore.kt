package de.ng.ahnenwin2gedcom.gedcom

import org.gedcom4j.model.MultiStringWithCustomFacts
import org.gedcom4j.model.Source

internal class SourceStore {
    private val store = mutableMapOf<String, Source>()
    private val sourceSequence = XrefSequence()

    fun createSource(sourceTitle: String): Source {
        val source = Source()
        val xref = sourceSequence.next()
        source.xref = xref
        val title = MultiStringWithCustomFacts()
        title.getLines(true).add(sourceTitle)
        source.title = title
        store[xref] = source
        return source
    }

    val all: Map<String, Source>
        get() = store
}
