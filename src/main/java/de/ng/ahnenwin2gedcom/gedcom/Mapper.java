package de.ng.ahnenwin2gedcom.gedcom;

import de.ng.ahnenwin2gedcom.csv.*;
import org.gedcom4j.model.Family;
import org.gedcom4j.model.Individual;

import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

class Mapper {
    private static final Predicate<Map.Entry<HusbandWifeRef, ?>> filterOutNoParentsRef =
            Predicate.not(entry -> entry.getKey().noParents());

    private final NoteStore noteStore;
    private final SourceStore sourceStore;
    private final FamilyStore familyStore;

    Mapper(NoteStore noteStore, SourceStore sourceStore) {
        this.noteStore = noteStore;
        this.sourceStore = sourceStore;
        this.familyStore = new FamilyStore(noteStore, sourceStore);
    }

    Map<String, Individual> toIndividuals(Collection<CsvAhne> csvAhnen) {
        return csvAhnen.stream()
                .map(this::toIndividual)
                .collect(Collectors.toMap(
                        Individual::getXref,
                        individual -> individual));
    }

    private Individual toIndividual(CsvAhne csvAhne) {
        return new IndividualBuilder(noteStore, sourceStore)
                .xref(csvAhne.getInt(AhnenColumn.NUMMER))
                .name(csvAhne.getString(AhnenColumn.VORNAMEN),
                        csvAhne.getString(AhnenColumn.NACHNAME),
                        csvAhne.getString(AhnenColumn.RUFNAME),
                        csvAhne.getString(AhnenColumn.SCHREIBWEISE))
                .sex(csvAhne.getGeschlecht())
                .religion(csvAhne.getString(AhnenColumn.RELIGION))
                .occupation(csvAhne.getString(AhnenColumn.BERUF))
                .birth(csvAhne.getString(AhnenColumn.GEBURTSTAG),
                        csvAhne.getString(AhnenColumn.GEBURTSMONAT),
                        csvAhne.getString(AhnenColumn.GEBURTSJAHR),
                        csvAhne.getString(AhnenColumn.GEBURTSORT),
                        csvAhne.getString(AhnenColumn.GEBURT_QUELLE))
                .baptism(csvAhne.getString(AhnenColumn.TAUFTAG),
                        csvAhne.getString(AhnenColumn.TAUFMONAT),
                        csvAhne.getString(AhnenColumn.TAUFJAHR),
                        csvAhne.getString(AhnenColumn.TAUFPATE),
                        csvAhne.getString(AhnenColumn.TAUFORT),
                        csvAhne.getString(AhnenColumn.TAUFE_QUELLE))
                .death(csvAhne.getString(AhnenColumn.STERBETAG),
                        csvAhne.getString(AhnenColumn.STERBEMONAT),
                        csvAhne.getString(AhnenColumn.STERBEJAHR),
                        csvAhne.getString(AhnenColumn.STERBEORT),
                        csvAhne.getString(AhnenColumn.STERBEQUELLE),
                        csvAhne.getString(AhnenColumn.TODESURSACHE))
                .burial(csvAhne.getString(AhnenColumn.BEERDIGUNG_TAG),
                        csvAhne.getString(AhnenColumn.BEERDIGUNG_MONAT),
                        csvAhne.getString(AhnenColumn.BEERDIGUNG_JAHR),
                        csvAhne.getString(AhnenColumn.BEERDIGUNG_ORT),
                        csvAhne.getString(AhnenColumn.BEERDIGUNG_QUELLE))
                .residence(csvAhne.getString(AhnenColumn.LEBENSORT))
                .aliveAsNote(csvAhne.getString(AhnenColumn.LEBT))
                .adoptedAsNote(csvAhne.getString(AhnenColumn.ADOPT))
                .source(csvAhne.getString(AhnenColumn.QUELLE))
                .address(csvAhne.getString(AhnenColumn.ADRESSE_1),
                        csvAhne.getString(AhnenColumn.ADRESSE_2),
                        csvAhne.getString(AhnenColumn.ADRESSE_ZUSATZ),
                        csvAhne.getString(AhnenColumn.PLZ),
                        csvAhne.getString(AhnenColumn.ADRESSE_ORT),
                        csvAhne.getString(AhnenColumn.HAUSNUMMER))
                .build();
    }

    Map<String, Family> toFamilies(Collection<CsvAhne> csvAhnen) {
        createFamiliesFromHusbandWifeRefs(csvAhnen);
        createFamiliesFromParentsRef(csvAhnen);
        addAsChildrenToFamilies(csvAhnen);
        addFamilyEvents(csvAhnen);
        return familyStore.getAllWithXrefKey();
    }

    private void createFamiliesFromHusbandWifeRefs(Collection<CsvAhne> ahnen) {
        for (var ahne : ahnen) createFamiliesFromHusbandWifeRefs(ahne);
    }

    private void createFamiliesFromHusbandWifeRefs(CsvAhne ahne) {
        husbandWifeRefs(ahne).forEach(familyStore::createFamilyIfNotExists);
    }

    private void createFamiliesFromParentsRef(Collection<CsvAhne> ahnen) {
        for (var ahne: ahnen) createFamilyFromParentsRef(ahne);
    }

    private void createFamilyFromParentsRef(CsvAhne ahne) {
        familyStore.createFamilyIfNotExists(parentsRef(ahne));
    }

    private void addFamilyEvents(Collection<CsvAhne> ahnen) {
        ahnen.stream()
                .map(CsvAhne::getPartnerVerbindungen)
                .flatMap(Set::stream)
                .collect(Collectors.groupingBy(Mapper::husbandWifeRef))
                .entrySet()
                .stream()
                .filter(entry -> entry.getKey().isPresent())
                .map(Map.Entry::getValue)
                .map(List::stream)
                .map(Stream::findAny)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .forEach(this::addFamilyEvents);
    }

    private void addFamilyEvents(CsvPartnerVerbindung partner) {
        husbandWifeRef(partner)
                .map(familyStore::get)
                .ifPresent(family -> addFamilyEvents(family, partner));
    }

    private void addFamilyEvents(Family family, CsvPartnerVerbindung partner) {
        Verbindung verbindung = partner.getVerbindung();
        FamilyBuilder familyBuilder = new FamilyBuilder(family, noteStore);
        switch (verbindung) {
            case EHESCHLIESSUNG:
                addKirchlicheHochzeit(familyBuilder, partner);
                addStandesamtlicheHochzeit(familyBuilder, partner);
                break;
            case VERLOBUNG:
                addEngagement(familyBuilder, partner);
                break;
            case ANDERE_BEZIEHUNG:
                addAndereBeziehung(familyBuilder, partner);
                break;
            case LEBENSGEMEINSCHAFT:
                addLebensgemeinschaft(familyBuilder, partner);
                break;
            case LEER:
            default:
        }
        addDivorce(familyBuilder, partner);
    }

    private static void addKirchlicheHochzeit(FamilyBuilder familyBuilder, CsvPartnerVerbindung partner) {
        familyBuilder.marriageKirchlich(
                partner.getString(VerbindungsColumn.HOCHZEITSTAG_KIRCHE),
                partner.getString(VerbindungsColumn.HOCHZEITSMONAT_KIRCHE),
                partner.getString(VerbindungsColumn.HOCHZEITSJAHR_KIRCHE),
                partner.getString(VerbindungsColumn.HOCHZEITSORT_KIRCHE));
    }

    private static void addStandesamtlicheHochzeit(FamilyBuilder familyBuilder, CsvPartnerVerbindung partner) {
        familyBuilder.marriageStandesamtlich(
                partner.getString(VerbindungsColumn.HOCHZEITSTAG_STANDESAMT),
                partner.getString(VerbindungsColumn.HOCHZEITSMONAT_STANDESAMT),
                partner.getString(VerbindungsColumn.HOCHZEITSJAHR_STANDESAMT),
                partner.getString(VerbindungsColumn.HOCHZEITSORT_STANDESAMT));
    }

    private static void addEngagement(FamilyBuilder familyBuilder, CsvPartnerVerbindung partner) {
        familyBuilder.engagement(
                partner.getString(VerbindungsColumn.HOCHZEITSTAG_KIRCHE),
                partner.getString(VerbindungsColumn.HOCHZEITSMONAT_KIRCHE),
                partner.getString(VerbindungsColumn.HOCHZEITSJAHR_KIRCHE),
                partner.getString(VerbindungsColumn.HOCHZEITSORT_KIRCHE));
    }

    private static void addAndereBeziehung(FamilyBuilder familyBuilder, CsvPartnerVerbindung partner) {
        familyBuilder.andereBeziehung(
                partner.getString(VerbindungsColumn.HOCHZEITSTAG_KIRCHE),
                partner.getString(VerbindungsColumn.HOCHZEITSMONAT_KIRCHE),
                partner.getString(VerbindungsColumn.HOCHZEITSJAHR_KIRCHE),
                partner.getString(VerbindungsColumn.HOCHZEITSORT_KIRCHE));
    }

    private static void addLebensgemeinschaft(FamilyBuilder familyBuilder, CsvPartnerVerbindung partner) {
        familyBuilder.lebensgemeinschaft(
                partner.getString(VerbindungsColumn.HOCHZEITSTAG_KIRCHE),
                partner.getString(VerbindungsColumn.HOCHZEITSMONAT_KIRCHE),
                partner.getString(VerbindungsColumn.HOCHZEITSJAHR_KIRCHE),
                partner.getString(VerbindungsColumn.HOCHZEITSORT_KIRCHE));
    }

    private static void addDivorce(FamilyBuilder familyBuilder, CsvPartnerVerbindung partner) {
        familyBuilder.divorce(
                partner.getString(VerbindungsColumn.SCHEIDUNG_TAG),
                partner.getString(VerbindungsColumn.SCHEIDUNG_MONAT),
                partner.getString(VerbindungsColumn.SCHEIDUNG_JAHR),
                partner.getString(VerbindungsColumn.SCHEIDUNG_ORT));
    }

    private void addAsChildrenToFamilies(Collection<CsvAhne> ahnen) {
        ahnen.stream()
                .collect(Collectors.groupingBy(Mapper::parentsRef))
                .entrySet()
                .stream()
                .filter(filterOutNoParentsRef)
                .forEach(this::addChildren);
    }

    private static HusbandWifeRef parentsRef(CsvAhne ahne) {
        return new HusbandWifeRef(ahne.getInt(AhnenColumn.VATER), ahne.getInt(AhnenColumn.MUTTER));
    }

    private static Set<HusbandWifeRef> husbandWifeRefs(CsvAhne ahne) {

        return ahne.getPartnerVerbindungen()
                .stream()
                .map(Mapper::husbandWifeRef)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .collect(Collectors.toSet());
    }

    private static Optional<HusbandWifeRef> husbandWifeRef(CsvPartnerVerbindung partner) {
        CsvAhne ahne = partner.getAhne();
        Geschlecht geschlecht = ahne.getGeschlecht();
        int ahnenNummer = ahne.getInt(AhnenColumn.NUMMER);
        Integer husbandId = switch (geschlecht) {
            case MAENNLICH -> ahnenNummer;
            case WEIBLICH -> partner.getPartnerNummer();
            case UNBEKANNT -> null;
        };
        Integer wifeId = switch (geschlecht) {
            case MAENNLICH -> partner.getPartnerNummer();
            case WEIBLICH -> ahnenNummer;
            case UNBEKANNT -> null;
        };
        return husbandId == null || wifeId == null
                ? Optional.empty()
                : Optional.of(new HusbandWifeRef(husbandId, wifeId));
    }

    private void addChildren(Map.Entry<HusbandWifeRef, List<CsvAhne>> children) {
        addChildren(children.getKey(), children.getValue());
    }

    private void addChildren(HusbandWifeRef husbandWifeRef, List<CsvAhne> children) {
        Family family = familyStore.get(husbandWifeRef);
        List<Integer> childIds = children.stream()
                .map(child -> child.getInt(AhnenColumn.NUMMER))
                .collect(Collectors.toList());
        new FamilyBuilder(family, noteStore)
                .addChildren(childIds);
    }
}
