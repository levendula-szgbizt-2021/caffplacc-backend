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
import hu.bme.szgbizt.levendula.caffplacc.user.UserDataUpdateRequest;
import hu.bme.szgbizt.levendula.caffplacc.user.UserResponse;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private CommentRepository commentRepository;

    @Mock
    private AnimationRepository animationRepository;

    @Mock
    private RefreshTokenRepository refreshTokenRepository;

    @Mock
    private PasswordEncoder bcryptEncoder;

    @Mock
    private UserResponseMapper mapper;

    @Mock
    private SecurityContext securityContext;

    @Mock
    private Authentication authentication;

    @InjectMocks
    @Autowired
    private UserService userService;

    @Test
    void getUserData() {
        UserResponse userResponse = new UserResponse("id", "username", "email");
        User mockUser = new User(UUID.randomUUID(), "test", "pass", "email", List.of(UserRole.ROLE_USER));
        UserDetails userDetails = new org.springframework.security.core.userdetails.User("test", "pass", List.of(new SimpleGrantedAuthority("ROLE_USER")));
        when(userRepository.findByUsername(anyString())).thenReturn(Optional.of(mockUser));
        when(userRepository.findById(Mockito.any(UUID.class))).thenReturn(Optional.of(mockUser));
        when(mapper.map(Mockito.any(User.class))).thenReturn(userResponse);
        when(authentication.getPrincipal()).thenReturn(userDetails);
        when(securityContext.getAuthentication()).thenReturn(authentication);
        securityContext.setAuthentication(authentication);
        SecurityContextHolder.setContext(securityContext);
        UserResponse response = userService.getUserData();
        verify(mapper, times(1)).map(any(User.class));
        verify(userRepository, times(1)).findById(any(UUID.class));
        verify(userRepository, times(1)).findByUsername(anyString());
    }

    @Test
    void changeUserDataOnlyUsernameThatExists() {
        var mockUserDto = new UserDataUpdateRequest("test1", null, null);
        User mockUser = new User(UUID.randomUUID(), "test", "pass", "email", List.of(UserRole.ROLE_USER));
        UserDetails userDetails = new org.springframework.security.core.userdetails.User("test", "pass", List.of(new SimpleGrantedAuthority("ROLE_USER")));
        when(userRepository.findByUsername(anyString())).thenReturn(Optional.of(mockUser));
        when(userRepository.findById(Mockito.any(UUID.class))).thenReturn(Optional.of(mockUser));
        when(authentication.getPrincipal()).thenReturn(userDetails);
        when(securityContext.getAuthentication()).thenReturn(authentication);
        securityContext.setAuthentication(authentication);
        SecurityContextHolder.setContext(securityContext);
        assertThrows(CaffplaccException.class, () -> {
            userService.changeUserData(mockUserDto);
        });
    }

    @Test
    void changeUserDataOnlyUsernameThatDoesNotExists() {
        UserResponse userResponse = new UserResponse("id", "username", "email");
        var mockUserDto = new UserDataUpdateRequest("test2", null, null);
        User mockUser = mock(User.class);
        when(mockUser.getId()).thenReturn(UUID.randomUUID());
        UserDetails userDetails = new org.springframework.security.core.userdetails.User("test", "pass", List.of(new SimpleGrantedAuthority("ROLE_USER")));
        when(userRepository.findByUsername("test")).thenReturn(Optional.of(mockUser));
        when(userRepository.findByUsername("test2")).thenReturn(Optional.empty());
        when(userRepository.findById(Mockito.any(UUID.class))).thenReturn(Optional.of(mockUser));
        when(userRepository.save(any(User.class))).thenReturn(new User());
        when(mapper.map(Mockito.any(User.class))).thenReturn(userResponse);
        when(authentication.getPrincipal()).thenReturn(userDetails);
        when(securityContext.getAuthentication()).thenReturn(authentication);
        securityContext.setAuthentication(authentication);
        SecurityContextHolder.setContext(securityContext);
        UserResponse response = userService.changeUserData(mockUserDto);
        verify(mockUser, times(1)).setUsername("test2");
        verify(mockUser, times(0)).setPassword(anyString());
        verify(mockUser, times(0)).setEmail(anyString());
    }

    @Test
    void changeUserDataOnlyPassword() {
        UserResponse userResponse = new UserResponse("id", "username", "email");
        var mockUserDto = new UserDataUpdateRequest(null, "pass2", null);
        User mockUser = mock(User.class);
        when(mockUser.getId()).thenReturn(UUID.randomUUID());
        UserDetails userDetails = new org.springframework.security.core.userdetails.User("test", "pass", List.of(new SimpleGrantedAuthority("ROLE_USER")));
        when(userRepository.findByUsername(anyString())).thenReturn(Optional.of(mockUser));
        when(userRepository.findById(Mockito.any(UUID.class))).thenReturn(Optional.of(mockUser));
        when(userRepository.save(any(User.class))).thenReturn(new User());
        when(mapper.map(Mockito.any(User.class))).thenReturn(userResponse);
        when(bcryptEncoder.encode(anyString())).thenReturn("SECRET");
        when(authentication.getPrincipal()).thenReturn(userDetails);
        when(securityContext.getAuthentication()).thenReturn(authentication);
        securityContext.setAuthentication(authentication);
        SecurityContextHolder.setContext(securityContext);
        UserResponse response = userService.changeUserData(mockUserDto);
        verify(mockUser, times(0)).setUsername(anyString());
        verify(mockUser, times(1)).setPassword("SECRET");
        verify(mockUser, times(0)).setEmail(anyString());
    }

    @Test
    void changeUserDataOnlyEmail() {
        UserResponse userResponse = new UserResponse("id", "username", "email");
        var mockUserDto = new UserDataUpdateRequest(null, null, "a@b.c");
        User mockUser = mock(User.class);
        when(mockUser.getId()).thenReturn(UUID.randomUUID());
        UserDetails userDetails = new org.springframework.security.core.userdetails.User("test", "pass", List.of(new SimpleGrantedAuthority("ROLE_USER")));
        when(userRepository.findByUsername(anyString())).thenReturn(Optional.of(mockUser));
        when(userRepository.findById(Mockito.any(UUID.class))).thenReturn(Optional.of(mockUser));
        when(userRepository.save(any(User.class))).thenReturn(new User());
        when(mapper.map(any(User.class))).thenReturn(userResponse);
        when(authentication.getPrincipal()).thenReturn(userDetails);
        when(securityContext.getAuthentication()).thenReturn(authentication);
        securityContext.setAuthentication(authentication);
        SecurityContextHolder.setContext(securityContext);
        UserResponse response = userService.changeUserData(mockUserDto);
        verify(mockUser, times(0)).setUsername(anyString());
        verify(mockUser, times(0)).setPassword(anyString());
        verify(mockUser, times(1)).setEmail("a@b.c");
    }

    @Test
    void deleteUserData() {
        UserResponse userResponse = new UserResponse("id", "username", "email");
        UserDto mockUserDto = new UserDto(null, null, "a@b.c");
        User mockUser = mock(User.class);
        UUID mockID = UUID.randomUUID();
        when(mockUser.getId()).thenReturn(mockID);
        UserDetails userDetails = new org.springframework.security.core.userdetails.User("test", "pass", List.of(new SimpleGrantedAuthority("ROLE_USER")));
        when(userRepository.findByUsername(anyString())).thenReturn(Optional.of(mockUser));
        when(authentication.getPrincipal()).thenReturn(userDetails);
        when(securityContext.getAuthentication()).thenReturn(authentication);
        securityContext.setAuthentication(authentication);
        SecurityContextHolder.setContext(securityContext);
        userService.deleteUserData();
        verify(animationRepository, times(1)).deleteAllByUserId(mockID);
        verify(userRepository, times(1)).deleteById(mockID);
    }
}