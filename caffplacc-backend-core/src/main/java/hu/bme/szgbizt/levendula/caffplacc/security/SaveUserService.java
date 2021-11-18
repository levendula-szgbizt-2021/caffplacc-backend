package hu.bme.szgbizt.levendula.caffplacc.security;

import hu.bme.szgbizt.levendula.caffplacc.login.UserDto;
import hu.bme.szgbizt.levendula.caffplacc.data.entity.User;
import hu.bme.szgbizt.levendula.caffplacc.data.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class SaveUserService {

    private final UserRepository userRepository;

    private final PasswordEncoder bcryptEncoder;

    public SaveUserService(UserRepository userRepository, PasswordEncoder bcryptEncoder) {
        this.userRepository = userRepository;
        this.bcryptEncoder = bcryptEncoder;
    }

    public String save(UserDto user) {
        User newUser = new User();
        newUser.setUsername(user.getUsername());
        newUser.setPassword(bcryptEncoder.encode(user.getPassword()));
        return userRepository.save(newUser).getId().toString();
    }
}