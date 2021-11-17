package hu.bme.szgbizt.levendula.caffplacc.refactorapi;

import hu.bme.szgbizt.levendula.caffplacc.exception.CaffplaccException;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

public interface UserAuthIF {
    @PostMapping("/register")
    ResponseEntity<?> saveUser(@RequestBody UserDto user) throws CaffplaccException;

    @PostMapping("/login")
    ResponseEntity<?> createAuthenticationToken(@RequestBody JwtRequest authenticationRequest) throws CaffplaccException;
}