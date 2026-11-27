package de.hs_esslingen.provacasy_backend.entitlement;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;
import org.mapstruct.ReportingPolicy;

/**
 * Wandelt die Entity VacationEntitlement in das DTO für PUT /api/vacation-entitlements/{userId} um.
 * userId kommt aus dem Schlüssel und nicht aus user, damit der User nicht extra geladen wird.
 */
// Grundprinzip von MapStruct siehe AbsenceMapper.
// Was hier automatisch passiert:
// • days: Short (Entity) zu Integer (DTO) wandelt MapStruct selbst um, da geht nichts verloren.
// • year: Die Entity hat die Methode getYear(). MapStruct schaut auf Getter, nicht auf Felder,
//   und findet so year, obwohl es eigentlich im Schlüssel id steckt.
// Nur userId braucht eine Angabe, weil es dafür keinen passenden Getter gibt.
@Mapper(componentModel = MappingConstants.ComponentModel.SPRING,
        unmappedTargetPolicy = ReportingPolicy.ERROR)
public interface VacationEntitlementMapper {

    // "id.userId" heißt: entitlement.getId().getUserId().
    @Mapping(target = "userId", source = "id.userId")
    de.hs_esslingen.provacasy_backend.dto.VacationEntitlement toDto(VacationEntitlement entitlement);
}
