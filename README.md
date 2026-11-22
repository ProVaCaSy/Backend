# ProVaCaSy Backend

Backend der **Professoren Vacation Calendar System** (ProVaCaSy) Webanwendung der Fakultät Informationstechnik der Hochschule Esslingen. Das System verwaltet die Abwesenheiten (Urlaub, Dienstreisen, Konferenzen usw.) von Professorinnen und Professoren, damit Sekretariat und Dekanat jederzeit sehen, wer wann erreichbar ist.

Projekt im Studiengang Software Engineering (SWB 4), WS 26/27. Team: Osman Sallamaci, Tommy Luu, Dominique Sluga. Kunde: Prof. Dr. Jörg Nitzsche.

## Technologien

| Bereich | Technologie |
| --- | --- |
| Sprache / Framework | Java 21, Spring Boot 4.1.1, Maven |
| Datenbank | PostgreSQL 17 (nur PostgreSQL, keine H2), Schema über Flyway-Migrationen |
| Persistenz | Spring Data JPA / Hibernate |
| Authentifizierung | Keycloak 26.8.1 (OAuth2 Resource Server, Token-Prüfung über JWKS) |
| API | API-First mit OpenAPI (`src/main/resources/openapi/provacasy-api.yaml`), Interfaces und DTOs per OpenAPI Generator beim Maven-Build, Zalando-Richtlinien |
| Mapping | MapStruct |
| Mail | Spring Mail, ICS-Dateien (lokal: MailHog) |
| Feiertage | externe Feiertags-API, gespeichert in der Datenbank, Spring Cache |
| Qualität | GitHub Actions, SonarQube, JaCoCo |

## Funktionen

- Abwesenheiten anlegen, ändern und löschen (Professoren für sich selbst, Sekretariat und Dekanat auch für andere)
- Genehmigungsablauf: Professoren-Einträge sind zunächst offen, Einträge von Sekretariat/Dekanat sind sofort genehmigt, Anträge können abgelehnt werden
- Schutz vor Doppelbuchungen (Service-Prüfung und Constraint in der Datenbank)
- Urlaubstage ohne Wochenenden und Feiertage; 24.12. und 31.12. zählen als ganze Feiertage
- Urlaubskontingent (Standard 30 Tage), Überschreitung wird nicht blockiert, sondern in der Statistik markiert; Anpassung des Kontingents durch Sekretariat/Dekanat
- Statistik und Export
- Mail mit ICS-Datei (Anlegen, Ändern, Absagen) und Deeplink
- Rollen: Professor, Sekretariat, Dekanat (aus den Keycloak-Gruppen `professors`, `staff`, `dekanat`)

## Projektstruktur

Basispaket `de.hs_esslingen.provacasy_backend`:

| Paket | Inhalt |
| --- | --- |
| `controller` | REST-Endpunkte |
| `service` | Fachlogik |
| `repository` | Datenzugriff (Spring Data JPA) |
| `model` | JPA-Entitäten und Enums |
| `dto` | API-Objekte (generiert) |
| `mapper` | Entität <-> DTO |
| `exception` | Exceptions und Fehlerbehandlung |
| `security` | Keycloak-Anbindung und Rollen |
| `config` | Technische Konfiguration |

Details und Regeln: [docs/architecture.md](docs/architecture.md).

## Lokal starten

Voraussetzungen: JDK 21, Maven, Docker.

1. Entwicklungsumgebung starten (PostgreSQL, Keycloak mit Realm `hse`, MailHog) mit der Compose-Datei `docker-compose.dev.yml` aus dem Deployment-Repository.
2. Backend starten:

   ```bash
   ./mvnw spring-boot:run
   ```

   Unter Windows (PowerShell): `.\mvnw.cmd spring-boot:run`

Das Backend läuft lokal auf http://localhost:8081, weil Keycloak in der Entwicklungsumgebung Port 8080 belegt. Im Container setzt `docker-compose.yml` den Port auf 8080.

Die Standardwerte in `application.properties` passen zur Entwicklungsumgebung. Sie lassen sich über Umgebungsvariablen überschreiben:

| Variable | Standard |
| --- | --- |
| `SERVER_PORT` | `8081` |
| `SPRING_DATASOURCE_URL` | `jdbc:postgresql://localhost:5432/provacasy` |
| `SPRING_DATASOURCE_USERNAME` | `provacasy` |
| `SPRING_DATASOURCE_PASSWORD` | `provacasy` |
| `SPRING_SECURITY_OAUTH2_RESOURCESERVER_JWT_ISSUER_URI` | `http://localhost:8080/realms/hse` |
| `SPRING_MAIL_HOST` | `localhost` |
| `SPRING_MAIL_PORT` | `1025` |

Das Datenbankschema legt Flyway beim Start an (`ddl-auto=validate`).

## Tests

```bash
mvn verify
```

Repository- und Integrationstests brauchen eine PostgreSQL-Instanz, da das Schema PostgreSQL-spezifische Funktionen nutzt.

## Lizenz

MIT
