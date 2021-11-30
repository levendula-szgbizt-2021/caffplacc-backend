package hu.bme.szgbizt.levendula.caffplacc.integration;

import hu.bme.szgbizt.levendula.caffplacc.CaffplaccApplication;
import hu.bme.szgbizt.levendula.caffplacc.animation.*;
import hu.bme.szgbizt.levendula.caffplacc.data.entity.Animation;
import hu.bme.szgbizt.levendula.caffplacc.data.entity.User;
import hu.bme.szgbizt.levendula.caffplacc.data.entity.UserRole;
import hu.bme.szgbizt.levendula.caffplacc.data.repository.AnimationRepository;
import hu.bme.szgbizt.levendula.caffplacc.data.repository.CommentRepository;
import hu.bme.szgbizt.levendula.caffplacc.data.repository.UserRepository;
import hu.bme.szgbizt.levendula.caffplacc.paging.PaginatedResponse;
import hu.bme.szgbizt.levendula.caffplacc.security.SecurityConstants;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
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
    private UUID userId;

    @LocalServerPort
    private int port;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private AnimationRepository animationRepository;

    @Autowired
    private CommentRepository commentRepository;

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
        commentRepository.deleteAll();
        animationRepository.deleteAll();
        userRepository.deleteAll();
        user = null;
        user = new User();
        user.setRoles(List.of(UserRole.ROLE_USER));
        user.setPassword("secret");
        user.setUsername("TesztElek");
        user.setEmail("a@b.c");
        user = userRepository.save(user);
        userId = user.getId();

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
    void testGetOneAnimationDetailedResponseById() {
        var entity = animationRepository.save(
                new Animation(
                        UUID.fromString("312401f2-37db-427b-86ce-c10ab9675915"),
                        userId,
                        "TesztElek",
                        12,
                        "TesztHash",
                        Instant.EPOCH,
                        "TesztTitle",
                        List.of()));

        ResponseEntity<AnimationDetailedResponse> response = restTemplate.getForEntity(path + "/" + entity.getId(), AnimationDetailedResponse.class);
        Assertions.assertTrue(response.getStatusCode().is2xxSuccessful());
        Assertions.assertEquals(entity.getId().toString(), Objects.requireNonNull(response.getBody()).getId());
    }

    @Test
    void testListMyAnimationsForUser() {
        var entity1 = animationRepository.save(new Animation(
                UUID.fromString("3f6be0e9-f275-4698-a1c2-0d4baede541e"), userId,
                "TesztElek", 11, "TesztHash1", Instant.EPOCH, "TesztTitle1", List.of()));
        var entity2 = animationRepository.save(new Animation(
                UUID.fromString("7222bf30-b2d7-440c-8e93-fbc5ebe2eb28"), userId,
                "TesztElek", 11, "TesztHash2", Instant.EPOCH, "TesztTitle2", List.of()));
        ResponseEntity<PaginatedResponse<AnimationResponse>> responsePage = restTemplate.exchange(path, HttpMethod.GET, null, new ParameterizedTypeReference<>() {
        });
        Assertions.assertTrue(responsePage.getStatusCode().is2xxSuccessful());
        Assertions.assertNotNull(responsePage.getBody());
        Assertions.assertNotNull(responsePage.getBody().getContent());
        Assertions.assertEquals(2, responsePage.getBody().getContent().size());
        Assertions.assertEquals(2, responsePage.getBody().getTotalElements());
        List<AnimationResponse> receivedResponseList = responsePage.getBody().getContent();
        Assertions.assertEquals(entity1.getId().toString(), receivedResponseList.get(0).getId());
        Assertions.assertEquals(entity1.getTitle(), receivedResponseList.get(0).getTitle());
        Assertions.assertEquals(entity2.getId().toString(), receivedResponseList.get(1).getId());
        Assertions.assertEquals(entity2.getTitle(), receivedResponseList.get(1).getTitle());
    }

    @Test
    void testListAllAnimations() {
        var entity1 = animationRepository.save(new Animation(
                UUID.fromString("3f6be0e9-f275-4698-a1c2-0d4baede541e"),
                UUID.fromString("312401f2-37db-427b-86ce-c10ab9675916"),
                "TesztElek", 11, "TesztHash1", Instant.EPOCH, "TesztTitle1", List.of()));
        var entity2 = animationRepository.save(new Animation(
                UUID.fromString("7222bf30-b2d7-440c-8e93-fbc5ebe2eb28"),
                UUID.fromString("312401f2-37db-427b-86ce-c10ab9675916"),
                "TesztElek", 11, "TesztHash2", Instant.EPOCH, "TesztTitle2", List.of()));
        ResponseEntity<PaginatedResponse<AnimationResponse>> responsePage = restTemplate.exchange(path + "/search", HttpMethod.GET, null, new ParameterizedTypeReference<>() {
        });
        Assertions.assertTrue(responsePage.getStatusCode().is2xxSuccessful());
        Assertions.assertNotNull(responsePage.getBody());
        Assertions.assertNotNull(responsePage.getBody().getContent());
        Assertions.assertEquals(2, responsePage.getBody().getContent().size());
        Assertions.assertEquals(2, responsePage.getBody().getTotalElements());
        List<AnimationResponse> receivedResponseList = responsePage.getBody().getContent();
        Assertions.assertEquals(entity1.getId().toString(), receivedResponseList.get(0).getId());
        Assertions.assertEquals(entity1.getTitle(), receivedResponseList.get(0).getTitle());
        Assertions.assertEquals(entity2.getId().toString(), receivedResponseList.get(1).getId());
        Assertions.assertEquals(entity2.getTitle(), receivedResponseList.get(1).getTitle());
    }

    @Test
    void testCreateAnimation() {
        //AnimationResponse createAnimation(@RequestParam String title, @RequestParam("file") MultipartFile file);
    }

    @Test
    void testUpdateAnimationTitle() {
        String oldTitle = "Old Title";
        String newTitle = "New Title";
        var entity = animationRepository.save(new Animation(
                UUID.fromString("3f6be0e9-f275-4698-a1c2-0d4baede541e"), userId,
                "TesztElek", 11, "TesztHash1", Instant.EPOCH, oldTitle, List.of()));

        ResponseEntity<AnimationDetailedResponse> getResponse1 = restTemplate.getForEntity(path + "/" + entity.getId(), AnimationDetailedResponse.class);
        Assertions.assertTrue(getResponse1.getStatusCode().is2xxSuccessful());
        Assertions.assertEquals(entity.getTitle(), Objects.requireNonNull(getResponse1.getBody()).getTitle());

        ResponseEntity<AnimationResponse> putResponse = restTemplate.exchange(path + "/" + entity.getId(), HttpMethod.PUT, new HttpEntity<>(new AnimationUpdateRequest(newTitle)), AnimationResponse.class);
        Assertions.assertTrue(putResponse.getStatusCode().is2xxSuccessful());
        Assertions.assertEquals(newTitle, Objects.requireNonNull(putResponse.getBody()).getTitle());

        ResponseEntity<AnimationDetailedResponse> getResponse2 = restTemplate.getForEntity(path + "/" + entity.getId(), AnimationDetailedResponse.class);
        Assertions.assertTrue(getResponse2.getStatusCode().is2xxSuccessful());
        Assertions.assertEquals(newTitle, Objects.requireNonNull(getResponse2.getBody()).getTitle());
    }

    @Test
    void testDeleteAnimationById() {
        var entity = animationRepository.save(new Animation(
                UUID.fromString("3f6be0e9-f275-4698-a1c2-0d4baede541e"), userId,
                "TesztElek", 11, "TesztHash1", Instant.EPOCH, "TesztTitle", List.of()));

        ResponseEntity<AnimationDetailedResponse> getResponse1 = restTemplate.getForEntity(path + "/" + entity.getId(), AnimationDetailedResponse.class);
        Assertions.assertTrue(getResponse1.getStatusCode().is2xxSuccessful());
        Assertions.assertEquals(0, Objects.requireNonNull(getResponse1.getBody()).getComments().size());

        restTemplate.delete(path + "/" + entity.getId());

        ResponseEntity<AnimationDetailedResponse> getResponse2 = restTemplate.getForEntity(path + "/" + entity.getId(), AnimationDetailedResponse.class);
        Assertions.assertTrue(getResponse2.getStatusCode().is5xxServerError());
    }

    @Test
    void testPreviewAnimationById() {
        //ResponseEntity<?> previewAnimation(@PathVariable String id);
    }

    @Test
    void testDownloadAnimationById() {
        //ResponseEntity<?> downloadAnimation(@PathVariable String id);
    }

    @Test
    void testCreateCommentForAnimation() {
        String comment = "testContent";
        var entity = animationRepository.save(new Animation(
                UUID.fromString("3f6be0e9-f275-4698-a1c2-0d4baede541e"), UUID.fromString("312401f2-37db-427b-86ce-c10ab9675916"),
                "TesztElek", 11, "TesztHash1", Instant.EPOCH, "TesztTitle", List.of()));

        ResponseEntity<AnimationDetailedResponse> getResponse1 = restTemplate.getForEntity(path + "/" + entity.getId(), AnimationDetailedResponse.class);
        Assertions.assertTrue(getResponse1.getStatusCode().is2xxSuccessful());
        Assertions.assertEquals(0, Objects.requireNonNull(getResponse1.getBody()).getComments().size());

        restTemplate.exchange(path + "/" + entity.getId() + "/comment", HttpMethod.POST, new HttpEntity<>(new CommentCreateUpdateRequest(comment)), CommentResponse.class);

        ResponseEntity<AnimationDetailedResponse> getResponse2 = restTemplate.getForEntity(path + "/" + entity.getId(), AnimationDetailedResponse.class);
        Assertions.assertEquals(1, Objects.requireNonNull(getResponse2.getBody()).getComments().size());
        Assertions.assertEquals(comment, getResponse2.getBody().getComments().get(0).getContent());
    }

    @Test
    void testUpdateComment() {
        String oldComment = "Old Comment";
        String newComment = "New Comment";
        var entity = animationRepository.save(new Animation(
                UUID.fromString("3f6be0e9-f275-4698-a1c2-0d4baede541e"), UUID.fromString("312401f2-37db-427b-86ce-c10ab9675916"),
                "TesztElek", 11, "TesztHash1", Instant.EPOCH, "TesztTitle", List.of()));

        ResponseEntity<AnimationDetailedResponse> getResponse1 = restTemplate.getForEntity(path + "/" + entity.getId(), AnimationDetailedResponse.class);
        Assertions.assertTrue(getResponse1.getStatusCode().is2xxSuccessful());
        Assertions.assertEquals(0, Objects.requireNonNull(getResponse1.getBody()).getComments().size());

        ResponseEntity<CommentResponse> postResponse = restTemplate.exchange(path + "/" + entity.getId() + "/comment", HttpMethod.POST, new HttpEntity<>(new CommentCreateUpdateRequest(oldComment)), CommentResponse.class);

        restTemplate.exchange(path + "/" + entity.getId() + "/comment" + "/" + Objects.requireNonNull(postResponse.getBody()).getId(), HttpMethod.PUT, new HttpEntity<>(new CommentCreateUpdateRequest(newComment)), CommentResponse.class);

        ResponseEntity<AnimationDetailedResponse> getResponse2 = restTemplate.getForEntity(path + "/" + entity.getId(), AnimationDetailedResponse.class);
        Assertions.assertEquals(1, Objects.requireNonNull(getResponse2.getBody()).getComments().size());
        Assertions.assertEquals(newComment, getResponse2.getBody().getComments().get(0).getContent());
    }

    @Test
    void testDeleteComment() {
        String comment = "testContent";
        var entity = animationRepository.save(new Animation(
                UUID.fromString("3f6be0e9-f275-4698-a1c2-0d4baede541e"), UUID.fromString("312401f2-37db-427b-86ce-c10ab9675916"),
                "TesztElek", 11, "TesztHash1", Instant.EPOCH, "TesztTitle", List.of()));

        ResponseEntity<AnimationDetailedResponse> getResponse1 = restTemplate.getForEntity(path + "/" + entity.getId(), AnimationDetailedResponse.class);
        Assertions.assertTrue(getResponse1.getStatusCode().is2xxSuccessful());
        Assertions.assertEquals(0, Objects.requireNonNull(getResponse1.getBody()).getComments().size());

        ResponseEntity<CommentResponse> postResponse = restTemplate.exchange(path + "/" + entity.getId() + "/comment", HttpMethod.POST, new HttpEntity<>(new CommentCreateUpdateRequest(comment)), CommentResponse.class);

        ResponseEntity<AnimationDetailedResponse> getResponse2 = restTemplate.getForEntity(path + "/" + entity.getId(), AnimationDetailedResponse.class);
        Assertions.assertEquals(1, Objects.requireNonNull(getResponse2.getBody()).getComments().size());
        Assertions.assertEquals(comment, getResponse2.getBody().getComments().get(0).getContent());

        restTemplate.delete(path + "/" + entity.getId() + "/comment" + "/" + Objects.requireNonNull(postResponse.getBody()).getId());

        ResponseEntity<AnimationDetailedResponse> getResponse3 = restTemplate.getForEntity(path + "/" + entity.getId(), AnimationDetailedResponse.class);
        Assertions.assertEquals(0, Objects.requireNonNull(getResponse3.getBody()).getComments().size());
    }

}
