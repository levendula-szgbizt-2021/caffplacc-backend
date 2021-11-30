package hu.bme.szgbizt.levendula.caffplacc.service;

import hu.bme.szgbizt.levendula.caffplacc.data.entity.User;
import hu.bme.szgbizt.levendula.caffplacc.data.entity.UserRole;
import hu.bme.szgbizt.levendula.caffplacc.data.repository.AnimationRepository;
import hu.bme.szgbizt.levendula.caffplacc.data.repository.CommentRepository;
import hu.bme.szgbizt.levendula.caffplacc.data.repository.RefreshTokenRepository;
import hu.bme.szgbizt.levendula.caffplacc.data.repository.UserRepository;
import hu.bme.szgbizt.levendula.caffplacc.exception.CaffplaccException;
import hu.bme.szgbizt.levendula.caffplacc.presentation.UserResponseMapper;
import hu.bme.szgbizt.levendula.caffplacc.user.AdminUserResponse;
import hu.bme.szgbizt.levendula.caffplacc.user.UserCreateUpdateRequest;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import javax.persistence.EntityNotFoundException;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@AllArgsConstructor
public class AdminUserService {

    private final UserRepository userRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final CommentRepository commentRepository;
    private final AnimationRepository animationRepository;
    private final UserResponseMapper mapper;
    private final PasswordEncoder bcryptEncoder;


    public Page<AdminUserResponse> listUsers(String username, Pageable pageable) {
        if (username == null) {
            return userRepository.findAll(pageable).map(mapper::mapToAdminUserResponse);
        } else {
            return userRepository.findAllByUsernameContains(username, pageable).map(mapper::mapToAdminUserResponse);
        }
    }

    public AdminUserResponse getOneUser(UUID id) {
        return mapper.mapToAdminUserResponse(findUserById(id));
    }

    public AdminUserResponse createUser(UserCreateUpdateRequest request) {
        var newUser = new User();
        log.info("Creating new user from admin account.");

        newUser.setUsername(request.getUsername());
        newUser.setPassword(bcryptEncoder.encode(request.getPassword()));
        newUser.setEmail(request.getEmail());
        if (request.isAdmin()) {
            newUser.setRoles(List.of(UserRole.ROLE_USER, UserRole.ROLE_ADMIN));
        } else {
            newUser.setRoles(List.of(UserRole.ROLE_USER));
        }

        log.info("Created new user from admin account: {}, {}, admin: {}", request.getUsername(), request.getEmail(), request.isAdmin());
        return mapper.mapToAdminUserResponse(userRepository.save(newUser));
    }

    public AdminUserResponse updateUser(UUID id, UserCreateUpdateRequest request) {
        var user = findUserById(id);
        log.info("Updating user from admin account with userId: {}", user.getId());

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
        userRepository.save(user);

        log.info("Updated user from admin account with userId: {}", user.getId());
        return mapper.mapToAdminUserResponse(user);
    }

    public void deleteUser(UUID id) {
        log.info("Deleting user from admin account with userId: {}", id);

        var token = refreshTokenRepository.findByUserId(id);
        token.ifPresent(refreshTokenRepository::delete);
        commentRepository.deleteAllByUserId(id);
        animationRepository.deleteAllByUserId(id); // todo delete comments on all animations
        userRepository.deleteById(id);

        log.info("Deleted user from admin account with userId: {}", id);
    }

    private User findUserById(UUID id) {
        return userRepository.findById(id).orElseThrow(() -> new EntityNotFoundException(User.class.getName()));
    }
}
