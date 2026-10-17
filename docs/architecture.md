# Architektur des Backends

Dieses Dokument erklärt, wie der Quellcode des ProVaCaSy-Backends aufgebaut ist, warum er so aufgebaut ist und wo neue Klassen hingehören. Es richtet sich an alle Teammitglieder und ergänzt die Beschreibung in der LaTeX-Dokumentation.

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

Basispaket: `de.hsesslingen.provacasy`

```
src/main/java/de/hsesslingen/provacasy/
├── controller/    REST-Endpunkte (Web-Schicht)
├── service/       Fachlogik (Kontingent, Doppelbuchung, Genehmigung, ICS-Versand)
├── repository/    Datenzugriff mit Spring Data JPA (ersetzt klassische DAOs)
├── model/         JPA-Entitäten und Enums (Datenmodell)
├── dto/           Request- und Response-Objekte der API
├── mapper/        MapStruct: Umwandlung Entität <-> DTO
├── exception/     Eigene Exceptions und zentrale Fehlerbehandlung
├── security/      Keycloak-Anbindung, Rollen, Zugriffsregeln
└── config/        Technische Konfiguration (OpenAPI, CORS, Mail)
```

| Paket | Aufgabe | Beispiele | Darf verwenden |
| --- | --- | --- | --- |
| `controller` | HTTP entgegennehmen, Eingaben validieren, Service aufrufen, DTO zurückgeben | `AbsenceController`, `DashboardController` | `service`, `dto` |
| `service` | Fachliche Regeln, Transaktionen | `AbsenceService`, `VacationBalanceService`, `WorkingDayCalculator`, `IcsService` | `repository`, `model`, `mapper`, `dto`, `exception` |
| `repository` | Datenbankzugriff | `AbsenceRepository`, `UserRepository` | `model` |
| `model` | Abbild der Tabellen | `User`, `Absence`, `AbsenceType` | nur sich selbst |
| `dto` | Vertrag der API | `AbsenceCreateRequest`, `AbsenceResponse` | nichts |
| `mapper` | Felder zwischen Entität und DTO kopieren | `AbsenceMapper` | `model`, `dto` |
| `exception` | Fehler und einheitliche Fehlerantworten | `AbsenceOverlapException`, `GlobalExceptionHandler` | nichts |
| `security` | Token prüfen, Rollen übersetzen | `SecurityConfig`, `KeycloakJwtAuthenticationConverter` | `config` falls nötig |
| `config` | `@Configuration`-Klassen | `OpenApiConfig`, `CorsConfig` | nichts Fachliches |

Die Klassennamen in den Beispielen sind Vorschläge und entstehen erst mit der Umsetzung.

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

## Beispiel: Urlaub beantragen

Ablauf von `POST /api/absences`:

1. Der Client sendet einen `AbsenceCreateRequest` (DTO).
2. Spring prüft den Token (`security`) und der Controller prüft die Rolle (`@PreAuthorize`) und die Eingabe (`@Valid`).
3. `AbsenceController` ruft `AbsenceService.create(...)` auf.
4. `AbsenceService` zählt die Arbeitstage (`WorkingDayCalculator`), fragt über `AbsenceRepository` nach überlappenden Einträgen und prüft das Kontingent.
5. Bei einer Doppelbuchung wirft der Service eine `AbsenceOverlapException`. Der `GlobalExceptionHandler` antwortet mit HTTP 409.
6. Sonst speichert der Service die Entität `Absence`, wandelt sie mit dem `AbsenceMapper` in eine `AbsenceResponse` um und löst die E-Mail mit der ICS-Datei aus.
7. Der Controller liefert die `AbsenceResponse` mit HTTP 201 zurück.

## Wohin gehört eine neue Klasse?

| Wenn die Klasse ... | dann in ... |
| --- | --- |
| einen Endpunkt anbietet | `controller` |
| eine Regel oder Berechnung enthält | `service` |
| nur Daten aus der Datenbank holt oder speichert | `repository` |
| eine Tabelle abbildet oder ein festes Wertepaar (Enum) ist | `model` |
| Daten der API beschreibt | `dto` |
| nur Felder kopiert | `mapper` |
| einen Fehlerfall beschreibt | `exception` |
| mit Anmeldung oder Rollen zu tun hat | `security` |
| nur Technik einrichtet (Bean, CORS, Swagger) | `config` |

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
| `repository` | `@DataJpaTest` |
| gesamter Ablauf | Integrationstests mit `@SpringBootTest` |

Die Datenmodell-Klassen (`model`, `dto`, `config`) sind in der JaCoCo-Auswertung ausgenommen, weil sie kaum Logik enthalten.

## Wie das Dokument gepflegt wird

Wer ein neues Paket anlegt oder die Regeln ändert, aktualisiert dieses Dokument, die `package-info.java` des Pakets und den Abschnitt in der LaTeX-Dokumentation im selben Pull Request.
