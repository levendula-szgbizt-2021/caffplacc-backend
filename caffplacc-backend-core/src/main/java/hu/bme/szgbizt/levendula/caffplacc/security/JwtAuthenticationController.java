package hu.bme.szgbizt.levendula.caffplacc.security;

import hu.bme.szgbizt.levendula.caffplacc.data.entity.RefreshToken;
import hu.bme.szgbizt.levendula.caffplacc.data.entity.User;
import hu.bme.szgbizt.levendula.caffplacc.login.*;
import hu.bme.szgbizt.levendula.caffplacc.exception.CaffplaccException;
import hu.bme.szgbizt.levendula.caffplacc.service.RefreshTokenService;
import io.jsonwebtoken.ExpiredJwtException;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.sql.Ref;
import java.util.Date;
import java.util.UUID;

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
        final UUID refreshtTokenId = refreshTokenService.saveRefreshToken(user, refreshToken, expirationDate);
        return ResponseEntity.ok(new JwtResponse(token, refreshToken));
    }

    @PostMapping("/refresh")
    public ResponseEntity<?> refreshAuthenticationToken(@RequestBody JwtRefreshRequest refreshRequest) throws  CaffplaccException {
        final String token = refreshRequest.getRefreshToken();
        final RefreshToken refreshToken = refreshTokenService.findByToken(token).orElseThrow(() -> new CaffplaccException("INVALID_REFRESH_TOKEN"));
        if(verifyExpiration(refreshToken.getToken())){
            final UserDetails userDetails = userDetailsService.loadUserByUsername(refreshToken.getUser().getUsername());
            final String newToken = jwtTokenUtil.generateToken(userDetails);
            return ResponseEntity.ok(new JwtResponse(newToken, refreshToken.getToken()));
        }
        return ResponseEntity.badRequest().body(new JwtResponse(null, null));
    }

    private boolean verifyExpiration(String refreshToken){
        try {
            return jwtTokenUtil.validateRefreshToken(refreshToken);
        } catch (ExpiredJwtException e){
            return false;
        } catch (Exception e){
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