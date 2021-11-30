package hu.bme.szgbizt.levendula.caffplacc.security;

import hu.bme.szgbizt.levendula.caffplacc.data.entity.User;
import hu.bme.szgbizt.levendula.caffplacc.data.entity.UserRole;
import hu.bme.szgbizt.levendula.caffplacc.data.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class JwtUserDetailsServiceTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private JwtUserDetailsService jwtUserDetailsService;

    @Test
    void loadUserByUsernameThatExists() {
        String username = "Test";
        User user = new User();
        user.setUsername("Test");
        user.setPassword("pass");
        user.setRoles(List.of(UserRole.ROLE_USER));
        List<SimpleGrantedAuthority> authorities = List.of(new SimpleGrantedAuthority(UserRole.ROLE_USER.toString()));
        when(userRepository.findByUsername(username)).thenReturn(Optional.of(user));
        UserDetails userDetails = jwtUserDetailsService.loadUserByUsername(username);
        assertEquals(user.getUsername(), userDetails.getUsername());
        assertEquals(user.getPassword(), userDetails.getPassword());
        assertArrayEquals(authorities.toArray(), userDetails.getAuthorities().toArray());
    }

    @Test
    void loadUserByUsernameThatDoestNotExists() {
        String username = "Test";
        when(userRepository.findByUsername(username)).thenReturn(Optional.empty());
        assertThrows(UsernameNotFoundException.class, () -> jwtUserDetailsService.loadUserByUsername(username));
    }
}