package hu.bme.szgbizt.levendula.caffplacc.security;

import hu.bme.szgbizt.levendula.caffplacc.data.entity.RefreshToken;
import hu.bme.szgbizt.levendula.caffplacc.data.entity.User;
import hu.bme.szgbizt.levendula.caffplacc.exception.CaffplaccException;
import hu.bme.szgbizt.levendula.caffplacc.login.*;
import hu.bme.szgbizt.levendula.caffplacc.service.RefreshTokenService;
import io.jsonwebtoken.ExpiredJwtException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.Date;
import java.util.Objects;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@CrossOrigin
@RestController
@RequestMapping("/api/auth")
public class JwtAuthenticationController implements UserAuthIF {

    private final AuthenticationManager authenticationManager;
    private final JwtTokenUtil jwtTokenUtil;
    private final JwtUserDetailsService userDetailsService;
    private final SaveUserService saveUserService;
    private final RefreshTokenService refreshTokenService;

    public JwtAuthenticationController(AuthenticationManager authenticationManager, JwtTokenUtil jwtTokenUtil, JwtUserDetailsService userDetailsService, SaveUserService saveUserService, RefreshTokenService refreshTokenService) {
        this.authenticationManager = authenticationManager;
        this.jwtTokenUtil = jwtTokenUtil;
        this.userDetailsService = userDetailsService;
        this.saveUserService = saveUserService;
        this.refreshTokenService = refreshTokenService;
    }

    @PostMapping("/register")
    public ResponseEntity<?> saveUser(@RequestBody UserDto user) throws CaffplaccException {
        return ResponseEntity.ok(saveUserService.save(user));
    }

    @PostMapping("/login")
    public ResponseEntity<?> createAuthenticationToken(@RequestBody JwtRequest authenticationRequest) throws CaffplaccException {
        authenticate(authenticationRequest.getUsername(), authenticationRequest.getPassword());
        final User user = saveUserService.loadUserFromUsername(authenticationRequest.getUsername());
        final UserDetails userDetails = userDetailsService.loadUserByUsername(authenticationRequest.getUsername());
        final String token = jwtTokenUtil.generateToken(userDetails);
        final String refreshToken = jwtTokenUtil.generateRefreshToken(userDetails);
        final Date expirationDate = jwtTokenUtil.getExpirationDateFromToken(refreshToken);

        final UUID refreshTokenId = refreshTokenService.saveRefreshToken(user, refreshToken, expirationDate);
        log.info("Successfully saved refresh token with ID: " + refreshTokenId);

        return ResponseEntity.ok(new JwtResponse(token, refreshToken, user.getId().toString(), user.getUsername(), user.getRoles().stream().map(Objects::toString).collect(Collectors.toList())));
    }

    @PostMapping("/refresh")
    public ResponseEntity<?> refreshAuthenticationToken(@RequestBody JwtRefreshRequest refreshRequest) throws CaffplaccException {
        final String token = refreshRequest.getRefreshToken();
        final RefreshToken refreshToken = refreshTokenService.findByToken(token).orElseThrow(() -> new CaffplaccException("INVALID_REFRESH_TOKEN"));
        verifyExpiration(refreshToken.getToken());
        final UserDetails userDetails = userDetailsService.loadUserByUsername(refreshToken.getUser().getUsername());
        final String newToken = jwtTokenUtil.generateToken(userDetails);
        final User user = saveUserService.loadUserFromUsername(userDetails.getUsername());
        return ResponseEntity.ok(new JwtResponse(newToken, refreshToken.getToken(), user.getId().toString(), user.getUsername(), user.getRoles().stream().map(Objects::toString).collect(Collectors.toList())));
    }

    private void verifyExpiration(String refreshToken) {
        try {
            jwtTokenUtil.validateRefreshToken(refreshToken);
        } catch (ExpiredJwtException e) {
            refreshTokenService.deleteByToken(refreshToken);
            throw new CaffplaccException("REFRESH_TOKEN_EXPIRED");
        } catch (Exception e) {
            throw new CaffplaccException("REFRESH_TOKEN_ERROR");
        }
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