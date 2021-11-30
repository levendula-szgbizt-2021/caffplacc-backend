package hu.bme.szgbizt.levendula.caffplacc.service;

import hu.bme.szgbizt.levendula.caffplacc.data.entity.User;
import hu.bme.szgbizt.levendula.caffplacc.data.repository.AnimationRepository;
import hu.bme.szgbizt.levendula.caffplacc.data.repository.CommentRepository;
import hu.bme.szgbizt.levendula.caffplacc.data.repository.RefreshTokenRepository;
import hu.bme.szgbizt.levendula.caffplacc.data.repository.UserRepository;
import hu.bme.szgbizt.levendula.caffplacc.exception.CaffplaccException;
import hu.bme.szgbizt.levendula.caffplacc.login.UserDto;
import hu.bme.szgbizt.levendula.caffplacc.presentation.UserResponseMapper;
import hu.bme.szgbizt.levendula.caffplacc.user.UserResponse;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import javax.persistence.EntityNotFoundException;
import java.util.UUID;

@Slf4j
@Service
@AllArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final CommentRepository commentRepository;
    private final AnimationRepository animationRepository;
    private final PasswordEncoder bcryptEncoder;
    private final UserResponseMapper mapper;

    public UserResponse getUserData() {
        return mapper.map(findUserById(getUserToken()));
    }

    public UserResponse changeUserData(UserDto request) {
        var user = findUserById(getUserToken());
        log.info("Updating user data for userId: {}", user.getId());

        if (request.getUsername() != null && !request.getUsername().equals(user.getUsername())) {
            if (userRepository.findByUsername(request.getUsername()).isPresent()) {
                throw new CaffplaccException("That username is taken!");
            } else {
                user.setUsername(request.getUsername());
            }
        }
        if (request.getPassword() != null) {
            user.setPassword(bcryptEncoder.encode(request.getPassword()));
        }
        if (request.getEmail() != null) {
            user.setEmail(request.getEmail());
        }

        log.info("Updated user data for userId: {}", user.getId());
        return mapper.map(userRepository.save(user));
    }

    private User findUserById(UUID id) {
        return userRepository.findById(id).orElseThrow(() -> new EntityNotFoundException(User.class.getName()));
    }

    private UUID getUserToken() {
        UserDetails userDetails = (UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        var userName = userDetails.getUsername();
        return userRepository.findByUsername(userName).orElseThrow(() -> new EntityNotFoundException(User.class.getName())).getId();
    }

    public void deleteUserData() {
        var userId = getUserToken();
        log.info("Deleting user data for userId: {}", userId);

        var token = refreshTokenRepository.findByUserId(userId);
        token.ifPresent(refreshTokenRepository::delete);
        commentRepository.deleteAllByUserId(userId);
        animationRepository.deleteAllByUserId(userId); // todo delete comments on all animations
        userRepository.deleteById(userId);

        log.info("Deleted user data for userId: {}", userId);
    }
}
