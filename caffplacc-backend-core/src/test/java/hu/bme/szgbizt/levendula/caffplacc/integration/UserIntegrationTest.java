package hu.bme.szgbizt.levendula.caffplacc.integration;

import hu.bme.szgbizt.levendula.caffplacc.CaffplaccApplication;
import hu.bme.szgbizt.levendula.caffplacc.data.entity.User;
import hu.bme.szgbizt.levendula.caffplacc.data.entity.UserRole;
import hu.bme.szgbizt.levendula.caffplacc.data.repository.UserRepository;
import hu.bme.szgbizt.levendula.caffplacc.login.UserDto;
import hu.bme.szgbizt.levendula.caffplacc.security.SecurityConstants;
import hu.bme.szgbizt.levendula.caffplacc.user.UserResponse;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.http.*;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.web.client.RestTemplate;

import javax.annotation.PostConstruct;
import javax.persistence.EntityNotFoundException;
import java.util.*;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;

@Slf4j
@ExtendWith(SpringExtension.class)
@SpringBootTest(classes = CaffplaccApplication.class, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class UserIntegrationTest {

    private String path;
    private User user;

    @LocalServerPort
    private int port;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private TestRestTemplate restTemplate;

    @PostConstruct
    void init() {
        path = "http://localhost:" + port + "/api/user/settings";
    }

    @BeforeEach
    void clear() {
        restTemplate = new TestRestTemplate();
        userRepository.deleteAll();
        user = new User();
        user.setRoles(List.of(UserRole.ROLE_USER));
        user.setPassword("secret");
        user.setUsername("TesztElek");
        user.setEmail("a@b.c");
        user = userRepository.save(user);
    }

    @AfterEach
    void clearAll() {
        userRepository.deleteAll();
        user = null;
    }

    @Test
    void getUserDateEndPointShouldReturnValidUserResponseForTheUser() {
        String tokenForUser = generateValidTokenForUserWithLongerExpirationDate(user);
        HttpHeaders headers = new HttpHeaders();
        headers.add("Authorization", "Bearer "+tokenForUser );

        ResponseEntity<UserResponse> response = restTemplate.exchange(
                path,
                HttpMethod.GET,
                new HttpEntity<>("parameters", headers),
                UserResponse.class
        );

        assertTrue(response.getStatusCode().is2xxSuccessful());
        assertEquals(user.getId().toString(), Objects.requireNonNull(response.getBody()).getId());
        assertEquals(user.getUsername(), response.getBody().getUsername());
        assertEquals(user.getEmail(), response.getBody().getEmail());
    }

    @Test
    void getUserDateShouldReturnUnAuthorizedWithoutValidToken() {
        HttpHeaders headers = new HttpHeaders();
        ResponseEntity<UserResponse> response = restTemplate.exchange(
                path,
                HttpMethod.GET,
                new HttpEntity<>(headers),
                UserResponse.class
        );
        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
    }

    @Test
    void changeUserDataShouldChangeUserName() {
        String tokenForUser = generateValidTokenForUserWithLongerExpirationDate(user);
        HttpHeaders headers = new HttpHeaders();
        UserDto userDto = new UserDto("TesztElek2", null, null);
        headers.add("Authorization", "Bearer "+tokenForUser );
        headers.setContentType(MediaType.APPLICATION_JSON);
        ResponseEntity<UserResponse> response = restTemplate.exchange(
                path,
                HttpMethod.POST,
                new HttpEntity<>(userDto, headers),
                UserResponse.class
        );

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(user.getId().toString(), Objects.requireNonNull(response.getBody()).getId());
        assertEquals(userDto.getUsername(), response.getBody().getUsername());
        assertEquals(user.getEmail(), response.getBody().getEmail());

        //New token is needed -> changing username manually for local user

        user.setUsername(userDto.getUsername());
        String newTokenForUser = generateValidTokenForUserWithLongerExpirationDate(user);
        HttpHeaders headers2 = new HttpHeaders();
        headers2.add("Authorization", "Bearer "+newTokenForUser );

        ResponseEntity<UserResponse> response2 = restTemplate.exchange(
                path,
                HttpMethod.GET,
                new HttpEntity<>("parameters", headers2),
                UserResponse.class
        );

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(user.getId().toString(), Objects.requireNonNull(response2.getBody()).getId());
        assertEquals(user.getUsername(), response2.getBody().getUsername());
        assertEquals(user.getEmail(), response2.getBody().getEmail());
    }

    @Test
    void changeUserDateShouldChangeAllValuesWhenGiven() {
        String tokenForUser = generateValidTokenForUserWithLongerExpirationDate(user);
        HttpHeaders headers = new HttpHeaders();
        UserDto userDto = new UserDto("TesztElek2", "Sasasasdads2", "b@c.d");
        headers.add("Authorization", "Bearer "+tokenForUser );
        headers.setContentType(MediaType.APPLICATION_JSON);
        ResponseEntity<UserResponse> response = restTemplate.exchange(
                path,
                HttpMethod.POST,
                new HttpEntity<>(userDto, headers),
                UserResponse.class
        );

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(user.getId().toString(), Objects.requireNonNull(response.getBody()).getId());
        assertEquals(userDto.getUsername(), response.getBody().getUsername());
        assertEquals(userDto.getEmail(), response.getBody().getEmail());

        //New token is needed -> changing username manually for local user

        user.setUsername(userDto.getUsername());
        String newTokenForUser = generateValidTokenForUserWithLongerExpirationDate(user);
        HttpHeaders headers2 = new HttpHeaders();
        headers2.add("Authorization", "Bearer "+newTokenForUser );

        ResponseEntity<UserResponse> response2 = restTemplate.exchange(
                path,
                HttpMethod.GET,
                new HttpEntity<>("parameters", headers2),
                UserResponse.class
        );

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(user.getId().toString(), Objects.requireNonNull(response.getBody()).getId());
        assertEquals(userDto.getUsername(), Objects.requireNonNull(response2.getBody()).getUsername());
        assertEquals(userDto.getEmail(), response2.getBody().getEmail());
    }

    @Test
    void deleteUserShouldDeleteUser() {
        String tokenForUser = generateValidTokenForUserWithLongerExpirationDate(user);
        HttpHeaders headers = new HttpHeaders();
        headers.add("Authorization", "Bearer "+tokenForUser );

        ResponseEntity<UserResponse> response = restTemplate.exchange(
                path,
                HttpMethod.DELETE,
                new HttpEntity<>(headers),
                UserResponse.class
        );

        assertEquals(HttpStatus.OK, response.getStatusCode());
        UUID userID = user.getId();
        Optional<User> optionalUser = userRepository.findById(userID);
        assertThrows(NoSuchElementException.class, optionalUser::get);
    }

    private String generateValidTokenForUserWithLongerExpirationDate(User user) {
        Calendar c = Calendar.getInstance();
        c.setTime(new Date(System.currentTimeMillis()));
        Date date = c.getTime();
        c.add(Calendar.HOUR, 1);
        Date expDate = c.getTime();
        Map<String, Object> claims = new HashMap<>();
        claims.put("roles", user.getRoles().stream().map(userRole -> new SimpleGrantedAuthority(userRole.toString())).collect(Collectors.toList()));
        return Jwts.builder().setClaims(claims).
                setSubject(user.getUsername()).
                setIssuedAt(date).setExpiration(expDate).
                signWith(SignatureAlgorithm.HS512, SecurityConstants.SECRET).compact();
    }
}
