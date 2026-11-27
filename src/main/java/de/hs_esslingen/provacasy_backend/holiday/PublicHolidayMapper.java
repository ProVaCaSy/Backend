package de.hs_esslingen.provacasy_backend.holiday;

import java.util.List;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;
import org.mapstruct.ReportingPolicy;

/**
 * Wandelt die Entity PublicHoliday in das DTO für GET /api/public-holidays um.
 * Das DTO heißt genauso wie die Entity, deshalb steht es mit vollem Paketnamen da.
 */
// Grundprinzip von MapStruct siehe AbsenceMapper. Der einfachste Mapper im Projekt:
// name heißt auf beiden Seiten gleich und wird automatisch übernommen,
// nur das Datum heißt in der Entity holidayDate und im DTO date.
// Nur eine Richtung: Feiertage gehen nur raus, es kommt nie ein Feiertag über die API rein.
@Mapper(componentModel = MappingConstants.ComponentModel.SPRING,
        unmappedTargetPolicy = ReportingPolicy.ERROR)
public interface PublicHolidayMapper {

    // target = Feld im Ergebnis (DTO), source = Feld in der Eingabe (Entity).
    // MapStruct erzeugt daraus einen Aufruf von setDate mit dem Wert von getHolidayDate.
    @Mapping(target = "date", source = "holidayDate")
    de.hs_esslingen.provacasy_backend.dto.PublicHoliday toDto(PublicHoliday holiday);

    // Für GET /api/public-holidays?year=..., das immer eine Liste liefert.
    List<de.hs_esslingen.provacasy_backend.dto.PublicHoliday> toDtoList(List<PublicHoliday> holidays);
}
