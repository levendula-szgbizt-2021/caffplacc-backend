package hu.bme.szgbizt.levendula.caffplacc.service;

import hu.bme.szgbizt.levendula.caffplacc.data.entity.User;
import hu.bme.szgbizt.levendula.caffplacc.data.repository.UserRepository;
import hu.bme.szgbizt.levendula.caffplacc.exception.CaffplaccException;
import hu.bme.szgbizt.levendula.caffplacc.login.UserDto;
import hu.bme.szgbizt.levendula.caffplacc.presentation.UserResponseMapper;
import hu.bme.szgbizt.levendula.caffplacc.user.UserResponse;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import javax.persistence.EntityNotFoundException;
import java.util.UUID;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder bcryptEncoder;
    private final UserResponseMapper mapper;

    public UserService(UserRepository userRepository, PasswordEncoder bcryptEncoder, UserResponseMapper mapper) {
        this.userRepository = userRepository;
        this.bcryptEncoder = bcryptEncoder;
        this.mapper = mapper;
    }

    public UserResponse changeUserData(UserDto request) {
        var user = findUserById(getUserToken());
        if (request.getUsername() != null) {
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
}
