package hu.bme.szgbizt.levendula.caffplacc.integration;

import hu.bme.szgbizt.levendula.caffplacc.CaffplaccApplication;
import hu.bme.szgbizt.levendula.caffplacc.data.entity.RefreshToken;
import hu.bme.szgbizt.levendula.caffplacc.data.entity.User;
import hu.bme.szgbizt.levendula.caffplacc.data.entity.UserRole;
import hu.bme.szgbizt.levendula.caffplacc.data.repository.RefreshTokenRepository;
import hu.bme.szgbizt.levendula.caffplacc.data.repository.UserRepository;
import hu.bme.szgbizt.levendula.caffplacc.login.JwtRefreshRequest;
import hu.bme.szgbizt.levendula.caffplacc.login.JwtRequest;
import hu.bme.szgbizt.levendula.caffplacc.login.JwtResponse;
import hu.bme.szgbizt.levendula.caffplacc.login.UserDto;
import hu.bme.szgbizt.levendula.caffplacc.paging.PaginatedResponse;
import hu.bme.szgbizt.levendula.caffplacc.security.SecurityConstants;
import hu.bme.szgbizt.levendula.caffplacc.user.AdminUserResponse;
import hu.bme.szgbizt.levendula.caffplacc.user.UserResponse;
import io.jsonwebtoken.Jwts;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.*;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import javax.annotation.PostConstruct;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

@Slf4j
@ExtendWith(SpringExtension.class)
@SpringBootTest(classes = CaffplaccApplication.class, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class AuthenticationControllerIntegrationTest {

    private String path;
    private User user;
    private User admin;

    @LocalServerPort
    private int port;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RefreshTokenRepository refreshTokenRepository;

    @Autowired
    private PasswordEncoder bcryenEncoder;

    @Autowired
    private TestRestTemplate restTemplate;

    @PostConstruct
    void init() {
        path = "http://localhost:" + port + "/api/auth";
    }

    @BeforeEach
    void clear() {
        userRepository.deleteAll();
        user = new User();
        user.setRoles(List.of(UserRole.ROLE_USER));
        user.setPassword(bcryenEncoder.encode("Secretere2"));
        user.setUsername("TesztElek");
        user.setEmail("a@b.c");
        user = userRepository.save(user);

        admin = new User();
        admin.setRoles(List.of(UserRole.ROLE_USER, UserRole.ROLE_ADMIN));
        admin.setPassword(bcryenEncoder.encode("Secretetert2"));
        admin.setUsername("TesztElek2");
        admin.setEmail("d@e.f");
        admin = userRepository.save(admin);
    }

    @AfterEach
    void clearAll() {
        refreshTokenRepository.deleteAll();
        userRepository.deleteAll();
        user = null;
        admin = null;
    }

    @Test
    void registerCanMakeANewUserWhenValidInformationIsGiven() {
        HttpHeaders headers = new HttpHeaders();
        UserDto userDto = new UserDto("TesztTibor", "Seerferef2", "l@m.n");
        ResponseEntity<String> response = restTemplate.exchange(
                path+"/"+"register",
                HttpMethod.POST,
                new HttpEntity<>(userDto, headers),
                String.class
        );
        assertEquals(HttpStatus.OK, response.getStatusCode());
        Optional<User> fetchUser = userRepository.findByUsername("TesztTibor");
        assertEquals(Objects.requireNonNull(response.getBody()), fetchUser.get().getId().toString());
    }

    @Test
    void registerShouldNotnMakeANewUserWhenInValidInformationIsGiven() {
        HttpHeaders headers = new HttpHeaders();
        UserDto userDto = new UserDto("TesztTibor", "Sef2", "l@m.n");
        ResponseEntity<String> response = restTemplate.exchange(
                path+"/"+"register",
                HttpMethod.POST,
                new HttpEntity<>(userDto, headers),
                String.class
        );
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }

    @Test
    void loginValidInformationShouldReturnNecessaryInformationAndTokens() {
        HttpHeaders headers = new HttpHeaders();
        JwtRequest jwtRequest = new JwtRequest("TesztElek", "Secretere2");
        ResponseEntity<JwtResponse> response = restTemplate.exchange(
                path+"/"+"login",
                HttpMethod.POST,
                new HttpEntity<>(jwtRequest, headers),
                JwtResponse.class
        );

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(user.getUsername(), Objects.requireNonNull(response.getBody()).getUsername());
        assertEquals(user.getId().toString(), response.getBody().getUserId());
        String usernameFromToken = getUsernameFromToken(response.getBody().getToken());
        assertEquals(user.getUsername(), usernameFromToken);
        String rolesFromToken = getRolesFromToken(response.getBody().getToken());
        assertEquals("[{authority=ROLE_USER}]", rolesFromToken);
        String usernameFromRefreshToken = getUsernameFromToken(response.getBody().getRefreshToken());
        assertEquals(user.getUsername(), usernameFromRefreshToken);
    }

    @Test
    void cantLoginWithWrongInformation() {
        HttpHeaders headers = new HttpHeaders();
        JwtRequest jwtRequest = new JwtRequest("TesztCsabi", "Secretere2");
        ResponseEntity<JwtResponse> response = restTemplate.exchange(
                path+"/"+"login",
                HttpMethod.POST,
                new HttpEntity<>(jwtRequest, headers),
                JwtResponse.class
        );

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }

    @Test
    void refreshEndpointGivesBackValidToken() {
        HttpHeaders headers = new HttpHeaders();
        JwtRequest jwtRequest = new JwtRequest("TesztElek", "Secretere2");
        ResponseEntity<JwtResponse> response = restTemplate.exchange(
                path+"/"+"login",
                HttpMethod.POST,
                new HttpEntity<>(jwtRequest, headers),
                JwtResponse.class
        );

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(user.getUsername(), Objects.requireNonNull(response.getBody()).getUsername());
        assertEquals(user.getId().toString(), response.getBody().getUserId());
        String usernameFromToken = getUsernameFromToken(response.getBody().getToken());
        assertEquals(user.getUsername(), usernameFromToken);
        String rolesFromToken = getRolesFromToken(response.getBody().getToken());
        assertEquals("[{authority=ROLE_USER}]", rolesFromToken);
        String usernameFromRefreshToken = getUsernameFromToken(response.getBody().getRefreshToken());
        assertEquals(user.getUsername(), usernameFromRefreshToken);
        HttpHeaders headers2 = new HttpHeaders();
        JwtRefreshRequest jwtRefreshRequest = new JwtRefreshRequest(Objects.requireNonNull(response.getBody()).getRefreshToken());
        ResponseEntity<JwtResponse> response2 = restTemplate.exchange(
                path+"/"+"refresh",
                HttpMethod.POST,
                new HttpEntity<>(jwtRefreshRequest, headers2),
                JwtResponse.class
        );
        assertEquals(HttpStatus.OK, response2.getStatusCode());
        assertEquals(user.getUsername(), Objects.requireNonNull(response2.getBody()).getUsername());
        assertEquals(user.getId().toString(), response.getBody().getUserId());
        String usernameFromToken2 = getUsernameFromToken(response2.getBody().getToken());
        assertEquals(user.getUsername(), usernameFromToken2);
        String rolesFromToken2 = getRolesFromToken(response2.getBody().getToken());
        assertEquals("[{authority=ROLE_USER}]", rolesFromToken2);
        String usernameFromRefreshToken2 = getUsernameFromToken(response2.getBody().getRefreshToken());
        assertEquals(user.getUsername(), usernameFromRefreshToken2);
    }

    private String getUsernameFromToken(String token) {
        return Jwts.parser().setSigningKey(SecurityConstants.SECRET).parseClaimsJws(token).getBody().getSubject();
    }

    private String getRolesFromToken(String token) {
        return Jwts.parser().setSigningKey(SecurityConstants.SECRET).parseClaimsJws(token).getBody().get("roles").toString();
    }
}
