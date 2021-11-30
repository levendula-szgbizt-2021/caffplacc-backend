package hu.bme.szgbizt.levendula.caffplacc.login;

import hu.bme.szgbizt.levendula.caffplacc.exception.CaffplaccException;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import javax.validation.Valid;

public interface UserAuthIF {

    @PostMapping("/register")
    ResponseEntity<?> saveUser(@RequestBody @Valid UserDto user) throws CaffplaccException;

    @PostMapping("/login")
    ResponseEntity<?> createAuthenticationToken(@RequestBody @Valid JwtRequest authenticationRequest) throws CaffplaccException;

    @PostMapping("/refresh")
    ResponseEntity<?> refreshAuthenticationToken(@RequestBody JwtRefreshRequest refreshRequest) throws CaffplaccException;
}