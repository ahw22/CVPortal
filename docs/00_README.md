# CVPortal – LAP Projektvorschlag
## Applikationsentwicklung - Coding | Betriebliches Projekt

---

## Projektidee

**Webbasiertes Lebenslauf- und Profilmanagementsystem** für BBRZ-Teilnehmer, inklusive öffentlicher Web-Visitenkarte (optional mit QR-Code, Wunschkriterium W01).

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
| `05_Testprotokoll.md` | Vorlage für Testnachweis (auszufüllen nach Testdurchführung) | Nachweis für Prüfungskommission |

---

## Erfüllte Pflichtanforderungen

Eigenständig lauffähige Applikation (zwei Spring Boot Apps).  
Datenbankanbindung (H2 im Backend via JPA).  
Webbasiert und responsive (Thymeleaf + Bootstrap 5).  
Sicherheitskonzept (JWT, Argon2id, CORS-Whitelist, Rollen).  
Entwicklungssprache Java.  
Programmieraufwand ≥ 50 Stunden (geplant: ~57h reine Coding-Phasen, siehe Projektplan).  
Gesamtaufwand ca. 98 Stunden.

## Technische Highlights für die Prüfung

- **CORS live demonstrierbar** im Browser-Netzwerk-Tab: Der Sichtbarkeits-Toggle (öffentlich/privat) ruft das Backend direkt per `fetch()` auf – dort ist der Preflight-Request (`OPTIONS`) sichtbar. Alle anderen Seiten laufen serverseitig über RestTemplate und sind davon nicht betroffen.
- **JWT-Flow** im Browser-DevTools: Token wird serverseitig in der Frontend-`HttpSession` gehalten; auf der CV-Bearbeitungsseite zusätzlich im HTML sichtbar (`data-jwt`-Attribut) sowie im `Authorization`-Header des direkten Backend-Aufrufs beim Sichtbarkeits-Toggle.
- **Öffentliche Visitenkarte** ohne Login aufrufbar – sofort beeindruckend in der Demo
- **QR-Code** auf der Visitenkarte (ZXing, Wunschkriterium W01)
