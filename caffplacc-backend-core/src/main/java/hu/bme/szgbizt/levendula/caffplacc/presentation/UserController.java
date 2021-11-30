package hu.bme.szgbizt.levendula.caffplacc.presentation;

import hu.bme.szgbizt.levendula.caffplacc.exception.CaffplaccException;
import hu.bme.szgbizt.levendula.caffplacc.login.UserDto;
import hu.bme.szgbizt.levendula.caffplacc.service.UserService;
import hu.bme.szgbizt.levendula.caffplacc.user.UserProfileIF;
import hu.bme.szgbizt.levendula.caffplacc.user.UserResponse;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@PreAuthorize("hasRole('ROLE_USER')")
@RequestMapping("/api/user/settings")
public class UserController implements UserProfileIF {

    private final UserService service;

    public UserController(UserService service) {
        this.service = service;
    }

    @Override
    @GetMapping
    public UserResponse getUserData() throws CaffplaccException {
        return service.getUserData();
    }

    @Override
    @PostMapping
    public UserResponse changeUserData(UserDto dto) {
        return service.changeUserData(dto);
    }

    @Override
    @DeleteMapping
    public void deleteUserData() throws CaffplaccException {
        service.deleteUserData();
    }
}
