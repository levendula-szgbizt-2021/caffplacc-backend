package hu.bme.szgbizt.levendula.caffplacc.data.repository;

import hu.bme.szgbizt.levendula.caffplacc.data.entity.Animation;
import hu.bme.szgbizt.levendula.caffplacc.data.entity.Comment;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@ExtendWith(SpringExtension.class)
class CommentRepositoryTest {

    @Autowired
    private CommentRepository commentRepository;

    @Autowired
    private AnimationRepository animationRepository;

    private Animation animation;
    private Comment comment;
    private Comment comment2;

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
        animation.setHash("eeff43434ffeebead445233");

        comment = new Comment();
        comment.setId(UUID.randomUUID());
        comment.setUserId(userid);
        comment.setUserName("John");
        comment.setContent("Comment");
        comment.setDate(Instant.now());
        comment.setAnimation(animation);

        comment2 = new Comment();
        comment2.setId(UUID.randomUUID());
        comment2.setUserId(userid);
        comment2.setUserName("John");
        comment2.setContent("Comment2");
        comment2.setDate(Instant.now());
        comment2.setAnimation(animation);
    }

    @AfterEach
    void tearDown() {
        animationRepository.deleteAll();
        commentRepository.deleteAll();
        animation = null;
        comment = null;
        comment2 = null;
    }

    @Test
    void givenNonExistingIdThenShouldThrowException() {
        Optional<Comment> fetchedComment = commentRepository.findById(UUID.randomUUID());
        assertThrows(NoSuchElementException.class, fetchedComment::get);
    }

    @Test
    void givenCommentToSaveThenShouldReturnSavedComment() {
        animationRepository.save(animation);
        commentRepository.save(comment);
        assertEquals(comment.getId(), commentRepository.findById(comment.getId()).get().getId());
    }

    @Test
    void givenIdAndUserIdThenShouldReturnCommentCorrespondingToThatIdAndUserId() {
        animationRepository.save(animation);
        commentRepository.save(comment);

        Comment fetchedComment = commentRepository.findByIdAndUserId(comment.getId(), animation.getUserId()).get();
        assertEquals(comment.getId(), fetchedComment.getId());
    }

    @Test
    void givenAnimationIdThenShouldReturnAllCommentsBelongingToThatAnimation() {
        animationRepository.save(animation);
        commentRepository.save(comment);
        commentRepository.save(comment2);
        List<String> expectedCommentUUIDs = Arrays.asList(comment.getId().toString(), comment2.getId().toString());

        List<Comment> foundComments = commentRepository.findAllByAnimationId(animation.getId());
        List<String> foundUUIDs = foundComments.stream().map(comment1 -> comment1.getId().toString()).collect(Collectors.toList());
        assertArrayEquals(expectedCommentUUIDs.toArray(), foundUUIDs.toArray());
    }

    @Test
    void givenAnimationIdThenShouldDeleteAllCommentsOnThatAnimation() {
        animationRepository.save(animation);
        commentRepository.save(comment);
        commentRepository.save(comment2);

        commentRepository.deleteAllByAnimationId(animation.getId());
        List<Comment> foundComments = commentRepository.findAllByAnimationId(animation.getId());
        assertEquals(0, foundComments.size());
    }

    @Test
    void deleteAllByUserId() {
        animationRepository.save(animation);
        commentRepository.save(comment);
        commentRepository.save(comment2);

        commentRepository.deleteAllByUserId(animation.getUserId());
        Optional<Comment> fetchedComment = commentRepository.findById(comment.getId());
        assertThrows(NoSuchElementException.class, fetchedComment::get);
        fetchedComment = commentRepository.findById(comment2.getId());
        assertThrows(NoSuchElementException.class, fetchedComment::get);
    }
}