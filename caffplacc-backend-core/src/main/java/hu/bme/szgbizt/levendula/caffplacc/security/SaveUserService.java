package hu.bme.szgbizt.levendula.caffplacc.security;

import hu.bme.szgbizt.levendula.caffplacc.data.entity.RefreshToken;
import hu.bme.szgbizt.levendula.caffplacc.data.entity.User;
import hu.bme.szgbizt.levendula.caffplacc.data.entity.UserRole;
import hu.bme.szgbizt.levendula.caffplacc.data.repository.UserRepository;
import hu.bme.szgbizt.levendula.caffplacc.exception.CaffplaccException;
import hu.bme.szgbizt.levendula.caffplacc.login.JwtRefreshRequest;
import hu.bme.szgbizt.levendula.caffplacc.login.JwtRequest;
import hu.bme.szgbizt.levendula.caffplacc.login.JwtResponse;
import hu.bme.szgbizt.levendula.caffplacc.login.UserDto;
import hu.bme.szgbizt.levendula.caffplacc.service.RefreshTokenService;
import io.jsonwebtoken.ExpiredJwtException;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
@AllArgsConstructor
public class SaveUserService {

    private final UserRepository userRepository;
    private final PasswordEncoder bcryptEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtTokenUtil jwtTokenUtil;
    private final JwtUserDetailsService userDetailsService;
    private final RefreshTokenService refreshTokenService;

    public User loadUserFromUsername(String username) {
        return userRepository.findByUsername(username).orElseThrow(() -> new UsernameNotFoundException("User not found with username: " + username));
    }

    public String save(UserDto user) {
        User newUser = new User();
        newUser.setUsername(user.getUsername());
        newUser.setPassword(bcryptEncoder.encode(user.getPassword()));
        newUser.setEmail(user.getEmail());
        newUser.setRoles(List.of(UserRole.ROLE_USER));
        return userRepository.save(newUser).getId().toString();
    }


    public JwtResponse login(JwtRequest authenticationRequest) {
        authenticate(authenticationRequest.getUsername(), authenticationRequest.getPassword());
        final User user = loadUserFromUsername(authenticationRequest.getUsername());
        final UserDetails userDetails = userDetailsService.loadUserByUsername(authenticationRequest.getUsername());
        final String token = jwtTokenUtil.generateToken(userDetails);
        final String refreshToken = jwtTokenUtil.generateRefreshToken(userDetails);
        final Date expirationDate = jwtTokenUtil.getExpirationDateFromToken(refreshToken);

        final UUID refreshTokenId = refreshTokenService.saveRefreshToken(user, refreshToken, expirationDate);
        log.info("Successfully saved refresh token with ID: " + refreshTokenId);
        return (new JwtResponse(token, refreshToken, user.getId().toString(), user.getUsername(), user.getRoles().stream().map(Objects::toString).collect(Collectors.toList())));
    }

    public JwtResponse refresh(JwtRefreshRequest refreshRequest) {
        final String token = refreshRequest.getRefreshToken();
        final RefreshToken refreshToken = refreshTokenService.findByToken(token).orElseThrow(() -> new CaffplaccException("INVALID_REFRESH_TOKEN"));
        verifyExpiration(refreshToken.getToken());
        final UserDetails userDetails = userDetailsService.loadUserByUsername(refreshToken.getUser().getUsername());
        final String newToken = jwtTokenUtil.generateToken(userDetails);
        final User user = loadUserFromUsername(userDetails.getUsername());
        return (new JwtResponse(newToken, refreshToken.getToken(), user.getId().toString(), user.getUsername(), user.getRoles().stream().map(Objects::toString).collect(Collectors.toList())));
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