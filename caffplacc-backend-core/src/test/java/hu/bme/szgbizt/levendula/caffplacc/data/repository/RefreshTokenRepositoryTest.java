package hu.bme.szgbizt.levendula.caffplacc.data.repository;

import hu.bme.szgbizt.levendula.caffplacc.data.entity.RefreshToken;
import hu.bme.szgbizt.levendula.caffplacc.data.entity.User;
import hu.bme.szgbizt.levendula.caffplacc.data.entity.UserRole;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.time.Instant;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@ExtendWith(SpringExtension.class)
class RefreshTokenRepositoryTest {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RefreshTokenRepository refreshTokenRepository;

    private User user;
    private RefreshToken refreshtoken;

    @BeforeEach
    void setUp() {
        List<UserRole> roles = new ArrayList<>();
        roles.add(UserRole.ROLE_USER);
        user = new User();
        user.setRoles(roles);
        user.setEmail("abcd@d.com");
        user.setUsername("name");
        user.setPassword("pass");

        refreshtoken = new RefreshToken();
        refreshtoken.setToken("sdfsdlfkbdfdfgldfkgmsldfmsdf"); //Only testing the repository, the validity of the token is not tested here
        refreshtoken.setExpiryDate(Instant.now());
        refreshtoken.setUser(user);

    }

    @AfterEach
    void tearDown() {
        refreshTokenRepository.deleteAll();
        refreshtoken = null;
        user = null;
    }

    @Test
    void givenRefreshtokenToStoreThenShouldReturnStoredRefreshToken() {
        userRepository.save(user);
        refreshTokenRepository.save(refreshtoken);

        assertEquals(refreshtoken.getId(), refreshTokenRepository.findById(refreshtoken.getId()).get().getId());
    }

    @Test
    void givenNonExistingTokenThenShouldThrowException() {
        Optional<RefreshToken> fetchedRefreshtoken = refreshTokenRepository.findByToken("sadasdasd");
        assertThrows(NoSuchElementException.class, fetchedRefreshtoken::get);
    }

    @Test
    void giveRefreshIsStoredAndRefreshTokenToDeleteIsGivenThenShouldDeleteGivenToken() {
        userRepository.save(user);
        refreshTokenRepository.save(refreshtoken);

        refreshTokenRepository.deleteRefreshTokenByToken(refreshtoken.getToken());

        Optional<RefreshToken> fetchedRefreshtoken = refreshTokenRepository.findByToken(refreshtoken.getToken());
        assertThrows(NoSuchElementException.class, fetchedRefreshtoken::get);

    }

    @Test
    void givenUserIdThenShouldReturnRefreshtokenCorrespondingToThatUser() {
        userRepository.save(user);
        refreshTokenRepository.save(refreshtoken);

        RefreshToken fetchedRefreshtoken = refreshTokenRepository.findByUserId(user.getId()).get();
        assertEquals(refreshtoken.getToken(), fetchedRefreshtoken.getToken());
    }
}