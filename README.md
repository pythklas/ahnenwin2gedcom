# ahnenwin2gedcom

## English

This tool reads backup files of Ahnenwin 5 (.hej) and maps the data to Gedcom files (version 5.5.1).
I developed the tool specifically for my mother. It may not be useful for someone who used Ahnenwin 5 
fields differently than her. To get an idea of what fields get mapped how, take a look at the last section of 
this document. It is only maintained in German.

I do not distribute built images of this software. I will ignore any requests of this kind.

## German / Deutsch

Dieses Tool liest Backup-Dateien von Ahnenwin 5 (.hej) ein und erstellt daraus Gedcom-Dateien in Version 5.5.1.
Ich habe das Tool ausschließlich für meine Mutter programmiert. Für Leute, die die Felder in Ahnenwin 5 anders benutzt 
haben als meine Mutter, ist dieses Tool vermutlich nicht perfekt geeignet. Im letzten Abschnitt
dieses Dokuments pflege ich eine Tabelle, die zeigt, wie die Felder aus Ahnenwin 5 nach Gedcom abgebildet werden.

Ich stelle keine gebauten Artefakte der Software zur Verfügung. Anfragen dieser Art werden ignoriert.

## Ahnenwin 5 Backup erstellen
 * Starte _Ahnenwin 5.0_.
 * Klicke auf _Sicherung_ und anschließend auf _Daten sichern_.
 * Bestätige die Dialog-Box mit _Ja_.
 * Speichere die Datei mit dem gewünschten Namen im gewünschten Verzeichnis ab.
 * Warte, bis die Datensicherung abgeschlossen ist (bis die roten Zahlen verschwinden).
 * Überprüfe, dass die Datei die Endung _.hej_ hat.

## Gedcom-Datei erstellen
 * Kopiere eine oder mehrere hej-Dateien in das gleiche Verzeichnis, in dem sich _ahnwin-2-gedcom-\<version\>.exe_ befindet.
 * Starte _ahnwin-2-gedcom-\<version\>.exe_.
 * Für jede hej-Datei ist nun eine gleichnamige ged-Datei entstanden.
 * Es ist ggf. eine Datei names _errors.log_ entstanden. 
   Hier findet man eine Liste mit Problemen bzw. Fehlern, die es bei der Übertragung 
   der Daten in das Gedcom-Format gab.
 * Überprüfe, dass die entstandene Gedcom-Datei plausiblen Inhalt hat, z. B. so:
    * Öffne die Datei mit einem beliebigen Text-Editor, um zu prüfen, dass die Datei nicht leer ist.
    * Das Character-Encoding der Datei ist UTF-8. Gehe sicher, dass unter Verwendung dieses Encodings Sonderzeichen ('ä', 'ö', 'ü' , etc.) richtig dargestellt werden.
    * Importiere die Datei in ein gewünschtes Ahnenforschungsprogramm, das das Gedcom-Format (Version 5.5.1) unterstützt.
        * Das kostenlose Programm _Ancestris 10_ (https://www.ancestris.org) kann Gedcom-Dateien importieren.
        * In _Ancestris 10_ kann man sich Stammbäume anzeigen lassen und auch die Daten aller importierten Individuen überprüfen. 
   
## Getestete Programme

Folgende Programme können erfolgreich die von diesem Tool erzeugte Gedcom-Datei lesen:
 * Ancestris 10

## Windows Executable (.exe) bauen

### Systemvoraussetzungen
 * Windows (10?)
 * Eine Installation von Visual Studio (2017 oder neuer) mit Visual Studio C++ (Community Edition).
 * Maven 3
 * GraalVM Java 11 JDK mit native-image.exe (https://www.graalvm.org/docs/getting-started/windows/).


### Anleitung
 * Checke dieses Repository auf einem Windows-System aus.
 * Stelle sicher, dass die Umgebungsvariable JAVA_HOME auf die GraalVM Java 11 JDK gesetzt ist.
 * Starte die _x64 Native Tools Command Prompt for VS_, die bei der Installation von Visual Studio enthalten ist.
 * Navigiere in der Command Prompt in das ausgecheckte Projekt (_cd path\to\project_).
 * Führe den Befehl _mvn clean package -Pnative-image_ aus.
 * Die Build-Artefakte sind nun unter _/target_ zu finden.

## Executable Jar bauen

### Systemvoraussetzungen
 * Maven 3

### Anleitung
 * Öffne ein Terminal im Grundverzeichnis des Projekts.
 * Führe den Befehl _mvn clean package -Pexecutable-jar_ aus.
 * Die Build-Artefakte sind nun unter _/target_ zu finden.

## Docker container build

### Systemvorraussetzungen
 * Docker

### Anleitung
 * Öffne ein Terminal im Grundverzeichnis des Projekts.
 * Starte den Docker container mittels: `docker run --rm -it -v ${PWD}:/mnt/myproject:rw -w /mnt/myproject vegardit/graalvm-maven:latest-java11 mvn clean package -Pnative-image`
 * Die Linux Build-Artefakte sind nun unter _/target_ zu finden.

## Was wird auf was abgebildet? (noch nicht fertig / still in progress)

In folgender Tabelle wird abgebildet, welche Attribute eines Ahnen aus Ahnenwin 5 auf welche Attribute eines Individuals in Gedcom abgebildet werden.

| Ahnenwin 5                                                  | Gedcom-Keyword                                                                                                                                                                                                   | Anmerkung                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                          |
|-------------------------------------------------------------|------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| Nummer                                                      | @\<Nummer\>@ INDI                                                                                                                                                                                                |                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                    |
| Vornamen                                                    | NAME \<Vornamen\> /\<Nachname\>/<br>  und<br>  GIVN \<Vornamen\> unter NAME                                                                                                                                      |                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                    |
| Nachname                                                    | NAME \<Vornamen\> /\<Nachname\>/<br>  und<br>  SURN \<Nachname\> unter NAME                                                                                                                                      |                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                    |
| Rufname                                                     | NICK \<Rufname\> unter NAME                                                                                                                                                                                      |                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                    |
| Geschlecht                                                  | SEX \<M oder F\>                                                                                                                                                                                                 | 'm' und 'M' werden auf 'M' abgebildet.<br> 'w' und 'W' werden auf 'F' abgebildet.<br> Alle anderen Einträge aus Ahnenwin 5 werden verworfen.                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                       |
| Religion                                                    | RELI \<Religion\>                                                                                                                                                                                                |                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                    |
| Beruf                                                       | OCCU \<Beruf\>                                                                                                                                                                                                   |                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                    |
| Geburtstag<br> -monat<br> -jahr<br> -ort -quelle            | DATE \<Geburtsdatum\> unter BIRT<br> PLAC \<Geburtsort\> unter BIRT<br> SOUR @\<CrossReference\>@ unter BIRT<br> <br> 0 @\<CrossReference\>@ SOUR<br> 1 TITL \<Geburtsquelle\>                                   | Geburtstag, -monat und -jahr werden in das Gedcom-konforme Datumsformat (d MMM y) übertragen.  MMM ist dabei die englische Lokalisation. (JAN, FEB, MAR, etc.) <br> <br> Ungültige Datumseinträge werden in ein NOTE ausgelagert, weil sie fachlich wertvolle Informationen enthalten könnten.<br> <br> Beim Geburtsort wird jedes ',' durch ';' ersetzt. Die Gedcom-Datei ist so konfiguriert, dass sie bei Orten keine Hierarchie erlaubt.  Dafür wird im Header der Datei folgendes Ortsschema definiert:<br> 1 PLAC<br> 2 FORM AhnenwinOrt<br> Die CrossReference für die Quelle wird dynamisch erzeugt und hat nur eine technische Bedeutung. |
| Tauftag<br> -monat<br> -jahr<br> -ort<br> -pate<br> -quelle | DATE \<Taufdatum\> unter BAPM<br> PLAC \<Taufort\> unter BAPM<br> NOTE Taufpate: \<Taufpate\> unter BAPM<br> SOUR @\<CrossReference\>@ unter BAPM<br> <br> 0 @\<CrossReference\>@ SOUR<br> 1 TITL \<Taufquelle\> | siehe Geburtstag.<br> Für den Taufpate ist in Ahnenwin 5 ein Textfeld und keine Referenz auf einen anderen Ahneneintrag. Deswegen kann sie hier nur als NOTE mit Text realisiert werden.                                                                                                                                                                                                                                                                                                                                                                                                                                                           |
|                                                             |                                                                                                                                                                                                                  |                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                    |
