package hu.bme.szgbizt.levendula.caffplacc.data.repository;

import hu.bme.szgbizt.levendula.caffplacc.data.entity.Animation;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@ExtendWith(SpringExtension.class)
class AnimationRepositoryTest {

    @Autowired
    private AnimationRepository animationRepository;

    private Animation animation;
    private Animation animation1;

    @BeforeEach
    void setUp() {
        UUID userid = UUID.randomUUID();
        animation = new Animation();
        animation.setId(UUID.randomUUID());
        animation.setUserId(userid);
        animation.setUploaderUserName("John");
        animation.setFileSizeInMb(5);
        animation.setUploadDate(Instant.now());
        animation.setTitle("Dinner");
        animation.setHash("eeff43434ffeebead445233"); //Only testing the repository, the validity of the hash is not tested here

        animation1 = new Animation();
        animation1.setId(UUID.randomUUID());
        animation1.setHash("eeffdfdff3213fffeeffaafaf33");
        animation1.setTitle("DinnerAtTheCastle");
        animation1.setUploadDate(Instant.now());
        animation1.setUploaderUserName("John");
        animation1.setFileSizeInMb(4);
        animation1.setUserId(userid);
    }

    @AfterEach
    void tearDown() {
        animationRepository.deleteAll();
        animation = null;
        animation1 = null;
    }

    @Test
    void givenAnimationToStoreThenShouldReturnStoredAnimation() {
        animationRepository.save(animation);

        Animation fetchedAnimation = animationRepository.findById(animation.getId()).get();
        assertEquals(animation.getId(), fetchedAnimation.getId());
    }

    @Test
    void givenNonExistingUserIdAndAnimationIdThenShouldThrowException() {
        Optional<Animation> fetchedAnimation = animationRepository.findByIdAndUserId(UUID.randomUUID(), UUID.randomUUID());
        assertThrows(NoSuchElementException.class, fetchedAnimation::get);
    }

    @Test
    void givenUserIdThenShouldReturnAnimationCorrespondingToGivenUserId() {
        animationRepository.save(animation);

        Animation fetchedAnimation = animationRepository.findById(animation.getId()).get();
        assertEquals(animation.getUserId(), fetchedAnimation.getUserId());
    }

    @Test
    void givenPartOfTitleThenShouldReturnAllAnimationWithTitleThatContainsThatPart() {
        animationRepository.save(animation1);
        animationRepository.save(animation);
        List<String> titles = Arrays.asList("DinnerAtTheCastle", "Dinner");


        Page<Animation> fetchAnimations = animationRepository.findAllByTitleContains("Dinner", PageRequest.of(0, 5));
        assertEquals(2, fetchAnimations.getNumberOfElements());
        List<String> titlesFound = fetchAnimations.get().map(Animation::getTitle).collect(Collectors.toList());
        assertArrayEquals(titles.toArray(), titlesFound.toArray());
    }

    @Test
    void givenUserIdThenShouldReturnAllAnimationBelongingToThatUser() {
        animationRepository.save(animation1);
        animationRepository.save(animation);
        List<String> UUIDs = Arrays.asList(animation1.getId().toString(), animation.getId().toString());

        Page<Animation> fetchAnimations = animationRepository.findAllByUserId(animation.getUserId(), PageRequest.of(0, 5));
        assertEquals(2, fetchAnimations.getNumberOfElements());
        List<String> foundUUIDs = fetchAnimations.get().map(animation2 -> animation2.getId().toString()).collect(Collectors.toList());
        assertArrayEquals(UUIDs.toArray(), foundUUIDs.toArray());
    }

    @Test
    void givenUserIdThenShouldDeleteAllAnimationBelongingToGiveUser() {
        animationRepository.save(animation);

        animationRepository.deleteAllByUserId(animation.getUserId());

        Page<Animation> fetchAnimations = animationRepository.findAllByUserId(animation.getUserId(), PageRequest.of(0, 5));
        assertEquals(0, fetchAnimations.getNumberOfElements());

    }
}