# Architektur des Backends

Dieses Dokument erklärt, wie der Quellcode des ProVaCaSy-Backends aufgebaut ist, warum er so aufgebaut ist und wo neue Klassen hingehören. Es richtet sich an alle Teammitglieder und ergänzt die Beschreibung in der LaTeX-Dokumentation (Meilenstein 2, Abschnitt 5.2).

Die Beschreibung der einzelnen Pakete steht zusätzlich als Javadoc in der Datei `package-info.java` des jeweiligen Pakets. Dort ist sie beim Programmieren direkt sichtbar.

## Warum Schichten?

Das Backend folgt einer Schichtenarchitektur (Controller, Service, Repository). Wir haben uns aus folgenden Gründen dafür entschieden:

- **Anforderung der Lehrveranstaltung:** Die Bewertung verlangt eine saubere Schichtentrennung mit Controllern, DTOs und DAOs. Die Pakete machen diese Trennung im Code sichtbar.
- **Verständlichkeit:** Jede Klasse hat genau eine Aufgabe. Wer eine Änderung machen will, weiß, wo er suchen muss.
- **Testbarkeit:** Die Fachlogik im Service lässt sich ohne Webserver und ohne Datenbank testen. Controller und Repositories werden getrennt davon getestet.
- **Paralleles Arbeiten:** Im Team arbeiten mehrere Personen gleichzeitig. Wenn jede Schicht klar abgegrenzt ist, geraten wir seltener in Merge-Konflikte.
- **Austauschbarkeit:** Die API (DTOs) ist vom Datenmodell (Entitäten) getrennt. Das Datenmodell darf sich ändern, ohne dass das Frontend brechen muss.

Wir gliedern den Code nach **Schichten** und nicht nach Fachbereichen (also `controller/`, `service/` statt `absence/`, `user/`). Für ein Projekt dieser Größe mit wenigen Entitäten ist das übersichtlicher und entspricht der Vorgabe. Würde das Backend deutlich wachsen, könnte man später pro Fachbereich ein Unterpaket einführen.

## Paketübersicht

Basispaket: `de.hs_esslingen.provacasy_backend`

```
src/main/java/de/hs_esslingen/provacasy_backend/
├── controller/    REST-Endpunkte (Web-Schicht), implementieren die generierten API-Interfaces
├── service/       Fachlogik (Kontingent, Doppelbuchung, Genehmigung, Statistik, ICS-Versand)
├── repository/    Datenzugriff mit Spring Data JPA (ersetzt klassische DAOs)
├── model/         JPA-Entitäten und Enums (Datenmodell)
├── dto/           Request- und Response-Objekte der API (aus OpenAPI generiert)
├── mapper/        MapStruct: Umwandlung Entität <-> DTO
├── exception/     Eigene Exceptions und zentrale Fehlerbehandlung
├── security/      Keycloak-Anbindung, Rollen, Zugriffsregeln
└── config/        Technische Konfiguration (CORS, Cache, Mail, Feiertags-Client)
```

| Paket | Aufgabe | Beispiele | Darf verwenden |
| --- | --- | --- | --- |
| `controller` | HTTP entgegennehmen, Eingaben validieren, Rolle prüfen, Service aufrufen, DTO zurückgeben | `AbsenceController`, `UserController`, `StatisticsController`, `PublicHolidayController` | `service`, `dto` |
| `service` | Fachliche Regeln, Transaktionen | `AbsenceService`, `UserService`, `StatisticsService`, `ExportService`, `PublicHolidayService`, `WorkingDayCalculator`, `MailService`, `IcsService` | `repository`, `model`, `mapper`, `dto`, `exception` |
| `repository` | Datenbankzugriff | `AbsenceRepository`, `UserRepository`, `VacationEntitlementRepository`, `PublicHolidayRepository` | `model` |
| `model` | Abbild der Tabellen | `User`, `Absence`, `VacationEntitlement`, `PublicHoliday`, `AbsenceStatus` | nur sich selbst |
| `dto` | Vertrag der API (generiert) | `AbsenceCreateRequest`, `AbsenceResponse`, `StatisticsResponse` | nichts |
| `mapper` | Felder zwischen Entität und DTO kopieren | `AbsenceMapper`, `UserMapper` | `model`, `dto` |
| `exception` | Fehler und einheitliche Fehlerantworten | `AbsenceOverlapException`, `ResourceNotFoundException`, `GlobalExceptionHandler` | nichts |
| `security` | Token prüfen, Gruppen in Rollen übersetzen | `SecurityConfig`, `KeycloakJwtAuthenticationConverter` | `config` falls nötig |
| `config` | `@Configuration`-Klassen | `CorsConfig`, `CacheConfig`, `MailConfig` | nichts Fachliches |

Die Klassennamen in den Beispielen folgen dem Entwurf aus der Dokumentation und können sich während der Umsetzung noch ändern.

## API-First und generierter Code

Die Schnittstelle wird zuerst in der OpenAPI-Datei `src/main/resources/openapi/provacasy-api.yaml` beschrieben (Richtlinien: Zalando RESTful API Guidelines, Pfade und Bezeichner auf Englisch). Beim Maven-Build werden daraus die API-Interfaces und die DTOs generiert. Die Controller implementieren diese Interfaces. Änderungen an der API erfolgen immer zuerst in der Spezifikation, generierter Code wird nicht von Hand bearbeitet und nicht eingecheckt.

## Abhängigkeitsregeln

Abhängigkeiten zeigen nur in eine Richtung:

```
controller  ->  service  ->  repository  ->  model
    |             |
    v             v
   dto  <-----  mapper
```

1. Ein **Controller** ruft nur Services auf. Er greift nie auf ein Repository oder eine Entität zu.
2. **Entitäten** verlassen die Service-Schicht nicht. Nach außen gehen nur DTOs.
3. **Fachlogik** steht nur im Service. Controller und Mapper enthalten keine Regeln.
4. **Repositories** enthalten nur Abfragen.
5. **Transaktionen** (`@Transactional`) werden im Service gesetzt.
6. **Fehler** werden im Service als Exceptions geworfen. Der `GlobalExceptionHandler` macht daraus einheitliche HTTP-Antworten.

## Rollen und Benutzer

- Keycloak verwaltet Benutzer, Gruppen und Rollen. Das Backend erhält das Token vom Frontend und prüft es mit dem öffentlichen Schlüssel von Keycloak (JWKS).
- Die Gruppen `professors`, `staff` und `dekanat` aus dem Claim `groups` werden auf die Rollen Professor, Sekretariat und Dekanat abgebildet. Bei mehreren Gruppen gilt die höchste Rolle. Benutzer ohne passende Gruppe erhalten keine Rolle, das Backend lehnt ihre Anfragen mit 403 ab.
- Endpunkte werden mit `@PreAuthorize` nach Rolle geschützt. Einschränkungen wie "Professoren nur eigene Einträge" prüft der Service.
- Die Tabelle `app_user` ist nur eine Kopie von Name und E-Mail. Der Eintrag entsteht beim ersten Login, die Id ist die User-ID aus Keycloak.

## Beispiel: Abwesenheit anlegen

Ablauf von `POST /api/absences` (siehe Sequenzdiagramm in der Dokumentation, Abschnitt 5.3.2):

1. Der Client sendet einen `AbsenceCreateRequest` (DTO).
2. Spring prüft den Token (`security`) und der Controller prüft die Rolle (`@PreAuthorize`) und die Eingabe (`@Valid`).
3. `AbsenceController` ruft `AbsenceService.create(...)` auf.
4. `AbsenceService` prüft, dass ein Professor nur für sich einträgt, und fragt über `AbsenceRepository` nach überlappenden Einträgen (abgelehnte zählen nicht).
5. Bei einer Doppelbuchung wirft der Service eine `AbsenceOverlapException`. Der `GlobalExceptionHandler` antwortet mit HTTP 409. Dasselbe gilt, wenn der Constraint der Datenbank bei parallelen Anfragen auslöst.
6. Sonst zählt der `WorkingDayCalculator` die Urlaubstage ohne Wochenenden und Feiertage (Feiertage aus dem `PublicHolidayService`). Eine Überplanung blockiert nicht, sie wird nur in der Statistik markiert.
7. Der Service speichert die Entität `Absence` (Professor für sich selbst: Status `OPEN`, Sekretariat oder Dekanat für andere: `APPROVED`), wandelt sie mit dem `AbsenceMapper` in eine `AbsenceResponse` um und löst die Mail mit ICS-Datei und Deeplink aus.
8. Der Controller liefert die `AbsenceResponse` mit HTTP 201 zurück.

## Wohin gehört eine neue Klasse?

| Wenn die Klasse ... | dann in ... |
| --- | --- |
| einen Endpunkt anbietet | `controller` |
| eine Regel oder Berechnung enthält | `service` |
| nur Daten aus der Datenbank holt oder speichert | `repository` |
| eine Tabelle abbildet oder ein festes Wertepaar (Enum) ist | `model` |
| Daten der API beschreibt | OpenAPI-Spezifikation (wird zu `dto` generiert) |
| nur Felder kopiert | `mapper` |
| einen Fehlerfall beschreibt | `exception` |
| mit Anmeldung oder Rollen zu tun hat | `security` |
| nur Technik einrichtet (Bean, CORS, Cache) | `config` |

Wir legen bewusst kein Paket `util` an. Hilfsklassen landen sonst schnell als Sammelbecken. Gehört eine Hilfsklasse zu einer Fachregel (z. B. der `WorkingDayCalculator`), liegt sie im `service`.

## Namenskonventionen

- Controller: `<Fachbegriff>Controller`, Service: `<Fachbegriff>Service`, Repository: `<Entität>Repository`, Mapper: `<Entität>Mapper`.
- Entitäten heißen im Singular (`Absence`, nicht `Absences`).
- DTOs enden auf `Request` oder `Response`.
- Bezeichner im Code sind Englisch. Kommentare und Dokumentation sind Deutsch.

## Tests

Die Tests liegen unter `src/test/java` in denselben Paketen wie der Code, den sie prüfen:

| Schicht | Testart |
| --- | --- |
| `service` | Unit-Tests ohne Spring (Mockito für Repositories) |
| `controller` | `@WebMvcTest` mit Mock-Service und Security-Test |
| `repository` | `@DataJpaTest` gegen PostgreSQL |
| gesamter Ablauf | Integrationstests mit `@SpringBootTest` gegen PostgreSQL |

Das Datenbankschema nutzt PostgreSQL-Funktionen (Enum-Typ, `btree_gist`, Ausschluss-Constraint gegen Überschneidungen). Eine H2-Datenbank kann die Migration nicht ausführen, Repository- und Integrationstests laufen deshalb gegen eine echte PostgreSQL-Instanz (z. B. als Container).

Die Datenmodell-Klassen (`model`, `config`) und der generierte Code (`dto`, API-Interfaces) sind in der JaCoCo-Auswertung ausgenommen, weil sie kaum Logik enthalten.

## Wie das Dokument gepflegt wird

Wer ein neues Paket anlegt oder die Regeln ändert, aktualisiert dieses Dokument, die `package-info.java` des Pakets und den Abschnitt in der LaTeX-Dokumentation im selben Pull Request.
