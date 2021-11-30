package hu.bme.szgbizt.levendula.caffplacc.data;

import hu.bme.szgbizt.levendula.caffplacc.data.entity.User;
import hu.bme.szgbizt.levendula.caffplacc.data.repository.UserRepository;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

import static hu.bme.szgbizt.levendula.caffplacc.data.entity.UserRole.ROLE_ADMIN;
import static hu.bme.szgbizt.levendula.caffplacc.data.entity.UserRole.ROLE_USER;

@Component
public class InitialData implements ApplicationRunner {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public InitialData(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(ApplicationArguments args) throws Exception {
        if (userRepository.count() == 0) {
            userRepository.save(new User(UUID.randomUUID(), "Admin", passwordEncoder.encode("Admin123"), "admin@caffplacc.hu", List.of(ROLE_USER, ROLE_ADMIN)));
        }
    }
}
