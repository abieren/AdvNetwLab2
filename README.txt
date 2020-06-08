AUFGABEN
========
Die Programm-Dateien der Aufgabe2-Teil1, Aufgabe2-Teil2 und Aufgabe3 befinden sich alle im Ordner "advanced_networking_lab". Die beiden Teile der Aufgabe 2 finden sich im Unterordner "exercise3" und die der Aufgabe 5 in "exercise 5".

INSTALLATION DER AUFGABE 5
==========================
Das Packen des Controller-Programmes zu einer ausführbaren JAR-Datei hat leider nicht funktioniert.

Zur Ausführung wird der Ordner "advanced_networking_lab" im Projekt in den Ordner "src/main/java" eingefügt. Zusätzlich müssen im Ordner "src/main/resources" noch die beiden Dateien "floodlightdefault.properties" und "META-INF/services/net.floodlightcontroller.core.module.IFloodlightModule", wie im Laborbericht beschrieben so angepasst werden, dass Floodlight das implementerte Modul ausführen kann. Der Klassenname des Moduls lautet hierbei "advanced_networking_lab.exercise5.Exercise5". Um den Aufwand zu veringern finden sich diese beiden schon angepassten Dateien im Ordner "src_main_resources".

TESTPROTOKOLL DER AUFGABE 5
===========================
Die Aufnahmen des Testprotokolls ist im Ordner "logs" zu finden. Die einzelnen Aufnahmen sind nach dem entsprechenden Schritt benannt.

Das Controller-Programm gibt zur besseren Lesbarkeit weitere Nachrichten aus, als von der Laboranleitung gefordert ist.

Es wurden folgende Schritte aufgezeichnet:
1) Startup Controller
2) Startup Mininet
3) pingall
4) link S1 S2 down; S1 ip link show S1-eth2
5) pingall
6) link S1 S2 up; S1 ip link show S1-eth2
7) pingall
8) Close Mininet

