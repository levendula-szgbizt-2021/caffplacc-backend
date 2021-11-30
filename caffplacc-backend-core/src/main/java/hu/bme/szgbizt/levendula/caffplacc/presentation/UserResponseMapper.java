package hu.bme.szgbizt.levendula.caffplacc.presentation;

import hu.bme.szgbizt.levendula.caffplacc.data.entity.User;
import hu.bme.szgbizt.levendula.caffplacc.data.entity.UserRole;
import hu.bme.szgbizt.levendula.caffplacc.user.AdminUserResponse;
import hu.bme.szgbizt.levendula.caffplacc.user.UserResponse;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

import java.util.UUID;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.ERROR)
public interface UserResponseMapper {

    UserResponse map(User user);

    default AdminUserResponse mapToAdminUserResponse(User user){
        return new AdminUserResponse(user.getId().toString(), user.getUsername(), user.getEmail(), user.getRoles().contains(UserRole.ROLE_ADMIN));
    }

    default String map(UUID uuid) {
        return uuid.toString();
    }
}
