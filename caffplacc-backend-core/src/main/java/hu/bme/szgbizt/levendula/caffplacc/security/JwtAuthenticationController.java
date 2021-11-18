package hu.bme.szgbizt.levendula.caffplacc.security;

import hu.bme.szgbizt.levendula.caffplacc.login.JwtRequest;
import hu.bme.szgbizt.levendula.caffplacc.login.JwtResponse;
import hu.bme.szgbizt.levendula.caffplacc.login.UserAuthIF;
import hu.bme.szgbizt.levendula.caffplacc.login.UserDto;
import hu.bme.szgbizt.levendula.caffplacc.exception.CaffplaccException;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@CrossOrigin
@RestController
@RequestMapping("/api/auth")
public class JwtAuthenticationController implements UserAuthIF {

    private final AuthenticationManager authenticationManager;

    private final JwtTokenUtil jwtTokenUtil;

    private final JwtUserDetailsService userDetailsService;

    private final SaveUserService saveUserService;

    public JwtAuthenticationController(AuthenticationManager authenticationManager, JwtTokenUtil jwtTokenUtil, JwtUserDetailsService userDetailsService, SaveUserService saveUserService) {
        this.authenticationManager = authenticationManager;
        this.jwtTokenUtil = jwtTokenUtil;
        this.userDetailsService = userDetailsService;
        this.saveUserService = saveUserService;
    }

    @PostMapping("/register")
    public ResponseEntity<?> saveUser(@RequestBody UserDto user) throws CaffplaccException {
        return ResponseEntity.ok(saveUserService.save(user));
    }

    @PostMapping("/login")
    public ResponseEntity<?> createAuthenticationToken(@RequestBody JwtRequest authenticationRequest) throws CaffplaccException {
        authenticate(authenticationRequest.getUsername(), authenticationRequest.getPassword());
        final UserDetails userDetails = userDetailsService.loadUserByUsername(authenticationRequest.getUsername());
        final String token = jwtTokenUtil.generateToken(userDetails);
        return ResponseEntity.ok(new JwtResponse(token));
    }

    private void authenticate(String username, String password) throws CaffplaccException {
        try {
            authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(username, password));
        } catch (DisabledException e) {
            throw new CaffplaccException("USER_DISABLED");
        } catch (BadCredentialsException e) {
            throw new CaffplaccException("INVALID_CREDENTIALS");
        }
    }
}