package hu.bme.szgbizt.levendula.caffplacc.service;

import hu.bme.szgbizt.levendula.caffplacc.data.entity.User;
import hu.bme.szgbizt.levendula.caffplacc.data.entity.UserRole;
import hu.bme.szgbizt.levendula.caffplacc.data.repository.AnimationRepository;
import hu.bme.szgbizt.levendula.caffplacc.data.repository.CommentRepository;
import hu.bme.szgbizt.levendula.caffplacc.data.repository.RefreshTokenRepository;
import hu.bme.szgbizt.levendula.caffplacc.data.repository.UserRepository;
import hu.bme.szgbizt.levendula.caffplacc.exception.CaffplaccException;
import hu.bme.szgbizt.levendula.caffplacc.login.UserDto;
import hu.bme.szgbizt.levendula.caffplacc.presentation.UserResponseMapper;
import hu.bme.szgbizt.levendula.caffplacc.user.AdminUserResponse;
import hu.bme.szgbizt.levendula.caffplacc.user.UserCreateUpdateRequest;
import hu.bme.szgbizt.levendula.caffplacc.user.UserResponse;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import javax.persistence.EntityNotFoundException;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@AllArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final CommentRepository commentRepository;
    private final AnimationRepository animationRepository;
    private final UserResponseMapper mapper;
    private final PasswordEncoder bcryptEncoder;

    public UserResponse getUserData() {
        return mapper.map(findUserById(getUserToken()));
    }

    public AdminUserResponse getOneUser(UUID id) {
        return mapper.mapToAdminUserResponse(findUserById(id));
    }

    public Page<AdminUserResponse> listUsers(String username, Pageable pageable) {
        if (username == null) {
            return userRepository.findAll(pageable).map(mapper::mapToAdminUserResponse);
        } else {
            return userRepository.findAllByUsernameContains(username, pageable).map(mapper::mapToAdminUserResponse);
        }
    }

    public AdminUserResponse createUserByAdmin(UserCreateUpdateRequest request) {
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

    public UserResponse changeUserData(UserDto request) {
        var userId = getUserToken();
        log.info("Updating user data for userId: {}", userId);
        var user = updateUser(userId, new UserCreateUpdateRequest(request.getUsername(), request.getPassword(), request.getEmail(), false));
        log.info("Updated user data for userId: {}", userId);
        return mapper.map(user);
    }

    public AdminUserResponse updateUserByAdmin(UUID id, UserCreateUpdateRequest request) {
        log.info("Updating user from admin account with userId: {}", id);
        var user = updateUser(id, request);
        log.info("Updated user from admin account with userId: {}", id);
        return mapper.mapToAdminUserResponse(user);
    }

    private User updateUser(UUID id, UserCreateUpdateRequest request) {
        var user = findUserById(id);
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
        return userRepository.save(user);
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
        deleteUser(userId);
        log.info("Deleted user data for userId: {}", userId);
    }

    public void deleteUserByAdmin(UUID userId) {
        log.info("Deleting user from admin account with userId: {}", userId);
        deleteUser(userId);
        log.info("Deleted user from admin account with userId: {}", userId);
    }

    private void deleteUser(UUID userId) {
        var token = refreshTokenRepository.findByUserId(userId);
        token.ifPresent(refreshTokenRepository::delete);
        commentRepository.deleteAllByUserId(userId);
        animationRepository.deleteAllByUserId(userId); // todo delete comments on all animations
        userRepository.deleteById(userId);
    }
}
