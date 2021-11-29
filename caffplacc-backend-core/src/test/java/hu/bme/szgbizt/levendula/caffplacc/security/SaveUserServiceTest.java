package hu.bme.szgbizt.levendula.caffplacc.security;

import hu.bme.szgbizt.levendula.caffplacc.data.entity.User;
import hu.bme.szgbizt.levendula.caffplacc.data.entity.UserRole;
import hu.bme.szgbizt.levendula.caffplacc.data.repository.UserRepository;
import hu.bme.szgbizt.levendula.caffplacc.login.UserDto;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SaveUserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder bcryptEncoder;

    @InjectMocks
    private SaveUserService saveUserService;

    @Test
    void loadUserFromUsernameThatExists() {
        String username = "Test";
        when(userRepository.findByUsername(username)).thenReturn(Optional.of(new User()));
        User response = saveUserService.loadUserFromUsername(username);
        verify(userRepository, times(1)).findByUsername(username);
    }

    @Test
    void loadUserFromUsernameThatDoesNotTExists() {
        String username = "Test";
        when(userRepository.findByUsername(username)).thenReturn(Optional.empty());
        assertThrows(UsernameNotFoundException.class, () -> saveUserService.loadUserFromUsername(username));
    }

    @Test
    void save() {
        UserDto userDto = new UserDto("Test", "pass", "a@b.c");
        User mockuser = mock(User.class);
        UUID mockID = UUID.randomUUID();
        when(mockuser.getId()).thenReturn(mockID);
        when(bcryptEncoder.encode("pass")).thenReturn("SECRET");
        when(userRepository.save(any(User.class))).thenReturn(mockuser);
        ArgumentCaptor<User> argument = ArgumentCaptor.forClass(User.class);
        String id = saveUserService.save(userDto);
        verify(bcryptEncoder, times(1)).encode("pass");
        verify(userRepository, times(1)).save(argument.capture());
        assertEquals(mockID.toString(), id.toString());
        assertEquals(userDto.getUsername(), argument.getValue().getUsername());
        assertEquals("SECRET", argument.getValue().getPassword());
        assertEquals(userDto.getEmail(), argument.getValue().getEmail());
        assertArrayEquals(List.of(UserRole.ROLE_USER).toArray(), argument.getValue().getRoles().toArray());
    }
}