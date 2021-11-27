package hu.bme.szgbizt.levendula.caffplacc.presentation;

import hu.bme.szgbizt.levendula.caffplacc.exception.CaffplaccException;
import hu.bme.szgbizt.levendula.caffplacc.login.UserDto;
import hu.bme.szgbizt.levendula.caffplacc.service.UserService;
import hu.bme.szgbizt.levendula.caffplacc.user.UserProfileIF;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@PreAuthorize("hasRole('ROLE_USER')")
@RequestMapping("/api/user/settings")
public class UserController implements UserProfileIF {

    private final UserService service;

    public UserController(UserService service) {
        this.service = service;
    }

    @Override
    @PostMapping
    public ResponseEntity<?> changeUserData(UserDto dto) throws CaffplaccException {
        return service.changeUserData(dto);
    }
}
