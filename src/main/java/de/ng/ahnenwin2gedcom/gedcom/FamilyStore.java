package de.ng.ahnenwin2gedcom.gedcom;

import org.gedcom4j.model.Family;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

class FamilyStore {
    private final XrefSequence familySequence = new XrefSequence();
    private final Map<HusbandWifeRef, Family> store = new HashMap<>();
    private final NoteStore noteStore;
    private final SourceStore sourceStore;

    FamilyStore(NoteStore noteStore, SourceStore sourceStore) {
        this.noteStore = noteStore;
        this.sourceStore = sourceStore;
    }

    Family createFamilyIfNotExists(HusbandWifeRef husbandWifeRef) {
        Family family = store.get(husbandWifeRef);
        if (family == null) {
            family = new FamilyBuilder(sourceStore, noteStore)
                    .xref(familySequence.next())
                    .husband(husbandWifeRef.getHusbandId())
                    .wife(husbandWifeRef.getWifeId())
                    .build();
            store.put(husbandWifeRef, family);
        }
        return family;
    }

    Family get(HusbandWifeRef husbandWifeRef) {
        return store.get(husbandWifeRef);
    }

    Map<String, Family> getAllWithXrefKey() {
        return store.values()
                .stream()
                .collect(Collectors.toMap(
                   Family::getXref,
                   family -> family));
    }
}
