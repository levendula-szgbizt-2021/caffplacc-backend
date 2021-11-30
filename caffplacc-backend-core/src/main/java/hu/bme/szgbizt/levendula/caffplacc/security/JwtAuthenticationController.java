package hu.bme.szgbizt.levendula.caffplacc.security;

import hu.bme.szgbizt.levendula.caffplacc.exception.CaffplaccException;
import hu.bme.szgbizt.levendula.caffplacc.login.JwtRefreshRequest;
import hu.bme.szgbizt.levendula.caffplacc.login.JwtRequest;
import hu.bme.szgbizt.levendula.caffplacc.login.UserAuthIF;
import hu.bme.szgbizt.levendula.caffplacc.login.UserDto;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

@CrossOrigin
@RestController
@RequestMapping("/api/auth")
public class JwtAuthenticationController implements UserAuthIF {

    private final SaveUserService saveUserService;

    public JwtAuthenticationController(SaveUserService saveUserService) {
        this.saveUserService = saveUserService;
    }

    @PostMapping("/register")
    public ResponseEntity<?> saveUser(@RequestBody @Valid UserDto user) throws CaffplaccException {
        return ResponseEntity.ok(saveUserService.save(user));
    }

    @PostMapping("/login")
    public ResponseEntity<?> createAuthenticationToken(@RequestBody @Valid JwtRequest authenticationRequest) throws CaffplaccException {
        return ResponseEntity.ok(saveUserService.login(authenticationRequest));
    }

    @PostMapping("/refresh")
    public ResponseEntity<?> refreshAuthenticationToken(@RequestBody JwtRefreshRequest refreshRequest) throws CaffplaccException {
        return ResponseEntity.ok(saveUserService.refresh(refreshRequest));
    }
}