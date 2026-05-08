# CVPortal – LAP Projektvorschlag
## Applikationsentwicklung - Coding | Betriebliches Projekt

---

## Projektidee

**Webbasiertes Lebenslauf- und Profilmanagementsystem** für BBRZ-Teilnehmer, inklusive öffentlicher Web-Visitenkarte mit QR-Code.

**Besonderheit:** Zwei getrennte Spring Boot Projekte (Backend-API + Frontend) mit CORS-Konfiguration.

**Technologiestack:**
- **Backend (Port 8080):** Java 21 · Spring Boot 4 · Spring Security · JWT · Spring Data JPA · H2 · ZXing
- **Frontend (Port 8081):** Java 21 · Spring Boot 4 · Thymeleaf · Bootstrap 5 · RestTemplate

---

## Enthaltene Dokumente

| Datei | Inhalt | Zweck |
|-------|--------|-------|
| `01_Anmeldung_betriebliches_Projekt.md` | Ausgefülltes Anmeldeformular | Bei Lehrlingsstelle einzureichen |
| `02_Pflichtenheft.md` | Vollständiges Pflichtenheft (alle 11 Abschnitte) | Bei Lehrlingsstelle einzureichen |
| `03_Projektplan_Zeitschaetzung.md` | Meilensteinplan + PSP mit Stundenaufstellung | Bei Lehrlingsstelle einzureichen |
| `04_Executive_Summary.md` | 2-seitige Projektbeschreibung nach den Leitfragen | Teil des Anmeldeformulars |

---

## Erfüllte Pflichtanforderungen

Eigenständig lauffähige Applikation (zwei Spring Boot Apps).  
Datenbankanbindung (H2 im Backend via JPA).  
Webbasiert und responsive (Thymeleaf + Bootstrap 5).  
Sicherheitskonzept (JWT, BCrypt, CORS-Whitelist, Rollen).  
Entwicklungssprache Java.  
Programmieraufwand ≥ 50 Stunden (geplant: ~54h rein Coding).  
Gesamtaufwand ca. 80 Stunden.

## Technische Highlights für die Prüfung

- **CORS live demonstrierbar** im Browser-Netzwerk-Tab (Preflight-Request sichtbar)
- **JWT-Flow** im Browser-DevTools (Authorization-Header, Token-Inhalt)
- **Öffentliche Visitenkarte** ohne Login aufrufbar – sofort beeindruckend in der Demo
- **QR-Code** auf der Visitenkarte (ZXing)

---

## Hinweis für Schüler

Name und Unterschriften in `01_Anmeldung_betriebliches_Projekt.md` sind individuell auszufüllen. Alle anderen Inhalte können als vollständige Vorlage für die Einreichung übernommen und angepasst werden.
