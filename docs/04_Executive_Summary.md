# Executive Summary
## CVPortal – Webbasiertes Lebenslauf- und Profilmanagementsystem für BBRZ-Teilnehmer

---

## Das Problem

BBRZ-Teilnehmer erarbeiten im Rahmen ihrer Ausbildung einen Lebenslauf – eines der wichtigsten Dokumente für den Wiedereinstieg in den Arbeitsmarkt. Die Realität sieht jedoch oft so aus:

- Lebensläufe existieren als lokale Word-Dateien ohne einheitliche Struktur
- Die Weitergabe erfolgt per E-Mail-Anhang – keine einfache, digitale Teilungsmöglichkeit
- Berater haben keinen zentralen Überblick über den Dokumentationsstand ihrer Teilnehmer
- Es gibt keine Möglichkeit, den Lebenslauf schnell und unkompliziert einem potenziellen Arbeitgeber digital zu zeigen – etwa beim Netzwerken oder auf einer Jobmesse

Für Teilnehmer, die den Schritt zurück in den Arbeitsmarkt wagen, ist eine professionelle, leicht zugängliche digitale Präsenz ein echter Wettbewerbsvorteil.

---

## Die Lösung

**CVPortal** ist eine webbasierte Plattform, auf der BBRZ-Teilnehmer ihre Lebensläufe strukturiert erfassen, verwalten und digital teilen können.

### Architektur: Zwei getrennte Spring Boot Anwendungen

Das Besondere an CVPortal aus technischer Sicht ist die **klare Trennung in Backend und Frontend** als zwei eigenständige Spring Boot Applikationen:

```
cvportal-backend  (Port 8080)  →  REST-API, Datenbank, Authentifizierung
cvportal-frontend (Port 8081)  →  Benutzeroberfläche, kommuniziert über REST
```

Diese Architektur erfordert eine explizite **CORS-Konfiguration** (Cross-Origin Resource Sharing) im Backend – ein reales Konzept aus der modernen Webentwicklung, das im Projektalltag allgegenwärtig ist.

### Web-Visitenkarte: Das Aushängeschild

Jeder Teilnehmer erhält eine **persönliche Web-Visitenkarte** unter der URL `/card/{benutzername}`:
- Immer öffentlich zugänglich – kein Login nötig
- Zeigt Name, Berufsbezeichnung, Kurzprofil und Kontaktdaten
- Enthält einen **QR-Code** (Wunschkriterium W01), der auf diese Seite verweist – ideal für Jobmessen oder Bewerbungsunterlagen
- Link zum vollständigen Lebenslauf (sofern vom Teilnehmer freigegeben)

---

## Kernfunktionen (Pflicht)

| Funktion | Beschreibung |
|----------|-------------|
| Strukturierter Lebenslauf | Berufserfahrung, Ausbildung, Kenntnisse, Sprachen |
| Web-Visitenkarte | Öffentliche Profilseite mit Kontaktdaten |
| Öffentlicher CV-Link | Freigebbarer Lebenslauf per URL |
| JWT-Authentifizierung | Sichere tokenbasierte Anmeldung |
| CORS-Architektur | Zwei getrennte Apps, professionell integriert |
| Admin-Übersicht | Berater sehen alle Teilnehmerprofile |

## Erweiterungen (Wunschkriterien)

| Funktion | Beschreibung |
|----------|-------------|
| QR-Code | Auf der Visitenkarte, verweist auf deren URL (W01) |
| PDF-Export | Lebenslauf als Datei herunterladen (W02) |

---

## Technologiestack

| Bereich | Technologie |
|---------|------------|
| Sprache | Java 21 |
| Backend-Framework | Spring Boot 4, Spring Security 7, Spring Data JPA |
| Authentifizierung | JWT (jjwt-Bibliothek) |
| Datenbank | H2 (embedded, Datei-Modus) |
| Frontend-Framework | Spring Boot 4 + Thymeleaf + Bootstrap 5 |
| QR-Code | ZXing Core |
| Build | Maven |
| Tests | JUnit 5, Mockito, Spring Boot Test / MockMvc |

---

## Warum CVPortal für die LAP?

Das Projekt verbindet praxisrelevante Themen der modernen Softwareentwicklung mit einem realen Anwendungsfall aus dem BBRZ-Umfeld:

- **CORS** ist ein fundamentales Konzept in der Webentwicklung – selten so klar demonstrierbar wie hier
- **JWT-Authentifizierung** ist industriestandard in REST-APIs
- **Zwei-Projekt-Architektur** spiegelt reale Microservice-Ansätze wider
- **Web-Visitenkarte** zeigt kreative Produktgestaltung über die bloße Datenverwaltung hinaus
- Alle Pflichtanforderungen der LAP sind erfüllt (eigenständige App, Datenbank, responsives Web, Sicherheit, Java, ≥ 50h Programmieraufwand)

---

*Dieses Projekt erfüllt alle Pflichtanforderungen des betrieblichen Projekts nach § 11 der Ausbildungsordnung Applikationsentwicklung – Coding: eigenständig lauffähige Applikation, Datenbankanbindung (H2/JPA), webbasiert und responsive (Bootstrap 5), Sicherheitskonzept (JWT, Argon2id, CORS), Entwicklungssprache Java, Programmieraufwand ≥ 50 Stunden.*
