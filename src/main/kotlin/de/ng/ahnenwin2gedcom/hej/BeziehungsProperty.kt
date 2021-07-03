package de.ng.ahnenwin2gedcom.hej

enum class BeziehungsProperty constructor(
    val columnIndex: Int,
    val datatype: Class<*> = String::class.java
) {
    PARTNER_1(0, Int::class.java),
    PARTNER_2(1, Int::class.java),
    HOCHZEIT_TAG_KIRCHE(2),
    HOCHZEIT_MONAT_KIRCHE(3),
    HOCHZEIT_JAHR_KIRCHE(4),
    HOCHZEIT_ORT_KIRCHE(5),
    HOCHZEIT_TRAUZEUGEN_KIRCHE(6),
    HOCHZEIT_TAG_STANDESAMT(7),
    HOCHZEIT_MONAT_STANDESAMT(8),
    HOCHZEIT_JAHR_STANDESAMT(9),
    HOCHZEIT_ORT_STANDESAMT(10),
    HOCHZEIT_TRAUZEUGEN_STANDESAMT(11),
    VERBINDUNG(12, Verbindung::class.java),
    SCHEIDUNG_TAG(13),
    SCHEIDUNG_MONAT(14),
    SCHEIDUNG_JAHR(15),
    SCHEIDUNG_ORT(16),
    HOCHZEIT_QUELLE_KIRCHE(17),
    HOCHZEIT_QUELLE_STANDESAMT(18),
    SCHEIDUNG_QUELLE(19)
}
