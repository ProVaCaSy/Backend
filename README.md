# ProVaCaSy Backend

**ProVaCaSy** (*Professoren Vacation Calendar System*) ist ein zentrales, webbasiertes System zur Verwaltung von Urlaubs- und Abwesenheitstagen von Professorinnen und Professoren. Dieses Repository enthält das **Backend** (REST-API).

Entstanden im Rahmen des Software-Projekts SWB 4 (Wintersemester 2026/27) an der Hochschule Esslingen.

> **Status:** Das Projekt befindet sich im Aufbau

## Hintergrund

Abwesenheiten werden an der Fakultät bisher dezentral als Outlook-Kalendereinträge geführt. Dabei kann doppelt gebucht werden (und dann doppelt als Urlaubstag zählen), und es ist nicht auf einen Blick erkennbar, wie viele der 30 Urlaubstage pro Jahr bereits verplant sind oder wer sein Kontingent überschritten hat. ProVaCaSy löst das durch eine zentrale Verwaltung mit automatischer Kontingentberechnung und Validierung gegen Doppelbuchungen.

## Geplanter Funktionsumfang (MVP)

- Login über **Keycloak** mit automatischer Rollenerkennung (Professor, Sekretariat, Dekanat)
- **Professoren:** Urlaubsanträge und Abwesenheiten erstellen, bearbeiten und stornieren; Einsicht in den Team-Kalender
- **Sekretariat:** Gesamtkalender aller Abwesenheiten der Fakultät
- **Dekanat:** wie Sekretariat, zusätzlich Anträge prüfen, genehmigen oder ablehnen
- Dashboard mit persönlichem Urlaubskontingent und Antragsstatus
- Automatische Prüfung auf Doppelbuchungen im selben Zeitraum
- E-Mail-Benachrichtigungen mit **ICS-Dateien** für den Import in Outlook (inkl. stabiler IDs für Änderungen und Stornierungen)

Nice-to-have: Statistiken, PDF-Export, Outlook-Erweiterung.

## Tech-Stack

| Bereich | Technologie |
| --- | --- |
| Sprache / Framework | Java 21, Spring Boot 3.5.x |
| API | Spring Web, Bean Validation, springdoc-openapi (Swagger UI) |
| Sicherheit | Spring Security, OAuth2 Resource Server, Keycloak (OpenID Connect) |
| Datenhaltung | Spring Data JPA (Hibernate), PostgreSQL (Produktion), H2 (Entwicklung), Flyway |
| Mapping | MapStruct, Lombok |
| Tests / Qualität | JUnit, Spring Boot Test, JaCoCo, SonarQube |
| Betrieb | Docker, Docker Compose, GitHub Actions, GitHub Packages |

## Architektur

Das Backend folgt einer klassischen Schichtenarchitektur. Controller greifen nicht direkt auf die Datenbank zu, Daten werden über DTOs ausgetauscht und die Entitäten bleiben in der Service-Schicht.

```
Controller (REST, DTOs)  ->  Service (Fachlogik)  ->  Repository (Spring Data JPA)  ->  PostgreSQL
```

Geplante Paketstruktur:

```
config/       Security, OpenAPI, CORS
controller/   REST-Endpunkte
service/      Geschäftslogik (Kontingent, Doppelbuchungen, ICS-Versand)
repository/   Spring-Data-Repositories
model/        JPA-Entitäten
dto/          Request- und Response-Objekte
mapper/       MapStruct-Mapper
exception/    zentrale Fehlerbehandlung
```

Die vollständige Beschreibung steht in der [Dokumentation](https://github.com/ProVaCaSy/Documentation).

## Voraussetzungen

- Java 21 (JDK)
- Docker und Docker Compose
- Zugang zu einer Keycloak-Instanz (für lokale Entwicklung ist eine Instanz in Docker Compose vorgesehen)

## Lokal starten

```bash
git clone https://github.com/ProVaCaSy/Backend.git
cd Backend

# Entwicklungsprofil (H2-In-Memory-Datenbank)
./mvnw spring-boot:run -Dspring-boot.run.profiles=dev
```

Mit Docker Compose (PostgreSQL und Backend):

```bash
docker compose up -d
```

### Konfiguration

Einstellungen werden über Umgebungsvariablen gesetzt, damit keine Zugangsdaten im Repository liegen. Eine Vorlage liegt als `.env.example` bei, die echte `.env` wird nicht eingecheckt.

| Variable | Bedeutung |
| --- | --- |
| `SPRING_PROFILES_ACTIVE` | `dev` (H2) oder `prod` (PostgreSQL) |
| `SPRING_DATASOURCE_URL` | JDBC-URL der Datenbank |
| `SPRING_DATASOURCE_USERNAME` | Datenbankbenutzer |
| `SPRING_DATASOURCE_PASSWORD` | Datenbankpasswort |
| `SPRING_SECURITY_OAUTH2_RESOURCESERVER_JWT_ISSUER_URI` | Issuer-URL des Keycloak-Realms |

## API-Dokumentation

Wenn die Anwendung läuft, ist die Swagger-Oberfläche erreichbar unter:

```
http://localhost:8080/swagger-ui/index.html
```

## Tests und Code-Qualität

```bash
./mvnw verify
```

Der Befehl führt Unit- und Integrationstests aus und erzeugt den JaCoCo-Coverage-Report unter `target/site/jacoco/index.html`. Die CI-Pipeline (GitHub Actions) baut das Projekt, führt die Tests aus und übergibt die Ergebnisse an SonarQube.

## Zusammenarbeit

- Für jede Aufgabe gibt es ein Issue und einen eigenen Branch (z. B. `feature/12-absence-crud`).
- Commits folgen den *Conventional Commits* (`feat:`, `fix:`, `docs:`, `test:`, `chore:`).
- Änderungen gelangen nur per Pull Request in `main`. Voraussetzung sind eine erfolgreiche Pipeline und die Freigabe durch mindestens ein weiteres Teammitglied (Vier-Augen-Prinzip).
- Eine Aufgabe ist erledigt, wenn die Definition of Done erfüllt ist (Umsetzung, Tests, Dokumentation, Review, Aufwand im Issue erfasst).

## Weitere Repositories

- [Frontend](https://github.com/ProVaCaSy/Frontend)
- [Dokumentation](https://github.com/ProVaCaSy/Documentation)

## Team

Osman Sallamaci, Tommy Luu, Dominique Sluga

## Lizenz

Dieses Projekt steht unter der [MIT-Lizenz](LICENSE).