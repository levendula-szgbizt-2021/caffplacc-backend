package hu.bme.szgbizt.levendula.caffplacc.integration;

import hu.bme.szgbizt.levendula.caffplacc.CaffplaccApplication;
import hu.bme.szgbizt.levendula.caffplacc.animation.AnimationDetailedResponse;
import hu.bme.szgbizt.levendula.caffplacc.data.entity.Animation;
import hu.bme.szgbizt.levendula.caffplacc.data.repository.AnimationRepository;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import javax.annotation.PostConstruct;
import java.time.Instant;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

@Slf4j
@ExtendWith(SpringExtension.class)
@SpringBootTest(classes = CaffplaccApplication.class, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class AnimationIntegrationTest {

    private String path;

    @LocalServerPort
    private int port;

    @Autowired
    private AnimationRepository animationRepository;

    @Autowired
    private TestRestTemplate restTemplate;

    @PostConstruct
    public void init() {
        path = "http://localhost:" + port + "/api/anim";
        log.info("Tesztek futtatása az alábbi tokennel: " + "token");
        restTemplate.getRestTemplate().setInterceptors(Collections.singletonList((request, body, execution) -> {
            request.getHeaders().add("Authorization", "Bearer " + "token");
            return execution.execute(request, body);
        }));
    }

    @BeforeEach
    public void clear() {
        animationRepository.deleteAll();
    }

    //@Test
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
