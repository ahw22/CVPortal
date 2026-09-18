# Testprotokoll
## CVPortal – Nachweis der Testdurchführung

**Getestet von:** Andreas Zincke
**Datum:** 18.9.2026
**Backend-Version / Commit:** `1e40b894`

---

## Manuelle Testfälle (gemäß Pflichtenheft Abschnitt 9)

| # | Testfall | Ergebnis (✅/❌) | Datum   | Bemerkung                       |
|---|----------|:--------------:|---------|---------------------------------|
| 1 | Registrierung und JWT-Login |  ✅              | 18.9.26 |                                 |
| 2 | Geschützter Endpunkt ohne Token |     ✅           |   18.9.26      |                                 |
| 3 | CORS-Preflight-Request |           ✅     |   18.9.26      |                                 |
| 4 | Lebenslauf anlegen und abrufen |         ✅       |   18.9.26      |                                 |
| 5 | Öffentliche Visitenkarte ohne Login |       ✅         |    18.9.26     |                                 |
| 6 | Öffentlicher Lebenslauf (freigegeben) |         ✅       |  18.9.26       |                                 |
| 7 | Öffentlicher Lebenslauf (nicht freigegeben) |       ✅         |    18.9.26     |                                 |
| 8 | Admin sieht alle Profile |           ✅     |     18.9.26    |                                 |
| 9 | Teilnehmer kann nicht auf Admin-Endpunkt zugreifen |     ✅           |    18.9.26     |                                 |
| 10 | QR-Code generieren (W01) |    ❌            |   18.9.26      | Nicht erfülltes Wunschkriterium |

## Automatisierte Tests (JUnit 5)

### Testfaelle 11 und 12 laut Pflichtenheft Abschnitt 9

| #  | Testklasse         | Testfall                        | Ergebnis (OK/NOK) | Datum   |
|----|--------------------|---------------------------------|:-----------------:|---------|
| 11 | `TokenServiceTest` | JWT-Generierung und Validierung |        ✅           | 18.9.26 |
| 12 | `CvServiceTest`    | Vollstaendigkeitsgrad-Berechnung |        ✅          | 18.9.26 |

### Vollständige Testsuite Backend

Über die beiden geforderten Testfaelle hinaus umfasst das Backend 19 Testklassen mit 147 Testmethoden. Pfade relativ zu `backend/src/test/java/at/bbrz/cvportal/backend/`.

| Bereich | Testklasse | Testmethoden |
|---------|------------|:------------:|
| Security | `security/TokenServiceTest` | 6 |
|  | `security/JwtConfigTest` | 3 |
|  | `security/SecurityConfigTest` | 13 |
|  | `security/JpaUserDetailsServiceTest` | 8 |
|  | `security/UserPrincipalTest` | 8 |
| Services | `services/AuthServiceTest` | 14 |
|  | `services/CvServiceTest` | 18 |
| Controller | `controller/AuthControllerTest` | 12 |
|  | `controller/AuthFlowIntegrationTest` | 9 |
|  | `services/CvControllerTest` | 14 |
|  | `services/WorkExperienceControllerTest` | 11 |
| Repositories | `repositories/UserRepositoryTest` | 8 |
|  | `repositories/WorkExperienceRepositoryTest` | 5 |
|  | `repositories/EducationRepositoryTest` | 5 |
|  | `repositories/LanguageRepositoryTest` | 5 |
|  | `repositories/SkillRepositoryTest` | 5 |
| Sonstige | `BackendApplicationTests` | 1 |
|  | `exceptions/GlobalExceptionManagerTest` | 1 |
|  | `MiscTests` | 1 |
| **Summe** | **19 Klassen** | **147** |

Das Frontend enthaelt keine automatisierten Tests; es wird ueber die manuellen Testfaelle 1 bis 10 abgedeckt.

**Testlauf-Nachweis:** Screenshot/Log der `mvn test`-Ausgabe (alle Tests grün) beilegen.

![tests_passed.png](tests_passed.png)

## Zusammenfassung

- Anzahl Testfälle gesamt: 12
- Bestanden: 11 / 12
- Bekannte offene Punkte: Wunschkriterium
