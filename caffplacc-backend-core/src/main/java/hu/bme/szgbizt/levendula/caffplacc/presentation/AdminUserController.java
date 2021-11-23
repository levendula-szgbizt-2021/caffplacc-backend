package hu.bme.szgbizt.levendula.caffplacc.presentation;

import hu.bme.szgbizt.levendula.caffplacc.service.AdminUserService;
import hu.bme.szgbizt.levendula.caffplacc.user.AdminUserIF;
import hu.bme.szgbizt.levendula.caffplacc.user.UserCreateRequest;
import hu.bme.szgbizt.levendula.caffplacc.user.UserResponse;
import hu.bme.szgbizt.levendula.caffplacc.user.UserUpdateRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/api/admin/settings")
public class AdminUserController implements AdminUserIF {

    private final AdminUserService service;

    public AdminUserController(AdminUserService service) {
        this.service = service;
    }

    @Override
    public Page<UserResponse> listUsers(String query, Pageable pageable) {
        return service.listUsers(query, pageable);
    }

    @Override
    public UserResponse getOneUser(String id) {
        return service.getOneUser(UUID.fromString(id));
    }

    @Override
    public UserResponse createUser(UserCreateRequest request) {
        return service.createUser(request);
    }

    @Override
    public UserResponse updateUser(String id, UserUpdateRequest request) {
        return service.updateUser(UUID.fromString(id), request);
    }

    @Override
    public void deleteUser(String id) {
        service.deleteUser(UUID.fromString(id));
    }
}
