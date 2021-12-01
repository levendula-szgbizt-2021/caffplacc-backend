package hu.bme.szgbizt.levendula.caffplacc.login;

import hu.bme.szgbizt.levendula.caffplacc.exception.CaffplaccException;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import javax.validation.Valid;

public interface UserAuthIF {

    @PostMapping("/register")
    ResponseEntity<Object> saveUser(@RequestBody @Valid UserDto user) throws CaffplaccException;

    @PostMapping("/login")
    ResponseEntity<Object> createAuthenticationToken(@RequestBody @Valid JwtRequest authenticationRequest) throws CaffplaccException;

    @PostMapping("/refresh")
    ResponseEntity<Object> refreshAuthenticationToken(@RequestBody JwtRefreshRequest refreshRequest) throws CaffplaccException;
}