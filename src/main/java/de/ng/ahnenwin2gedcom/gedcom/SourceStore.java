package de.ng.ahnenwin2gedcom.gedcom;

import org.gedcom4j.model.MultiStringWithCustomFacts;
import org.gedcom4j.model.Source;

import java.util.HashMap;
import java.util.Map;

class SourceStore {
    private final Map<String, Source> store = new HashMap<>();
    private final XrefSequence sourceSequence = new XrefSequence();

    Source createSource(String sourceTitle) {
        Source source = new Source();
        String xref = sourceSequence.next();
        source.setXref(xref);
        MultiStringWithCustomFacts title = new MultiStringWithCustomFacts();
        title.getLines(true).add(sourceTitle);
        source.setTitle(title);
        store.put(xref, source);
        return source;
    }

    Map<String, Source> getAll() {
        return store;
    }
}
