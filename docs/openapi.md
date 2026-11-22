# ProVaCaSy – Erklärung der OpenAPI-Spezifikation

**Datei:** `src/main/resources/openapi/provacasy-api.yaml`  
**Stand:** 20.11.2026  

## Aufbau und Funktionsweise der Datei

```yaml
openapi: 3.0.3
info:
  title: ProVaCaSy API
  version: 0.1.0
servers:
  - url: /api
security:
  - bearerAuth: []
paths:
  /users/me:
    get:
      # ...
components:
  securitySchemes:
    bearerAuth:
      type: http
      scheme: bearer
      bearerFormat: JWT
```

| Bereich | Bedeutung |
|---|---|
| `openapi` | Version des OpenAPI-Dokumentformats (nicht Spring Boot). |
| `info` | Titel, Version und allgemeine Beschreibung des API-Vertrags. |
| `servers` | Gemeinsamer Präfix `/api`. **Achtung:** `paths: /users/me` ergibt im Browser `/api/users/me`. |
| `tags` | Sortierung der Endpunkte nach fachlichem Bereich in der API-Dokumentation. |
| `security` | Alle Operationen verwenden ein Bearer-JWT von Keycloak. |
| `paths` | HTTP-Endpunkte mit Parametern, Request-Body, Rückgaben und Fehlerfällen. |
| `components.schemas` | Wiederverwendbare Typen und Datenmodelle. |
| `components.parameters` | Gemeinsam verwendete Query- und Path-Parameter. |
| `components.responses` | Einheitliche Fehlerbeschreibungen mit `application/problem+json`. |
| `$ref` | Verweist auf einen anderen Teil der YAML, damit ein Schema nicht mehrfach definiert werden muss. |

**Beispiel:** `POST /absences` verweist mit `$ref: '#/components/schemas/AbsenceCreateRequest'` auf die Definition seines JSON-Request-Bodys. Die Antwort `201` verwendet `Absence`, also das vollständige gespeicherte Objekt. Wenn Felder geändert werden, müssen Backend und generierter Frontend-Client dem Vertrag entsprechen.

## Alle API-Operationen

Alle hier aufgeführten Pfade haben den Basispräfix `/api`. In der folgenden Übersicht bezeichnen P = `PROFESSOR`, S = `SECRETARIAT` und D = `DEAN`.

| Methode | Pfad | Rolle | Aufgabe | Wesentliche Rückgabe |
|---|---|---|---|---|
| GET | `/users/me` | gültiger Keycloak-Login, auch ohne Fachrolle | Eigenes Profil; bei Erstlogin lokal anlegen | `User` |
| GET | `/users?search=...` | S, D | Benutzer suchen | `User[]` |
| GET | `/absences?from=&to=&status=` | P, S, D | Abwesenheiten im Zeitraum anzeigen | `Absence[]` |
| POST | `/absences` | P, S, D | Abwesenheit erstellen | `201 Absence` |
| PUT | `/absences/{id}` | P, S, D | Abwesenheit ändern | `Absence` |
| DELETE | `/absences/{id}` | P, S, D | Abwesenheit löschen | `204` |
| POST | `/absences/{id}/approve` | D | Offenen Antrag genehmigen | `Absence` |
| POST | `/absences/{id}/reject` | D | Antrag ablehnen | `Absence` |
| POST | `/absences/approve-all` | D | Alle offenen Anträge genehmigen | `Absence[]` |
| GET | `/absences/{id}/ics` | P, S, D, nach Berechtigungsprüfung | ICS herunterladen | `text/calendar` |
| GET | `/statistics?year=` | P, S, D | Urlaubsstatistik abrufen | `StatisticsResponse` |
| GET | `/statistics/export?year=&type=` | S, D | CSV- oder PDF-Export | `text/csv` oder `application/pdf` |
| PUT | `/vacation-entitlements/{userId}?year=` | S | Urlaubsanspruch ändern | `VacationEntitlement` |
| GET | `/public-holidays?year=` | P, S, D | Feiertage abrufen | `PublicHoliday[]` |

Die Methoden und Routen entsprechen der Endpunkt-Tabelle in Abschnitt 5.6.2; die vollständigen HTTP-Statuscodes sind an jedem Pfad dokumentiert. Die Unterscheidung zwischen „authentifiziert“ und „für Fachfunktionen berechtigt“ ist besonders für `GET /users/me` wichtig.

## Authentifizierung: Keycloak, JWT und Rollen

1. Der Browser meldet Benutzer bei Keycloak (`hse`-Realm) mittels Authorization Code Flow und PKCE an. Keycloak, **nicht** das Backend, verarbeitet das Passwort.
2. Das Frontend erhält ein Access Token und sendet es bei einem API-Aufruf im Header `Authorization: Bearer <access-token>`.
3. Spring Security prüft JWT-Signatur, Issuer und Ablaufzeit. Es liest die Keycloak-Gruppe aus dem Claim `groups`.
4. Die Gruppen `professors`, `staff` und `dekanat` werden auf die Fachrollen `PROFESSOR`, `SECRETARIAT` und `DEAN` abgebildet. Bei mehreren Gruppen gilt die höchste Rolle (D > S > P).
5. Die Autorisierung findet im Backend statt, etwa durch `@PreAuthorize`; zusätzlich muss die Geschäftslogik prüfen, ob Professoren nur **eigene offene** Einträge verändern.
6. `students` oder Benutzer ohne passende Gruppe haben keine Fachrolle. Der Aufruf `GET /users/me` muss für angemeldete Benutzer trotzdem funktionieren, damit das Frontend die „kein Zugriff“-Landing-Page darstellen kann. Alle anderen Fachendpunkte bleiben gesperrt (`403`).

Die YAML definiert die **Anforderung** an den Bearer-Token, übernimmt aber **nicht** die Prüfung oder die Keycloak-Konfiguration. Insbesondere legt ein Eintrag `security: - bearerAuth: []` keine Spring-Security-Filterkette an.

## Die zentralen Datenmodelle

| Schema | Zweck | Wichtige Felder |
|---|---|---|
| `User` | Person aus Keycloak (lokal gespiegelt) | `id` (UUID), `first_name`, `last_name`, `email`, `role` (nullable) |
| `Role` | Effektive Zugriffsrolle | `PROFESSOR`, `SECRETARIAT`, `DEAN` |
| `AbsenceCreateRequest` | Daten zum Anlegen | `start_date`, `end_date`, optional `user_id`, `title` |
| `AbsenceUpdateRequest` | Änderungen an Datum und Titel | `start_date`, `end_date`, `title` |
| `Absence` | Gespeicherter Kalendereintrag | `id`, `user_id`, `created_from`, `updated_from`, Datumsfelder, `status`, `version`, Zeitstempel |
| `AbsenceStatus` | Genehmigungszustand | `OPEN`, `APPROVED`, `REJECTED` |
| `VacationEntitlementRequest` | Neuer Anspruch | `days` (nicht negativ) |
| `VacationEntitlement` | Anspruch eines Jahres | `user_id`, `year`, `days` |
| `StatisticsResponse` | Statistiken | `year`, Liste `users` |
| `VacationStatistics` | Statistik je Person | `entitlement_days`, `used_days`, `remaining_days`, `overplanned`, monatliche Werte |
| `PublicHoliday` | Feiertag | `date`, `name` |
| `Problem` | Einheitliche HTTP-Fehler | `type`, `title`, `status`, optional `detail`, `instance` |

Die Modellnamen sind für Backend-Interfaces und den TypeScript-Client wichtig: Der OpenAPI Generator erzeugt daraus Klassen oder Typen. Die `required`-Listen beschreiben, welche JSON-Felder vorhanden sein müssen. `nullable: true` bedeutet dagegen, dass der Wert ausdrücklich `null` sein darf. Bei `role` ist das absichtlich möglich.

### Beispiel: Abwesenheit anlegen

**Request** `POST /api/absences`:

```json
{
  "start_date": "2026-12-01",
  "end_date": "2026-12-04",
  "title": "Vacation"
}
```

**Beispielantwort** `201 Created` (UUIDs und Zeitstempel nur zur Illustration):

```json
{
  "id": 42,
  "user_id": "5a13e105-0712-4ab0-9dfc-caa64a1e2260",
  "created_from": "5a13e105-0712-4ab0-9dfc-caa64a1e2260",
  "updated_from": null,
  "start_date": "2026-12-01",
  "end_date": "2026-12-04",
  "title": "Vacation",
  "status": "OPEN",
  "version": 0,
  "created_at": "2026-11-20T14:00:00+01:00",
  "updated_at": null
}
```

Das Backend bestimmt `created_from` aus dem JWT und setzt den Status, statt diese Angaben ungeprüft vom Browser zu übernehmen. Wochenenden, Feiertage und Doppelbuchungen werden im Backend geprüft. Eintragungen durch das Sekretariat oder Dekanat **für andere Personen** sind laut User Stories sofort genehmigt.

### Beispiel: Statistik

**Beispielantwort** von `GET /api/statistics?year=2026`:

```json
{
  "year": 2026,
  "users": [
    {
      "user_id": "5a13e105-0712-4ab0-9dfc-caa64a1e2260",
      "entitlement_days": 30,
      "used_days": 8,
      "remaining_days": 22,
      "overplanned": false,
      "days_by_month": [{ "month": 12, "days": 4 }]
    }
  ]
}
```

**Entwurfsannahme:** Die Dokumentation legt fest, *welche Kennzahlen* verfügbar sein müssen, aber nicht deren exakte JSON-Verschachtelung. `StatisticsResponse.users` und `days_by_month` sind eine ergänzte, nachvollziehbare Vertragsform, die das Team mit Frontend und Backend abgleichen sollte.

## 6. Fehler und HTTP-Statuscodes

| Code | Bedeutung |
|---|---|
| `200` | Erfolgreiche Abfrage oder Änderung |
| `201` | Abwesenheit erfolgreich angelegt |
| `204` | Abwesenheit gelöscht; kein Body |
| `400` | Ungültige Anfrage, z. B. Ende vor Beginn, ungültiges Exportformat |
| `401` | JWT fehlt, ist abgelaufen oder ungültig |
| `403` | Token gültig, aber Rolle oder Eigentümerschaft reicht nicht aus |
| `404` | Eintrag oder User nicht gefunden |
| `409` | Überschneidung, ungültiger Statuswechsel oder Versionskonflikt |

Fehlerantworten werden als `application/problem+json` (RFC 9457) modelliert. Beispiel:

```json
{
  "type": "about:blank",
  "title": "Conflict",
  "status": 409,
  "detail": "Absence overlaps an existing entry"
}
```

Der Spring-Boot-Code muss solche Statuscodes und Bodies tatsächlich erzeugen. Die YAML allein löst noch keine `409`-Antwort aus.

## Abgleich mit Dokumentation und offene Entscheidungen

Die folgenden Punkte müssen für eine endgültig verbindliche API **gemeinsam entschieden** werden. Wo die Meilenstein-2-Dokumentation keine vollständigen DTO-Felder oder Verarbeitungsschritte vorgibt, wurde eine brauchbare, aber **nicht bestätigte** Ausprägung gewählt.

1. **Statistik-DTO (Entwurfsannahme):** `StatisticsResponse` ist ein Objekt mit `year` und `users[]`; es enthält pro User die geforderten Werte und optional `days_by_month`. Die genaue Form ist in Meilenstein 2 nicht festgelegt. Außerdem braucht das Frontend für eine namentliche Gesamtstatistik eventuell zusätzliche User-Informationen oder einen separaten Lookup.
2. **Absence-DTO (Entwurfsannahme):** Aus dem physischen Datenmodell wurden `id`, `user_id`, `created_from`, `updated_from`, `start_date`, `end_date`, `title`, `status`, `version`, `created_at`, `updated_at` übernommen. `user_id` im Create-Request ist optional (Self-Service-Default); das ist eine technische Ergänzung. Ob ein Update für Sekretariat und Dekanat auch genehmigte Einträge ändern darf, ist in den User Stories nicht vollständig ausformuliert.
3. **Ablehnung bestätigen – fehlende API-Operation:** US-D3 sieht vor, dass eine abgelehnte Abwesenheit erst nach Bestätigung des Betroffenen gelöscht wird. Die Endpunktliste in Abschnitt 5.6.2 enthält dafür **keinen eigenen Endpunkt**. Dieser wurde deshalb **nicht erfunden**. Vor Implementierung muss entschieden werden, wie die Bestätigung an das Backend geschickt wird. Dies ist eine echte Lücke im aktuellen API-Vertrag.
4. **Keine Rolle / `GET /users/me`:** Die User Stories verlangen eine Landing Page für Benutzer ohne Fachrolle. Deshalb ist `role: null` vorgesehen und `GET /users/me` bewusst auch nach Login ohne Fachrolle erreichbar. Eine unmittelbare `403`-Sperre dieses Endpunkts würde die Rollenerkennung für die Landing Page erschweren.
5. **Urlaubsanspruch:** US-S5 sagt eindeutig, dass **nur Sekretariat** den Anspruch ändert; die allgemeine Übersicht im README nennt teils auch das Dekanat. Hier wurde die spezifische User Story US-S5 priorisiert. `year` wurde als Pflichtparameter für das `PUT` modelliert; die Änderungsberechtigung auf das aktuelle Jahr muss das Backend prüfen.
6. **Jahresauswahl:** US-P10 erlaubt dem Professor keinen Jahreswechsel; US-D5 dem Dekanat schon. Die YAML erlaubt einen optionalen `year`-Parameter für Statistiken und Feiertage, aber die tatsächlichen rollenabhängigen Regeln muss das Backend durchsetzen.
7. **Benennung der JSON-Felder:** Die bestehende Datei benutzte `first_name`, `last_name` (snake_case). Diese Konvention wurde fortgeführt. Java-/TypeScript-Generatoren müssen entsprechend konfiguriert bzw. ihre generierten Properties geprüft werden. Wenn das Team camelCase bevorzugt, sollte der Vertrag **vor** Beginn der Implementierung konsistent geändert werden.
8. **ICS-Format und Versionsprüfung:** Die Dokumentation legt UID aus Abwesenheits-ID und SEQUENCE aus der Version fest. Die YAML beschreibt nur den HTTP-Download als `text/calendar`; die korrekte Generierung, Mails, Deeplinks und CANCEL-Verarbeitung müssen im Backend implementiert und getestet werden.
9. **Datumsparameter / Defaults (Entwurfsannahme):** `from` und `to` wurden für die Kalenderliste als Pflichtparameter modelliert. `year` hat bei GET-Operationen einen Default auf das aktuelle Jahr. Für den Export ist `type` ein Pflichtparameter; nur `csv` und `pdf` sind erlaubt.
10. **Mehrdeutige Beschreibung von POST /absences:** Tabelle 4 nennt `OPEN` als Ausgangsstatus, während US-S4 und Abschnitt 5.3.2 Einträge des Sekretariats/Dekanats für andere unmittelbar als `APPROVED` festlegen. Die Methodenbeschreibung folgt den **ausdrücklichen User Stories** und dem Sequenzablauf. Es gibt bewusst kein Client-Feld `status`, damit niemand die Berechtigungsprüfung umgeht.
11. **Datum und Urlaubstage:** Das Backend zieht Samstage, Sonntage, gesetzliche Feiertage Baden-Württembergs sowie den 24. und 31. Dezember ab. Die API liefert Kalenderdaten, aber die Urlaubstagsberechnung liegt ausdrücklich **nicht** im Client.
12. **Sicherheit / Export / Sichtbarkeit:** Der Fachzugriff ist per Rolle abgesichert. Zusätzliche Regeln wie Eigentümerschaft, Status und sensible Felder müssen in Controller/Services geprüft werden. Auch der ICS-Download sollte den Zugriff auf den konkreten Eintrag prüfen.

## Wie wird daraus Code?

Das Backend-Projekt besitzt bereits den `openapi-generator-maven-plugin` in `pom.xml`. Bei einem Maven-Build kann er aus dieser YAML Spring-Interfaces und DTOs generieren, unter anderem in den Paketnamen:

- `de.hs_esslingen.provacasy_backend.api`
- `de.hs_esslingen.provacasy_backend.dto`

Die konkrete Generierung hängt von den bereits vorhandenen Plugin-Optionen ab (u. a. `interfaceOnly`, `useSpringBoot4`, `useBeanValidation`, `useTags`, `openApiNullable`)


### Beispiel: Aufruf in einem HTTP-Client

```http
GET /api/absences?from=2026-12-01&to=2026-12-31&status=OPEN HTTP/1.1
Host: localhost:8081
Authorization: Bearer <access-token>
Accept: application/json
```

`localhost:8081` ist der lokale Port des Backends (`server.port` in `application.properties`). Ein gültiger Keycloak-Token ist erforderlich.

## Tests und Done-Kriterien

Vor dem Merge sollten mindestens folgende Fälle getestet werden:

- Ein gültiges Professor-Token erlaubt `GET /users/me` und die eigenen Einträge, aber keine Verwaltung fremder Einträge.
- Ein gültiger Student-Token erlaubt `GET /users/me` mit `role: null`, aber keine Fachfunktion.
- Ungültige/abgelaufene Tokens liefern `401`, fehlende Berechtigungen `403`.
- `POST /absences` mit gültigen Daten erzeugt `201` und statusabhängig `OPEN` oder `APPROVED`.
- Überlappung erzeugt `409`, falsch sortierte Datumsangaben `400`.
- Nur Dekanat kann genehmigen/ablehnen; schon entschiedene Anträge dürfen nicht erneut genehmigt werden.
- Nur Sekretariat kann den Anspruch ändern.
- `GET /statistics` zählt offene und genehmigte, nicht abgelehnte Einträge.
- `GET /statistics/export?type=csv|pdf` liefert den korrekten MIME-Typ; andere Formate `400`.
- `GET /public-holidays` berücksichtigt Baden-Württemberg und den 24./31. Dezember.
- ICS hat stabile UID und korrekte SEQUENCE bei Änderungen; Storno nutzt CANCEL.
- `./mvnw clean verify` läuft im Team-Setup, generierte Interfaces/DTOs und Tests bestehen.

**Status dieser Lieferung:** YAML-Parsing und lokale strukturelle Konsistenzprüfungen sind durchgeführt; ein vollständiger Maven-Build, echte Integrationstests und Keycloak-/PostgreSQL-Lauftests wurden hier **nicht** durchgeführt. Die Ergebnisse dürfen nicht als implementierte oder bereits getestete REST-Endpunkte verstanden werden.
