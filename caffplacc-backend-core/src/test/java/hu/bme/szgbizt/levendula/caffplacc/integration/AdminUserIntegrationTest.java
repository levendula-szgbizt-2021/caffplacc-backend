package hu.bme.szgbizt.levendula.caffplacc.integration;

import hu.bme.szgbizt.levendula.caffplacc.CaffplaccApplication;
import hu.bme.szgbizt.levendula.caffplacc.data.entity.User;
import hu.bme.szgbizt.levendula.caffplacc.data.entity.UserRole;
import hu.bme.szgbizt.levendula.caffplacc.data.repository.UserRepository;
import hu.bme.szgbizt.levendula.caffplacc.paging.PaginatedResponse;
import hu.bme.szgbizt.levendula.caffplacc.security.SecurityConstants;
import hu.bme.szgbizt.levendula.caffplacc.user.AdminUserResponse;
import hu.bme.szgbizt.levendula.caffplacc.user.UserCreateUpdateRequest;
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
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.*;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import javax.annotation.PostConstruct;
import java.util.*;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;

@Slf4j
@ExtendWith(SpringExtension.class)
@SpringBootTest(classes = CaffplaccApplication.class, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class AdminUserIntegrationTest {

    private String path;
    private User user;
    private User admin;

    @LocalServerPort
    private int port;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private TestRestTemplate restTemplate;

    @PostConstruct
    void init() {
        path = "http://localhost:" + port + "/api/admin/settings";
    }

    @BeforeEach
    void clear() {
        userRepository.deleteAll();
        user = new User();
        user.setRoles(List.of(UserRole.ROLE_USER));
        user.setPassword("secret");
        user.setUsername("TesztElek");
        user.setEmail("a@b.c");
        user = userRepository.save(user);

        admin = new User();
        admin.setRoles(List.of(UserRole.ROLE_USER, UserRole.ROLE_ADMIN));
        admin.setPassword("secret2");
        admin.setUsername("TesztElek2");
        admin.setEmail("d@e.f");
        admin = userRepository.save(admin);
    }

    @AfterEach
    void clearAll() {
        userRepository.deleteAll();
        user = null;
        admin = null;
    }

    @Test
    void endpointCanBeAccessedByAdminUser() {
        String tokenForUser = generateValidTokenForUserWithLongerExpirationDate(admin);
        HttpHeaders headers = new HttpHeaders();
        headers.add("Authorization", "Bearer "+tokenForUser );

        ResponseEntity<PaginatedResponse<AdminUserResponse>> response = restTemplate.exchange(
                path,
                HttpMethod.GET,
                new HttpEntity<>(headers),
                new ParameterizedTypeReference<PaginatedResponse<AdminUserResponse>>() {}
        );

        assertEquals(HttpStatus.OK, response.getStatusCode());
    }

    @Test
    void listingUsersWithoutGivenUsernameShouldReturnAllUser() {
        User user2 = new User();
        user2.setUsername("TesztTibor");
        user2.setPassword("pass2");
        user2.setEmail("j@k.l");
        user.setRoles(List.of(UserRole.ROLE_USER));
        userRepository.save(user2);

        String tokenForUser = generateValidTokenForUserWithLongerExpirationDate(admin);
        HttpHeaders headers = new HttpHeaders();
        headers.add("Authorization", "Bearer "+tokenForUser );

        ResponseEntity<PaginatedResponse<AdminUserResponse>> response = restTemplate.exchange(
                path,
                HttpMethod.GET,
                new HttpEntity<>(headers),
                new ParameterizedTypeReference<PaginatedResponse<AdminUserResponse>>() {}
        );

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(3, Objects.requireNonNull(response.getBody()).getTotalElements());
        assertArrayEquals(List.of("TesztElek","TesztElek2", "TesztTibor").toArray(), response.getBody().stream().map(AdminUserResponse::getUsername).toArray());
    }

    @Test
    void listingUsersWithGivenUsernameShouldReturnAllUserWhoseUsernameContainsThatPart() {
        User user2 = new User();
        user2.setUsername("TesztTibor");
        user2.setPassword("pass2");
        user2.setEmail("j@k.l");
        user.setRoles(List.of(UserRole.ROLE_USER));
        userRepository.save(user2);

        String tokenForUser = generateValidTokenForUserWithLongerExpirationDate(admin);
        HttpHeaders headers = new HttpHeaders();
        headers.add("Authorization", "Bearer "+tokenForUser );

        ResponseEntity<PaginatedResponse<AdminUserResponse>> response = restTemplate.exchange(
                path+"?username={query_string}",
                HttpMethod.GET,
                new HttpEntity<>(headers),
                new ParameterizedTypeReference<PaginatedResponse<AdminUserResponse>>() {},
                "Teszt"
        );

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(3, Objects.requireNonNull(response.getBody()).getTotalElements());
        assertArrayEquals(List.of("TesztElek","TesztElek2", "TesztTibor").toArray(), response.getBody().stream().map(AdminUserResponse::getUsername).toArray());
    }

    @Test
    void getOneUserShouldReturnTheResponseForThatUser() {
        String token = generateValidTokenForUserWithLongerExpirationDate(admin);
        HttpHeaders headers = new HttpHeaders();
        headers.add("Authorization", "Bearer "+token );
        ResponseEntity<AdminUserResponse> response = restTemplate.exchange(
                path + "/" + user.getId().toString(),
                HttpMethod.GET,
                new HttpEntity<>(headers),
                AdminUserResponse.class
        );

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(user.getUsername(), Objects.requireNonNull(response.getBody()).getUsername());
        assertEquals(user.getEmail(), response.getBody().getEmail());
        assertEquals(user.getId().toString(), response.getBody().getId());
    }

    @Test
    void getOneUserFromNonAdminShouldReturnForbidden() {
        String token = generateValidTokenForUserWithLongerExpirationDate(user);
        HttpHeaders headers = new HttpHeaders();
        headers.add("Authorization", "Bearer "+token );
        ResponseEntity<AdminUserResponse> response = restTemplate.exchange(
                path + "/" + user.getId().toString(),
                HttpMethod.GET,
                new HttpEntity<>(headers),
                AdminUserResponse.class
        );

        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
    }

    @Test
    void getOneUserShouldNotReturnValidResponseForNonExistingUser() {
        String token = generateValidTokenForUserWithLongerExpirationDate(admin);
        HttpHeaders headers = new HttpHeaders();
        headers.add("Authorization", "Bearer "+token );
        ResponseEntity<AdminUserResponse> response = restTemplate.exchange(
                path + "/" + UUID.randomUUID().toString(),
                HttpMethod.GET,
                new HttpEntity<>(headers),
                AdminUserResponse.class
        );

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
    }

    @Test
    void createUserShouldSucceedWithValidInformation() {
        String token = generateValidTokenForUserWithLongerExpirationDate(admin);
        HttpHeaders headers = new HttpHeaders();
        UserCreateUpdateRequest userCreateUpdateRequest = new UserCreateUpdateRequest("TesztElek3", "Pppppppppp1", "g@h.i", false);
        headers.add("Authorization", "Bearer "+token );
        ResponseEntity<AdminUserResponse> response = restTemplate.exchange(
                path,
                HttpMethod.POST,
                new HttpEntity<>(userCreateUpdateRequest, headers),
                AdminUserResponse.class
        );

        assertEquals(HttpStatus.OK, response.getStatusCode());
        UUID fromResponse = UUID.fromString(Objects.requireNonNull(response.getBody()).getId());

        ResponseEntity<AdminUserResponse> response2 = restTemplate.exchange(
                path + "/" + fromResponse.toString(),
                HttpMethod.GET,
                new HttpEntity<>(headers),
                AdminUserResponse.class
        );

        assertEquals(HttpStatus.OK, response2.getStatusCode());
        assertEquals(userCreateUpdateRequest.getUsername(), Objects.requireNonNull(response2.getBody()).getUsername());
        assertEquals(userCreateUpdateRequest.getEmail(), response2.getBody().getEmail());
        assertFalse(response2.getBody().isAdmin());
    }

    @Test
    void createUserShouldNotSucceedWithInValidInformation() {
        String token = generateValidTokenForUserWithLongerExpirationDate(admin);
        HttpHeaders headers = new HttpHeaders();
        UserCreateUpdateRequest userCreateUpdateRequest = new UserCreateUpdateRequest("TesztElek3", "1", "g@h.i", false);
        headers.add("Authorization", "Bearer "+token );
        ResponseEntity<AdminUserResponse> response = restTemplate.exchange(
                path,
                HttpMethod.POST,
                new HttpEntity<>(userCreateUpdateRequest, headers),
                AdminUserResponse.class
        );
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }

    @Test
    void creatingAlreadyExistingUser() {
        String token = generateValidTokenForUserWithLongerExpirationDate(admin);
        HttpHeaders headers = new HttpHeaders();
        UserCreateUpdateRequest userCreateUpdateRequest = new UserCreateUpdateRequest("TesztElek", "1", "g@h.i", false);
        headers.add("Authorization", "Bearer "+token );
        ResponseEntity<AdminUserResponse> response = restTemplate.exchange(
                path,
                HttpMethod.POST,
                new HttpEntity<>(userCreateUpdateRequest, headers),
                AdminUserResponse.class
        );
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }

    @Test
    void updateUserShouldChangeUsernameWhenGiven() {
        String token = generateValidTokenForUserWithLongerExpirationDate(admin);
        HttpHeaders headers = new HttpHeaders();
        UserCreateUpdateRequest userCreateUpdateRequest = new UserCreateUpdateRequest("TesztElek3", null, null, false);
        headers.add("Authorization", "Bearer "+token );
        ResponseEntity<AdminUserResponse> response = restTemplate.exchange(
                path + "/" + user.getId(),
                HttpMethod.PUT,
                new HttpEntity<>(userCreateUpdateRequest, headers),
                AdminUserResponse.class
        );

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(userCreateUpdateRequest.getUsername(), Objects.requireNonNull(response.getBody()).getUsername());

        ResponseEntity<AdminUserResponse> response2 = restTemplate.exchange(
                path + "/" + user.getId().toString(),
                HttpMethod.GET,
                new HttpEntity<>(headers),
                AdminUserResponse.class
        );

        assertEquals(HttpStatus.OK, response2.getStatusCode());
        assertEquals(userCreateUpdateRequest.getUsername(), Objects.requireNonNull(response2.getBody()).getUsername());
        assertEquals(user.getEmail(), response2.getBody().getEmail());
        assertEquals(user.getId().toString(), response2.getBody().getId());
    }

    @Test
    void updateUserShouldChangeAllInformationWhenGiven() {
        String token = generateValidTokenForUserWithLongerExpirationDate(admin);
        HttpHeaders headers = new HttpHeaders();
        UserCreateUpdateRequest userCreateUpdateRequest = new UserCreateUpdateRequest("TesztElek3", "Rgfdlkfjgdlfkgjj2", "e@f.g", false);
        headers.add("Authorization", "Bearer "+token );
        ResponseEntity<AdminUserResponse> response = restTemplate.exchange(
                path + "/" + user.getId(),
                HttpMethod.PUT,
                new HttpEntity<>(userCreateUpdateRequest, headers),
                AdminUserResponse.class
        );

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(userCreateUpdateRequest.getUsername(), Objects.requireNonNull(response.getBody()).getUsername());

        ResponseEntity<AdminUserResponse> response2 = restTemplate.exchange(
                path + "/" + user.getId().toString(),
                HttpMethod.GET,
                new HttpEntity<>(headers),
                AdminUserResponse.class
        );

        assertEquals(HttpStatus.OK, response2.getStatusCode());
        assertEquals(userCreateUpdateRequest.getUsername(), Objects.requireNonNull(response2.getBody()).getUsername());
        assertEquals(userCreateUpdateRequest.getEmail(), response2.getBody().getEmail());
        assertFalse(response2.getBody().isAdmin());
    }

    @Test
    void deleteUserShouldDeleteUserIfItExists() {
        String token = generateValidTokenForUserWithLongerExpirationDate(admin);
        HttpHeaders headers = new HttpHeaders();
        headers.add("Authorization", "Bearer "+token );
        ResponseEntity<AdminUserResponse> response = restTemplate.exchange(
                path + "/" + user.getId(),
                HttpMethod.DELETE,
                new HttpEntity<>(headers),
                AdminUserResponse.class
        );

        assertEquals(HttpStatus.OK, response.getStatusCode());
        UUID userID = user.getId();
        Optional<User> optionalUser = userRepository.findById(userID);
        assertThrows(NoSuchElementException.class, optionalUser::get);
    }

    @Test
    void deleteUserShouldNotReturnPositiveResponseGivenNonExistingUser() {
        String token = generateValidTokenForUserWithLongerExpirationDate(admin);
        HttpHeaders headers = new HttpHeaders();
        headers.add("Authorization", "Bearer "+token );
        ResponseEntity<AdminUserResponse> response = restTemplate.exchange(
                path + "/" + UUID.randomUUID(),
                HttpMethod.DELETE,
                new HttpEntity<>(headers),
                AdminUserResponse.class
        );

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
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
