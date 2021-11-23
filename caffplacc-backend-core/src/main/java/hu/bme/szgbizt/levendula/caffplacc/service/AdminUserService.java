package hu.bme.szgbizt.levendula.caffplacc.service;

import hu.bme.szgbizt.levendula.caffplacc.data.entity.User;
import hu.bme.szgbizt.levendula.caffplacc.data.entity.UserRole;
import hu.bme.szgbizt.levendula.caffplacc.data.repository.AnimationRepository;
import hu.bme.szgbizt.levendula.caffplacc.data.repository.CommentRepository;
import hu.bme.szgbizt.levendula.caffplacc.data.repository.UserRepository;
import hu.bme.szgbizt.levendula.caffplacc.presentation.UserResponseMapper;
import hu.bme.szgbizt.levendula.caffplacc.user.UserCreateRequest;
import hu.bme.szgbizt.levendula.caffplacc.user.UserResponse;
import hu.bme.szgbizt.levendula.caffplacc.user.UserUpdateRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import javax.persistence.EntityNotFoundException;
import java.util.List;
import java.util.UUID;

@Service
public class AdminUserService {

    private final UserRepository userRepository;
    private final CommentRepository commentRepository;
    private final AnimationRepository animationRepository;
    private final UserResponseMapper mapper;
    private final PasswordEncoder bcryptEncoder;

    public AdminUserService(UserRepository userRepository, CommentRepository commentRepository, AnimationRepository animationRepository, UserResponseMapper mapper, PasswordEncoder bcryptEncoder) {
        this.userRepository = userRepository;
        this.commentRepository = commentRepository;
        this.animationRepository = animationRepository;
        this.mapper = mapper;
        this.bcryptEncoder = bcryptEncoder;
    }

    public Page<UserResponse> listUsers(String username, Pageable pageable) {
        return userRepository.findAllByUsernameContains(username, pageable).map(mapper::map);
    }

    public UserResponse getOneUser(UUID id) {
        return mapper.map(findUserById(id));
    }

    public UserResponse createUser(UserCreateRequest request) {
        var newUser = new User();
        newUser.setUsername(request.getUsername());
        newUser.setPassword(bcryptEncoder.encode(request.getPassword()));
        if (request.isAdmin()) {
            newUser.setRoles(List.of(UserRole.ROLE_USER, UserRole.ROLE_ADMIN));
        } else {
            newUser.setRoles(List.of(UserRole.ROLE_USER));
        }
        return mapper.map(userRepository.save(newUser));
    }

    public UserResponse updateUser(UUID id, UserUpdateRequest request) {
        return null;
    }

    public void deleteUser(UUID id) {
        commentRepository.deleteAllByUserId(id);
        animationRepository.deleteAllByUserId(id);
        userRepository.deleteById(id);
    }

    private User findUserById(UUID id) {
        return userRepository.findById(id).orElseThrow(() -> new EntityNotFoundException(User.class.getName()));
    }
}
