package de.hs_esslingen.provacasy_backend.absence;

import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;
import org.mapstruct.ReportingPolicy;

/**
 * Wandelt die Entity Absence in das DTO für GET, POST und PUT /api/absences um.
 * Statt der ganzen User Objekte gehen nur deren IDs raus.
 */
// So funktioniert MapStruct: Wir schreiben nur dieses Interface. Beim Kompilieren erzeugt
// MapStruct daraus die Klasse AbsenceMapperImpl mit ganz normalem Java Code (new, get, set).
// Zu finden unter target/generated-sources/annotations/.../absence/AbsenceMapperImpl.java.
// Felder mit gleichem Namen auf beiden Seiten (startDate, endDate, title, status, ...)
// ordnet MapStruct selbst zu. Nur Abweichungen stehen unten als @Mapping.
//
// componentModel = SPRING: Die erzeugte Klasse bekommt @Component. Der AbsenceService kann
// sich den Mapper dann per Konstruktor geben lassen, wie ein Repository.
// unmappedTargetPolicy = ERROR: Bleibt ein Feld im DTO leer, bricht der Build ab, statt nur
// zu warnen. Kommt später ein Feld in die OpenAPI dazu, fällt es so sofort auf.
//
// Nur die Richtung Entity -> DTO: Aus einem AbsenceCreateRequest baut der Service die Entity
// selbst, weil er dafür erst die User laden und den Status festlegen muss (Fachlogik).
@Mapper(componentModel = MappingConstants.ComponentModel.SPRING,
        unmappedTargetPolicy = ReportingPolicy.ERROR)
public interface AbsenceMapper {

    // source = "user.id" heißt: absence.getUser().getId(). Der Punkt geht eine Ebene tiefer.
    // Bei updatedFrom (kann null sein) baut MapStruct selbst eine Null Prüfung ein.
    // Der volle Paketname beim DTO ist nötig, weil Entity und DTO beide "Absence" heißen.
    // Der kurze Name meint hier die Entity, weil sie im selben Paket liegt.
    @Mapping(target = "userId", source = "user.id")
    @Mapping(target = "createdFrom", source = "createdFrom.id")
    @Mapping(target = "updatedFrom", source = "updatedFrom.id")
    de.hs_esslingen.provacasy_backend.dto.Absence toDto(Absence absence);

    // Keine Angaben nötig: MapStruct erkennt, dass es für jedes Element toDto aufrufen kann.
    List<de.hs_esslingen.provacasy_backend.dto.Absence> toDtoList(List<Absence> absences);

    // Instant (Entity) zu OffsetDateTime (DTO) kann MapStruct nicht von allein, weil dafür
    // eine Zeitzone gewählt werden muss. MapStruct sucht im Interface nach einer Methode mit
    // passenden Typen (Instant rein, OffsetDateTime raus) und benutzt sie für createdAt und
    // updatedAt. "default" heißt: Die Methode hat einen Rumpf, obwohl sie im Interface steht.
    /** Zeitstempel werden in UTC ausgegeben, das Frontend rechnet sie in die lokale Zeit um. */
    default OffsetDateTime toOffsetDateTime(Instant instant) {
        return instant == null ? null : instant.atOffset(ZoneOffset.UTC);
    }
}
