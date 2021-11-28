package hu.bme.szgbizt.levendula.caffplacc.presentation;

import hu.bme.szgbizt.levendula.caffplacc.service.AdminUserService;
import hu.bme.szgbizt.levendula.caffplacc.user.AdminUserIF;
import hu.bme.szgbizt.levendula.caffplacc.user.UserCreateUpdateRequest;
import hu.bme.szgbizt.levendula.caffplacc.user.UserResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@PreAuthorize("hasRole('ROLE_ADMIN')")
@RequestMapping("/api/admin/settings")
public class AdminUserController implements AdminUserIF {

    private final AdminUserService service;

    public AdminUserController(AdminUserService service) {
        this.service = service;
    }

    @Override
    @GetMapping
    public Page<UserResponse> listUsers(@RequestParam(required = false, name = "username") String username, Pageable pageable) {
        return service.listUsers(username, pageable);
    }

    @Override
    @GetMapping("/{id}")
    public UserResponse getOneUser(@PathVariable String id) {
        return service.getOneUser(UUID.fromString(id));
    }

    @Override
    @PostMapping
    public UserResponse createUser(@RequestBody UserCreateUpdateRequest request) {
        return service.createUser(request);
    }

    @Override
    @PutMapping("/{id}")
    public UserResponse updateUser(@PathVariable String id, @RequestBody UserCreateUpdateRequest request) {
        return service.updateUser(UUID.fromString(id), request);
    }

    @Override
    @DeleteMapping("/{id}")
    public void deleteUser(@PathVariable String id) {
        service.deleteUser(UUID.fromString(id));
    }
}
