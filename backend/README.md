# CVPortal - Backend

REST-Backend des LAP-Projekts CVPortal (Lebenslaufmanagementsystem).
Springboot 4.1 - Java 21 - H2 - JWT - Argon2id

Das Frontend liegt im Schwesterproject `cvportal-frontend` (Port 8081) und ruft dieses Backend auf.

## Starten

Vorraussetzung JDK 21. Maven wird mitgeliefert.

```bash
cd backend
./mvnw spring-boot:run # Windows: mvnw.cmd spring-boot:run
```

Dann ist erreichbar:

| Was          | URL                                   |
|--------------|---------------------------------------|
| API          | http://localhost:8080/api             |
| Swagger UI   | http://localhost:8080/swagger-ui.html |
| OpenAPI-Spec | http://localhost:8080/v3/api-docs     |
| H2-Konsole   | http://localhost:8080/h2-console      |

H2-Konsole: JDBC-URL= `jdbc:h2:file:./data/cvportal`, Benutzer `sa`, kein Passwort.

### Datenbank

Dateibasierte H2 Datenbank gespeichert in `backend/data/cvportal.mv.db`, per `.gitignore` aus dem Repo ausgenommen.
Das Schema wird via Hibernate erzeugt, `data.sql` läuft bei **jedem** Start und legt Testbenutzer per `MERGE` an.
Vorhandene Daten bleiben erhalten.

Zum zurücksetzen der Datenbank einfach die Applikation beenden und `backend/data/cvportal.mv.db` löschen und dann neu starten.

### Konfiguration

| Umgebungsvariable     | Default                                      | Zweck                               |
|-----------------------|----------------------------------------------|-------------------------------------|
| `CVPORTAL_JWT_SECRET` | Entwicklungsschlüssel aus `application.yaml` | HMAC-Schlüssel für die JWT Signatur |

## Testbenutzer

Aus `src/main/resources/data.sql` :

| Benutzername | Passwort      | Rolle      |
|--------------|---------------|------------|
| `admin`      | `admin12345`  | ADMIN      |
| `muster`     | `muster12345` | TEILNEHMER |
| `beispiel` | `muster12345` | TEILNEHMER |

`beispiel` hat einen vollen Musterlebenslauf.


## API testen

1. `POST /api/auth/login` mit `{"username":"muster","password":"muster12345"}`
2. `token` aus der Antwort kopieren
3. Im Swagger-UI oben rechts auf **Authorize** klicken, Token einfügen (ohne `Bearer `)
4. Die `/api/cv/me/**`  Endpunkte sind jetzt aufrufbar

Der selbe Vorgang funktioniert auch mit dem admin Testnutzer.

## Endpunkte im Überblick

Vollständige Beschreibung inklusive Schemata im Swagger UI.

| Bereich                 | Endpunkte                                                                                                                   | Zugriff                          |
|-------------------------|-----------------------------------------------------------------------------------------------------------------------------|----------------------------------|
| Auth                    | `POST /api/auth/register`, `POST /api/auth/login`                                                                           | public                           |
| Lebenslauf (eigener)    | `GET/PUT /api/cv/me`, `PUT /api/cv/me/visibility`                                                                           | TEILNEHMER                       |
| Berufserfahrung         | `GET/POST /api/cv/me/work-experience` , `PUT/DELETE .../{id}                                                                | TEILNEHMER                       |
| Ausbildung              | `GET/POST /api/cv/me/education` , `PUT/DELETE .../{id}                                                                      | TEILNEHMER                       |
| Skills                  | `GET/POST /api/cv/me/skills` , `PUT/DELETE .../{id}                                                                         | TEILNEHMER                       |
| Sprachen                | `GET/POST /api/cv/me/languages` , `PUT/DELETE .../{id}                                                                      | TEILNEHMER                       |
| Öffentlicher Lebenslauf | `GET /api/cv/public/{username}`                                                                                             | public wenn vom User freigegeben |
| Visitenkarte            | `GET /api/card/{username}`                                                                                                  | public                           |
| Admin                   | `GET /api/admin/participants`, `GET /api/admin/users`, `PUT /api/admin/users/{id}/active`, `PUT /api/admin/users/{id}/role` | ADMIN                            |

### Fehlerformat

Alle Fehler laufen über den `GlobalExceptionsManager` und folgen den RFC 9457 (`Problemdetail`) Standard. Validierungsfehler
enthalten zusätzlich `fieldErrors`: 

```json
{
  "detail": "Die Eingabe ist unvollständig oder ungültig",
  "instance": "/api/cv/me/skills",
  "status": 400,
  "title": "Validierungsfehler",
  "timestamp": "2026-09-04T08:57:33.951064900Z",
  "fieldErrors": {
    "name": "must not be blank"
  }
}
```

## Sicherheit

- Passwörter: Argon2id (`Argon2PasswordEncoder`) encoded, niemals im Klartext abgespeichert
- JWT als Bearer-Token mit Rolle im Claim `role` -> Authority `ROLE_ADMIN` / `ROLE_TEILNEHMER`
- Öffentlich zugängliche Pfade ohne token: Registrierung, Login, Visitenkarte, Öffentlicher Lebenslauf, Swagger, H2-Konsole
- Besitzprüfung der Lebenslauf-Abschnitte über die Repository Abfrage (`findByIdAndCv_User_Id`), nicht Über eine nachgelagerte Prüfung

### CORS

Nur `PUT /api/cv/me/visibility` wird direkt vom Browser des Benutzers via Browser `fetch()` aufgerufen und löst damit 
einen CORS-Preflight aus. Alle anderen Aufrufe laufen serverseitung womit die Same-Origin-Policy nicht greift. 
Konfiguriert in `SecurityConfig`, erlaube Origin `http://localhost:8081`.

## Tests

```bash
./mvnw test
```

