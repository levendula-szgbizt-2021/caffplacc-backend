package hu.bme.szgbizt.levendula.caffplacc.presentation;

import hu.bme.szgbizt.levendula.caffplacc.data.entity.User;
import hu.bme.szgbizt.levendula.caffplacc.user.UserResponse;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

import java.util.UUID;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.ERROR)
public interface UserResponseMapper {

    UserResponse map(User user);

    default String map(UUID uuid) {
        return uuid.toString();
    }
}
