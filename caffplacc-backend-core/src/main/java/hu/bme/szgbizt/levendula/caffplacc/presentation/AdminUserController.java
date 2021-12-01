package hu.bme.szgbizt.levendula.caffplacc.presentation;

import hu.bme.szgbizt.levendula.caffplacc.service.UserService;
import hu.bme.szgbizt.levendula.caffplacc.user.AdminUserIF;
import hu.bme.szgbizt.levendula.caffplacc.user.AdminUserResponse;
import hu.bme.szgbizt.levendula.caffplacc.user.UserDataCreateRequest;
import hu.bme.szgbizt.levendula.caffplacc.user.UserDataUpdateRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.UUID;

@RestController
@PreAuthorize("hasRole('ROLE_ADMIN')")
@RequestMapping("/api/admin/settings")
public class AdminUserController implements AdminUserIF {

    private final UserService service;

    public AdminUserController(UserService service) {
        this.service = service;
    }

    @Override
    @GetMapping
    public Page<AdminUserResponse> listUsers(@RequestParam(required = false, name = "username") String username, Pageable pageable) {
        return service.listUsers(username, pageable);
    }

    @Override
    @GetMapping("/{id}")
    public AdminUserResponse getOneUser(@PathVariable String id) {
        return service.getOneUser(UUID.fromString(id));
    }

    @Override
    @PostMapping
    public AdminUserResponse createUser(@RequestBody @Valid UserDataCreateRequest request) {
        return service.createUserByAdmin(request);
    }

    @Override
    @PutMapping("/{id}")
    public AdminUserResponse updateUser(@PathVariable String id, @RequestBody @Valid UserDataUpdateRequest request) {
        return service.updateUserByAdmin(UUID.fromString(id), request);
    }

    @Override
    @DeleteMapping("/{id}")
    public void deleteUser(@PathVariable String id) {
        service.deleteUserByAdmin(UUID.fromString(id));
    }
}
