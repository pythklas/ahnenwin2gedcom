package de.ng.ahnenwin2gedcom.gedcom

import de.ng.ahnenwin2gedcom.hej.AhnenProperty
import de.ng.ahnenwin2gedcom.hej.HejAhne
import org.gedcom4j.model.Individual
import java.util.function.Consumer

internal class IndividualMapper(private val sourceStore: SourceStore, private val individualStore: IndividualStore) {
    fun map(ahnen: Collection<HejAhne>): Map<String, Individual> {
        for (ahne in ahnen) createIndividual(ahne)
        return individualStore.all
    }

    private fun createIndividual(ahne: HejAhne) {
        val individual = IndividualBuilder(sourceStore)
            .xref(ahne.getInt(AhnenProperty.NUMMER))
            .name(
                ahne.getString(AhnenProperty.VORNAMEN),
                ahne.getString(AhnenProperty.NACHNAME),
                ahne.getString(AhnenProperty.RUFNAME),
                ahne.getString(AhnenProperty.SCHREIBWEISE)
            )
            .sex(ahne.geschlecht)
            .religion(ahne.getString(AhnenProperty.RELIGION))
            .occupation(ahne.getString(AhnenProperty.BERUF))
            .birth(
                ahne.getString(AhnenProperty.GEBURTSTAG),
                ahne.getString(AhnenProperty.GEBURTSMONAT),
                ahne.getString(AhnenProperty.GEBURTSJAHR),
                ahne.getString(AhnenProperty.GEBURTSORT),
                ahne.getString(AhnenProperty.GEBURT_QUELLE)
            )
            .baptism(
                ahne.getString(AhnenProperty.TAUFTAG),
                ahne.getString(AhnenProperty.TAUFMONAT),
                ahne.getString(AhnenProperty.TAUFJAHR),
                ahne.getString(AhnenProperty.TAUFPATE),
                ahne.getString(AhnenProperty.TAUFORT),
                ahne.getString(AhnenProperty.TAUFE_QUELLE)
            )
            .death(
                ahne.getString(AhnenProperty.STERBETAG),
                ahne.getString(AhnenProperty.STERBEMONAT),
                ahne.getString(AhnenProperty.STERBEJAHR),
                ahne.getString(AhnenProperty.STERBEORT),
                ahne.getString(AhnenProperty.STERBEQUELLE),
                ahne.getString(AhnenProperty.TODESURSACHE)
            )
            .burial(
                ahne.getString(AhnenProperty.BEERDIGUNG_TAG),
                ahne.getString(AhnenProperty.BEERDIGUNG_MONAT),
                ahne.getString(AhnenProperty.BEERDIGUNG_JAHR),
                ahne.getString(AhnenProperty.BEERDIGUNG_ORT),
                ahne.getString(AhnenProperty.BEERDIGUNG_QUELLE)
            )
            .residence(ahne.getString(AhnenProperty.LEBENSORT))
            .aliveAsNote(ahne.getString(AhnenProperty.LEBT))
            .hofnameAsNote(ahne.getString(AhnenProperty.HOFNAME))
            .phoneNumber(ahne.getString(AhnenProperty.TELEFON))
            .source(ahne.getString(AhnenProperty.QUELLE))
            .address(
                ahne.getString(AhnenProperty.ADRESSE_1),
                ahne.getString(AhnenProperty.ADRESSE_2),
                ahne.getString(AhnenProperty.ADRESSE_ZUSATZ),
                ahne.getString(AhnenProperty.PLZ),
                ahne.getString(AhnenProperty.ADRESSE_ORT)
            )
            .textAsNote(ahne.getStringArray(AhnenProperty.TEXT))
            .build()
        individualStore.add(individual)
    }
}
