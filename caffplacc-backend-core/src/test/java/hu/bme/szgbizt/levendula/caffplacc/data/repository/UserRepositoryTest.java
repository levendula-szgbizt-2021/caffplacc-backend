package hu.bme.szgbizt.levendula.caffplacc.data.repository;

import hu.bme.szgbizt.levendula.caffplacc.data.entity.User;
import hu.bme.szgbizt.levendula.caffplacc.data.entity.UserRole;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.*;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@ExtendWith(SpringExtension.class)
class UserRepositoryTest {

    @Autowired
    private UserRepository userRepository;
    private User user;

    @BeforeEach
    void setUp() {
        List<UserRole> roles = new ArrayList<>();
        roles.add(UserRole.ROLE_USER);
        user = new User();
        user.setRoles(roles);
        user.setEmail("abcd@d.com");
        user.setUsername("name");
        user.setPassword("pass");
    }

    @AfterEach
    void tearDown() {
        userRepository.deleteAll();
        user = null;
    }

    @Test
    void givenUserToSaveThenShouldReturnSavedUser() {
        userRepository.save(user);
        assertEquals(user.getId(), userRepository.findById(user.getId()).get().getId());
    }

    @Test
    void givenNonExistingUsernameThenShouldThrowException() {
        Optional<User> fetchedUser = userRepository.findById(UUID.randomUUID());
        assertThrows(NoSuchElementException.class, fetchedUser::get);
    }

    @Test
    void givenUsernameThenShouldReturnUserWithThatUserName() {
        userRepository.save(user);

        User fetchedUser = userRepository.findByUsername(user.getUsername()).get();
        assertEquals("name", fetchedUser.getUsername());
    }

    @Test
    void givenPartOfUsernameThenShouldReturnAllUserWithUsernameThatContainsThatPart() {
        List<UserRole> roles = new ArrayList<>();
        roles.add(UserRole.ROLE_USER);
        User user1 = new User(UUID.randomUUID(), "name1", "pass", "email@b.com", roles);
        User user2 = new User(UUID.randomUUID(), "name2", "pass", "email@b.com", roles);
        userRepository.save(user1);
        userRepository.save(user2);
        List<String> names = Arrays.asList("name1", "name2");

        Page<User> users = userRepository.findAllByUsernameContains("name", PageRequest.of(0, 5));
        assertEquals(2, users.getNumberOfElements());
        List<String> namesFound = users.get().map(User::getUsername).collect(Collectors.toList());
        assertArrayEquals(names.toArray(), namesFound.toArray());

    }
}