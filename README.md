# Mehrschichtige und dezentrale Entscheidungsprozesse in Agentensystemen
Fachpraktikum Künstliche Intelligenz: Multiagentenprogrammierung SS 2022
Gruppe 3 \
Artificial Intelligence Group, FernUniversität in Hagen, Deutschland

## Teilnehmer Gruppe 3:
- H. Stadler
- M. Betz
- P. Heger
- B. Wladasch

## Motivation
Der [`Multi-Agent Programming Contest 2022`](https://multiagentcontest.org/) bildet die thematische
Grundlage für die in diesem Repository enthaltenen Agentensysteme. Es handelt
sich um zwei Varianten, basierend auf der BDI-Architektur, die das menschliche Denken und Schlussfolgern abstrahiert bzw. nachbildet. Beide Konzepte (Agentensystem V1 bzw. V2) unterscheiden sich konzeptionell durch unterschiedlich stark zentralisierte
Entscheidungsprozesse. Ziel des dualen Ansatzes soll sein, die Leistungsfähigkeit
beider Varianten zu prüfen und verschiedene Lösungsansätze zu erhalten, die
zwischen den Systemen ausgetauscht werden können.

## Voraussetzungen:
Die Agentensysteme wurden in der Programmiersprache JAVA implementiert.
Um die Agentensysteme auszuführen wird ein JDK >= 17 benötigt. 

## Programmstart
Um eine Simulation zu starten wird ein [`Simulationsserver`](https://github.com/agentcontest/massim_2022/blob/main/docs/server.md) benötigt.
Die Agentensysteme können mittels `java -jar group3-2022-1.0-jar-with-dependencies.jar` gestartet werden. Es ist darauf zu achten, dass sich im aktuellen Arbeitsverzeichnis ein Konfigurationsordner befindet. Eine Beispielkonfiguration kann dem `conf` Ordner im Hauptverzeichnis dieses Repositories entnommen werden.

Nachdem das Agentensystem gestartet wurde, kann die Agentenversion gewählt werden. Es stehen folgende Agenten zur Auswahl:
- BdiAgentV1
- BdiAgentV2

Eine Beschreibung der Agenten kann dem Projektpaper entnommen werden.

## Inhalt Repository:
- [`Ausführbare JAR`](https://github.com/h1Modeling/ss22_fp_mapc_gruppe3/tree/master/target)
- [`Beispielkonfiguration`](https://github.com/h1Modeling/ss22_fp_mapc_gruppe3/tree/master/)
- [`Projektseite mit technischer Dokumentation (JavaDoc)`](https://github.com/h1Modeling/ss22_fp_mapc_gruppe3/tree/master/target/site/)
- [`Projektpaper`](https://github.com/h1Modeling/ss22_fp_mapc_gruppe3/blob/master/documents/paper/gruppe3.pdf)
- [`Projektpräsentation`](https://github.com/h1Modeling/ss22_fp_mapc_gruppe3/blob/master/documents/presentation/presentation_gruppe3.pdf)
- [`Selbstständigkeitserklärungen`](https://github.com/h1Modeling/ss22_fp_mapc_gruppe3/tree/master/documents/declaration)






