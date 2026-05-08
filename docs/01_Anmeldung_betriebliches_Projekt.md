# Anmeldung zur Lehrabschlussprüfung
## Praktische Prüfung – Applikationsentwicklung - Coding
### Betriebliches Projekt

---

**Name des Prüfungskandidaten / der Prüfungskandidatin:**

> *Andreas Zincke*

---

**Titel des Projektes:**

> **CVPortal – Webbasiertes Lebenslauf- und Profilmanagementsystem für BBRZ-Teilnehmer**

---

## Executive Summary / Kurze Beschreibung des zu erstellenden Programmes

### Welches Problem soll gelöst werden?

BBRZ-Teilnehmer absolvieren Aus- und Weiterbildungsmaßnahmen mit dem Ziel der (Wieder-)Eingliederung in den Arbeitsmarkt. Ein zentrales Ergebnis dieser Ausbildungen ist ein professioneller Lebenslauf. Aktuell erstellen Teilnehmer ihre Lebensläufe lokal in Word oder ähnlichen Programmen – ohne einheitliche Struktur, ohne einfache Teilungsmöglichkeit und ohne digitale Sichtbarkeit. **CVPortal** löst dieses Problem durch eine zentrale, webbasierte Plattform, auf der Lebensläufe strukturiert erfasst, verwaltet und als öffentliche Web-Visitenkarte geteilt werden können.

### Wer hat dieses Problem?

BBRZ-Teilnehmer, die ihren Lebenslauf potenziellen Arbeitgebern unkompliziert digital vorzeigen möchten. Gleichzeitig profitieren BBRZ-Berater, die den Fortschritt der Teilnehmer überblicken und bei der Lebenslaufgestaltung unterstützen möchten.

### USP gegenüber bestehenden Lösungen

Plattformen wie LinkedIn oder Xing erfordern eine öffentliche Registrierung, sind auf Englisch ausgerichtet und für Teilnehmer in der Wiedereingliederung oft eine Hemmschwelle. **CVPortal** ist:
- **BBRZ-intern** – keine öffentliche Social-Media-Präsenz nötig
- **niederschwellig** – einfache strukturierte Eingabe, kein Design-Know-how nötig
- **teilbar** – jeder Teilnehmer erhält eine persönliche Web-Visitenkarte mit QR-Code
- **mehrsprachig vorbereitet** – Inhalte können auf Deutsch und Englisch erfasst werden
- **technisch modern** – getrennte Backend/Frontend-Architektur mit REST-API

### Kernelemente der Lösung

- **Backend-API** (Spring Boot, Port 8080): REST-Endpunkte für alle Datenzugriffe, CORS-konfiguriert
- **Frontend** (separates Spring Boot Projekt, Port 8081): Benutzeroberfläche mit Thymeleaf und Bootstrap 5, kommuniziert ausschließlich über die REST-API mit dem Backend
- **Web-Visitenkarte**: QR-Code-Link zu öffentlich zugängliche Profilseite pro Teilnehmer mit vollständigem Lebenslauf
- **Rollenkonzept**: Admin (Berater), Teilnehmer – jeweils mit eigenem Funktionsumfang
- **Sicherheit**: JWT-basierte Authentifizierung, CORS-Policy, Eingabevalidierung

### Zielgruppe / Anwender

BBRZ-Teilnehmer als primäre Nutzer, BBRZ-Berater als Administratoren, potenzielle Arbeitgeber als externe Betrachter der öffentlichen Visitenkarten.

---

## Hinweis zur Einreichung

Mit diesem Dokument werden folgende Unterlagen bei der Lehrlingsstelle eingereicht:
Pflichtenheft (`02_Pflichtenheft.md`)  
Projektplan mit Zeitschätzung (`03_Projektplan_Zeitschaetzung.md`)

Der Quellcode beider Teilprojekte wird spätestens 3 Wochen vor dem Prüfungstermin offengelegt.

---

**Ort, Datum:** _________________________, den _________________________

**Name und Unterschrift des Prüfungskandidaten / der Prüfungskandidatin:**

_______________________________________________

**Name und Unterschrift des Lehrberechtigten:**

_______________________________________________
