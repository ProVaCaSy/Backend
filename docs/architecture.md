# Architektur des Backends

Dieses Dokument erklärt, wie der Quellcode des ProVaCaSy-Backends aufgebaut ist, warum er so aufgebaut ist und wo neue Klassen hingehören. Es richtet sich an alle Teammitglieder und ergänzt die Beschreibung in der LaTeX-Dokumentation (Meilenstein 2, Abschnitt 5.2).

Die Beschreibung der einzelnen Pakete steht zusätzlich als Javadoc in der Datei `package-info.java` des jeweiligen Pakets. Dort ist sie beim Programmieren direkt sichtbar.

## Schichten und Pakete nach Fachlichkeit

Das Backend folgt einer Schichtenarchitektur (Controller, Service, Repository). Wir haben uns aus folgenden Gründen dafür entschieden:

- **Anforderung der Lehrveranstaltung:** Die Bewertung verlangt eine saubere Schichtentrennung mit Controllern, DTOs und DAOs. Die Klassen machen diese Trennung im Code sichtbar.
- **Verständlichkeit:** Jede Klasse hat genau eine Aufgabe. Wer eine Änderung machen will, weiß, wo er suchen muss.
- **Testbarkeit:** Die Fachlogik im Service lässt sich ohne Webserver und ohne Datenbank testen. Controller und Repositories werden getrennt davon getestet.
- **Paralleles Arbeiten:** Im Team arbeiten mehrere Personen gleichzeitig. Wenn jede Schicht klar abgegrenzt ist, geraten wir seltener in Merge-Konflikte.
- **Austauschbarkeit:** Die API (DTOs) ist vom Datenmodell (Entitäten) getrennt. Das Datenmodell darf sich ändern, ohne dass das Frontend brechen muss.

Die Schichten trennen wir innerhalb der Klassen, die Pakete gliedern wir aber nach **Fachlichkeit** (package by feature, seit 26.11.2026): Alles zu einem Thema liegt zusammen, also `absence/` mit `Absence`, `AbsenceRepository`, `AbsenceMapper`, `AbsenceService` und `AbsenceController`. Die Schicht erkennt man am Klassennamen. Vorteil: Eine fachliche Änderung betrifft meist nur ein Paket, und man muss nicht in fünf Ordnern suchen. Querschnittsthemen, die alle Fachbereiche betreffen (`security`, `config`, `exception`), bleiben eigene Pakete.

## Paketübersicht

Basispaket: `de.hs_esslingen.provacasy_backend`

```
src/main/java/de/hs_esslingen/provacasy_backend/
├── absence/       Abwesenheiten: Entität, Status, Repository, Mapper, später Service und Controller
├── entitlement/   Urlaubsanspruch pro Person und Jahr (mit zusammengesetztem Schlüssel)
├── holiday/       Feiertage Baden-Württemberg, später Feiertags-Client und WorkingDayCalculator
├── user/          Benutzer aus Keycloak, GET /api/users/me
├── security/      Keycloak-Anbindung, Rollen, CORS
├── config/        Technische Konfiguration (Cache, Mail, Feiertags-Client)
├── exception/     Eigene Exceptions und zentrale Fehlerbehandlung
└── api/, dto/     aus der OpenAPI-Spezifikation generiert (nicht eingecheckt)
```

**Fachpakete** (Stand 27.11.2026):

| Paket | Vorhanden | Geplant |
| --- | --- | --- |
| `absence` | `Absence`, `AbsenceStatus`, `AbsenceRepository`, `AbsenceMapper` | `AbsenceService`, `AbsenceController`, ICS-Datei und Mail |
| `entitlement` | `VacationEntitlement`, `VacationEntitlementId`, `VacationEntitlementRepository`, `VacationEntitlementMapper` | Service und Controller für `PUT /api/vacation-entitlements/{userId}` |
| `holiday` | `PublicHoliday`, `PublicHolidayRepository`, `PublicHolidayMapper` | `PublicHolidayService` (Feiertags-API, Cache, 24.12. und 31.12.), `WorkingDayCalculator`, `PublicHolidayController` |
| `user` | `User`, `UserRepository`, `UserMapper`, `UserService`, `UserController` | Benutzersuche `GET /api/users?search=` |
| Statistik und Export | noch nicht angelegt | eigenes Fachpaket, zum Beispiel `statistics` |

**Querschnittspakete:**

| Paket | Aufgabe | Beispiele |
| --- | --- | --- |
| `security` | Token prüfen, Gruppen in Rollen übersetzen, CORS | `SecurityConfig`, `KeycloakJwtAuthenticationConverter` |
| `config` | `@Configuration`-Klassen ohne Fachlogik | `CacheConfig`, `MailConfig` (geplant) |
| `exception` | Fehler und einheitliche Fehlerantworten | `AbsenceOverlapException`, `ResourceNotFoundException`, `GlobalExceptionHandler` (geplant) |
| `api`, `dto` | Vertrag der API, generiert | `UsersApi`, `AbsenceCreateRequest`, `Absence` |

Die geplanten Klassennamen folgen dem Entwurf aus der Dokumentation und können sich während der Umsetzung noch ändern.

## API-First und generierter Code

Die Schnittstelle wird zuerst in der OpenAPI-Datei `src/main/resources/openapi/provacasy-api.yaml` beschrieben (Richtlinien: Zalando RESTful API Guidelines, Pfade und Bezeichner auf Englisch). Beim Maven-Build werden daraus die API-Interfaces und die DTOs generiert. Die Controller implementieren diese Interfaces. Änderungen an der API erfolgen immer zuerst in der Spezifikation, generierter Code wird nicht von Hand bearbeitet und nicht eingecheckt.

## Abhängigkeitsregeln

Abhängigkeiten zeigen nur in eine Richtung. `Xyz` steht für das Thema des Fachpakets, zum Beispiel `Absence`:

```
XyzController  ->  XyzService  ->  XyzRepository  ->  Entität
      |                |
      v                v
     dto  <-------  XyzMapper
```

1. Ein **Controller** ruft nur Services auf. Er greift nie auf ein Repository oder eine Entität zu.
2. **Entitäten** verlassen die Service-Schicht nicht. Nach außen gehen nur DTOs.
3. **Fachlogik** steht nur im Service. Controller und Mapper enthalten keine Regeln.
4. **Repositories** enthalten nur Abfragen.
5. **Transaktionen** (`@Transactional`) werden im Service gesetzt.
6. **Fehler** werden im Service als Exceptions geworfen. Der `GlobalExceptionHandler` macht daraus einheitliche HTTP-Antworten.
7. Ein **Fachpaket nutzt ein anderes** nur über dessen Service, nicht über dessen Repository. Ausnahme: Entitäten dürfen sich gegenseitig referenzieren (zum Beispiel `Absence` auf `User`).

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
4. `AbsenceService` prüft, dass ein Professor nur für sich einträgt, und fragt über `AbsenceRepository.existsOverlap(...)` nach überlappenden Einträgen (abgelehnte zählen nicht).
5. Bei einer Doppelbuchung wirft der Service eine `AbsenceOverlapException`. Der `GlobalExceptionHandler` antwortet mit HTTP 409. Dasselbe gilt, wenn der Constraint der Datenbank bei parallelen Anfragen auslöst (Spring meldet dann eine `DataIntegrityViolationException`).
6. Sonst zählt der `WorkingDayCalculator` die Urlaubstage ohne Wochenenden und Feiertage (Feiertage aus dem `PublicHolidayService`). Eine Überplanung blockiert nicht, sie wird nur in der Statistik markiert.
7. Der Service speichert die Entität `Absence` (Professor für sich selbst: Status `OPEN`, Sekretariat oder Dekanat für andere: `APPROVED`), wandelt sie mit dem `AbsenceMapper` in das DTO `Absence` um und löst die Mail mit ICS-Datei und Deeplink aus.
8. Der Controller liefert das DTO mit HTTP 201 zurück.

Entität und DTO heißen beide `Absence` (siehe Namenskonventionen). Im Code steht deshalb eine der beiden mit vollem Paketnamen, zum Beispiel `de.hs_esslingen.provacasy_backend.dto.Absence`.

## Wohin gehört eine neue Klasse?

Zwei Fragen:

1. **Zu welchem Thema gehört sie?** Das bestimmt das Paket: `absence`, `entitlement`, `holiday`, `user` oder ein neues Fachpaket. Betrifft sie alle Themen, kommt sie in ein Querschnittspaket.
2. **Welche Rolle hat sie?** Das bestimmt die Endung des Namens.

| Wenn die Klasse ... | heißt sie ... | und liegt in ... |
| --- | --- | --- |
| einen Endpunkt anbietet | `XyzController` | Fachpaket |
| eine Regel oder Berechnung enthält | `XyzService` oder ein sprechender Name | Fachpaket |
| nur Daten aus der Datenbank holt oder speichert | `XyzRepository` | Fachpaket |
| eine Tabelle abbildet oder ein festes Wertepaar (Enum) ist | Name der Entität | Fachpaket |
| nur Felder zwischen Entität und DTO kopiert | `XyzMapper` | Fachpaket |
| Daten der API beschreibt | (OpenAPI-Spezifikation, wird zu `dto` generiert) | `dto` |
| einen Fehlerfall beschreibt | `...Exception` | `exception` |
| mit Anmeldung oder Rollen zu tun hat | | `security` |
| nur Technik einrichtet (Bean, Cache, Mail) | `...Config` | `config` |

Wir legen bewusst kein Paket `util` an. Hilfsklassen landen sonst schnell als Sammelbecken. Gehört eine Hilfsklasse zu einer Fachregel, liegt sie im passenden Fachpaket, zum Beispiel der `WorkingDayCalculator` in `holiday`.

## Namenskonventionen

- Controller: `<Fachbegriff>Controller`, Service: `<Fachbegriff>Service`, Repository: `<Entität>Repository`, Mapper: `<Entität>Mapper`.
- Entitäten heißen im Singular (`Absence`, nicht `Absences`).
- Request-DTOs enden auf `Request` (`AbsenceCreateRequest`). Antworten heißen wie die Ressource (`Absence`, `User`) und damit genauso wie die Entität. Im Code steht deshalb eine der beiden mit vollem Paketnamen.
- Bezeichner im Code sind Englisch. Kommentare und Dokumentation sind Deutsch.

## Tests

Die Tests liegen unter `src/test/java` in denselben Paketen wie der Code, den sie prüfen (zum Beispiel `absence/AbsenceRepositoryTest`).

| Testart | Werkzeug | Beispiele |
| --- | --- | --- |
| Mapper | Unit-Test ohne Spring, `Mappers.getMapper(...)` | `AbsenceMapperTest`, `UserMapperTest` |
| Service | Unit-Test ohne Spring, Mockito für Repositories | `UserServiceTest` |
| Repository | `@DataJpaTest` gegen PostgreSQL aus Testcontainers | `AbsenceRepositoryTest`, `VacationEntitlementRepositoryTest`, `PublicHolidayRepositoryTest` |
| Security | Rollen-Mapping und geschützte Endpunkte | `KeycloakJwtAuthenticationConverterTest`, `SecurityConfigTest` |
| Controller | `@WebMvcTest` mit Mock-Service und Security-Test | (geplant) |
| gesamter Ablauf | `@SpringBootTest` gegen PostgreSQL | `ProvacasyBackendApplicationTests` |

Das Datenbankschema nutzt PostgreSQL-Funktionen (Enum-Typ, `btree_gist`, Ausschluss-Constraint gegen Überschneidungen). Eine H2-Datenbank kann die Migration nicht ausführen. Die Repository-Tests starten deshalb mit Testcontainers einen eigenen `postgres:17` Container, Flyway spielt das echte Schema ein. Sie brauchen nur Docker, nicht die Dev-Datenbank, und laufen genauso in der CI. Der `AbsenceRepositoryTest` hat so einen Fehler gefunden: Eine Enum-Konstante direkt in JPQL schreibt Hibernate mit dem Java-Klassennamen als PostgreSQL-Typ (`absencestatus` statt `absence_status`). Deshalb wird der Status in den Abfragen als Parameter übergeben.

`ProvacasyBackendApplicationTests` (`contextLoads`) nutzt die Datenbank aus `application.properties`, lokal also die Dev-Datenbank, in der CI den Postgres-Service.

Der generierte Code (`api`, `dto`) ist in der JaCoCo-Auswertung ausgenommen, weil er nicht von uns stammt.

## Wie das Dokument gepflegt wird

Wer ein neues Paket anlegt oder die Regeln ändert, aktualisiert dieses Dokument, die `package-info.java` des Pakets und den Abschnitt in der LaTeX-Dokumentation im selben Pull Request.
