package hu.bme.szgbizt.levendula.caffplacc.integration;

import hu.bme.szgbizt.levendula.caffplacc.CaffplaccApplication;
import hu.bme.szgbizt.levendula.caffplacc.animation.AnimationDetailedResponse;
import hu.bme.szgbizt.levendula.caffplacc.data.entity.Animation;
import hu.bme.szgbizt.levendula.caffplacc.data.entity.User;
import hu.bme.szgbizt.levendula.caffplacc.data.entity.UserRole;
import hu.bme.szgbizt.levendula.caffplacc.data.repository.AnimationRepository;
import hu.bme.szgbizt.levendula.caffplacc.data.repository.UserRepository;
import hu.bme.szgbizt.levendula.caffplacc.security.SecurityConstants;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.context.event.annotation.AfterTestClass;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import javax.annotation.PostConstruct;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.util.*;

@Slf4j
@ExtendWith(SpringExtension.class)
@SpringBootTest(classes = CaffplaccApplication.class, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class AnimationIntegrationTest {

    private String path;
    private User user;

    @LocalServerPort
    private int port;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private AnimationRepository animationRepository;

    @Autowired
    private TestRestTemplate restTemplate;

    @PostConstruct
    public void init() {
        path = "http://localhost:" + port + "/api/anim";
        Calendar c = Calendar.getInstance();
        c.setTime(new Date(System.currentTimeMillis()));
        Date date = c.getTime();
        c.add(Calendar.YEAR, 2);
        Date expDate = c.getTime();
        Map<String, Object> claims = new HashMap<>();
        claims.put("roles", List.of(new SimpleGrantedAuthority(UserRole.ROLE_USER.toString())));
        String token = Jwts.builder().setClaims(claims).
                setSubject("TesztElek").
                setIssuedAt(date).setExpiration(expDate).
                signWith(SignatureAlgorithm.HS512, SecurityConstants.SECRET).compact();
        log.info("Tesztek futtatása az alábbi tokennel: " + token);
        restTemplate.getRestTemplate().setInterceptors(Collections.singletonList((request, body, execution) -> {
            request.getHeaders().add("Authorization", "Bearer " + token);
            return execution.execute(request, body);
        }));
    }

    @BeforeEach
    void clear() {
        user = new User();
        user.setRoles(List.of(UserRole.ROLE_USER));
        user.setPassword("secret");
        user.setUsername("TesztElek");
        user.setEmail("a@b.c");
        user = userRepository.save(user);
    }

    @AfterEach
    void clearAll() {
        animationRepository.deleteAll();
        userRepository.deleteAll();
        user = null;
    }

    @AfterAll
    static void tearDown() throws IOException {
        File files = new File("src/test/resources/files");
        Path filesPath = files.toPath();
        File previews = new File("src/test/resources/previews");
        Path previewsPath = previews.toPath();
        boolean result = Files.deleteIfExists(filesPath);
        result = Files.deleteIfExists(previewsPath);
    }

    @AfterTestClass
    public void deleteDb() throws IOException {
        File dbFile = new File("src/test/resources/caffplacc-backend-application-h2.mv.db");
        File dbFile2 = new File("src/test/resources/caffplacc-backend-application-h2.trace.db");
        Path dbFilePath = dbFile.toPath();
        Path dbFile2Path = dbFile2.toPath();
        boolean result = Files.deleteIfExists(dbFilePath);
        result = Files.deleteIfExists(dbFile2Path);
        System.out.println(result);
    }

    @Test
    void getAnimationDetailedResponseByIdAPI() {
        var entity = animationRepository.save(new Animation(UUID.fromString("312401f2-37db-427b-86ce-c10ab9675915"),
                UUID.fromString("312401f2-37db-427b-86ce-c10ab9675916"),
                "TesztElek",
                12,
                "TesztHash",
                Instant.EPOCH,
                "TesztTitle",
                List.of()));

        ResponseEntity<AnimationDetailedResponse> response = restTemplate.getForEntity(path + "/" + entity.getId(), AnimationDetailedResponse.class);
        Assertions.assertTrue(response.getStatusCode().is2xxSuccessful());
        Assertions.assertEquals(entity.getId().toString(), response.getBody().getId());
    }

}
