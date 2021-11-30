package hu.bme.szgbizt.levendula.caffplacc.data;

import hu.bme.szgbizt.levendula.caffplacc.data.entity.User;
import hu.bme.szgbizt.levendula.caffplacc.data.repository.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

import static hu.bme.szgbizt.levendula.caffplacc.data.entity.UserRole.ROLE_ADMIN;
import static hu.bme.szgbizt.levendula.caffplacc.data.entity.UserRole.ROLE_USER;

@Slf4j
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
        if (userRepository.findAllByRolesContaining(ROLE_ADMIN).isEmpty()) {
            log.info("No administrators found. Creating admin user.");
            userRepository.save(new User(UUID.randomUUID(), "Admin", passwordEncoder.encode("Admin123"), "admin@caffplacc.hu", List.of(ROLE_USER, ROLE_ADMIN)));
        }
    }
}
