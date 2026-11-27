package de.hs_esslingen.provacasy_backend.user;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;
import org.mapstruct.ReportingPolicy;

import de.hs_esslingen.provacasy_backend.dto.Role;

/**
 * Wandelt die Entity User in das DTO für GET /api/users/me um.
 * Die Rolle steht nicht in der Datenbank, sie kommt aus dem Token und wird deshalb mitgegeben.
 */
// Grundprinzip von MapStruct siehe AbsenceMapper.
// id, firstName, lastName und email heißen auf beiden Seiten gleich und gehen automatisch.
@Mapper(componentModel = MappingConstants.ComponentModel.SPRING,
        unmappedTargetPolicy = ReportingPolicy.ERROR)
public interface UserMapper {

    // Zwei Parameter: MapStruct kann aus mehreren Quellen gleichzeitig mappen.
    // source = "role" meint hier den Parameter role, nicht ein Feld im User (den gibt es nicht).
    // Ist role null (Studierende, keine passende Gruppe), bleibt die Rolle im DTO leer.
    @Mapping(target = "role", source = "role")
    de.hs_esslingen.provacasy_backend.dto.User toDto(User user, Role role);
}
