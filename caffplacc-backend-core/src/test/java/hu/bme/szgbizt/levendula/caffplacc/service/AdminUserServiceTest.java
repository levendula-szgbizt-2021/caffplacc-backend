package hu.bme.szgbizt.levendula.caffplacc.service;

import hu.bme.szgbizt.levendula.caffplacc.data.entity.User;
import hu.bme.szgbizt.levendula.caffplacc.data.entity.UserRole;
import hu.bme.szgbizt.levendula.caffplacc.data.repository.AnimationRepository;
import hu.bme.szgbizt.levendula.caffplacc.data.repository.CommentRepository;
import hu.bme.szgbizt.levendula.caffplacc.data.repository.UserRepository;
import hu.bme.szgbizt.levendula.caffplacc.exception.CaffplaccException;
import hu.bme.szgbizt.levendula.caffplacc.presentation.UserResponseMapper;
import hu.bme.szgbizt.levendula.caffplacc.user.UserCreateUpdateRequest;
import hu.bme.szgbizt.levendula.caffplacc.user.UserResponse;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AdminUserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private CommentRepository commentRepository;

    @Mock
    private AnimationRepository animationRepository;

    @Mock
    private PasswordEncoder bcryptEncoder;

    @Mock
    private UserResponseMapper mapper;

    @InjectMocks
    @Autowired
    private AdminUserService adminUserService;

    @Test
    void listUsersWithoutUsername() {
        UserResponse userResponse = new UserResponse("id", "username", "email");
        Page<User> page = Page.empty();
        Pageable pageable = PageRequest.of(0, 5);
        when(userRepository.findAll(pageable)).thenReturn(page);
        Page<UserResponse> response = adminUserService.listUsers(null, pageable);
        verify(userRepository, times(1)).findAll(pageable);
        verify(userRepository, times(0)).findAllByUsernameContains(anyString(), any());
    }

    @Test
    void listUsersWithUsername() {
        Page<User> page = Page.empty();
        Pageable pageable = PageRequest.of(0, 5);
        when(userRepository.findAllByUsernameContains("test", pageable)).thenReturn(page);
        Page<UserResponse> response = adminUserService.listUsers("test", pageable);
        verify(userRepository, times(0)).findAll(pageable);
        verify(userRepository, times(1)).findAllByUsernameContains(anyString(), any());
    }

    @Test
    void getOneUser() {
        UserResponse userResponse = new UserResponse("id", "username", "email");
        User mockUser = mock(User.class);
        when(userRepository.findById(any(UUID.class))).thenReturn(Optional.of(mockUser));
        when(mapper.map(mockUser)).thenReturn(userResponse);
        UserResponse response = adminUserService.getOneUser(UUID.randomUUID());
        verify(userRepository, times(1)).findById(any(UUID.class));
    }

    @Test
    void createUserIfNotAdmin() {
        UserResponse userResponse = new UserResponse("id", "username", "email");
        UserCreateUpdateRequest request = new UserCreateUpdateRequest("john", "pass", "a@b.c", false);
        when(bcryptEncoder.encode(anyString())).thenReturn("SECRET");
        when(userRepository.save(any(User.class))).thenReturn(new User());
        when(mapper.map(any(User.class))).thenReturn(userResponse);
        ArgumentCaptor<User> argument = ArgumentCaptor.forClass(User.class);
        UserResponse response = adminUserService.createUser(request);
        verify(bcryptEncoder, times(1)).encode("pass");
        verify(userRepository, times(1)).save(argument.capture());
        assertEquals("john", argument.getValue().getUsername());
        assertEquals("SECRET", argument.getValue().getPassword());
        assertEquals("a@b.c", argument.getValue().getEmail());
        assertArrayEquals(List.of(UserRole.ROLE_USER).toArray(), argument.getValue().getRoles().toArray());
    }

    @Test
    void createUserIfAdmin() {
        UserResponse userResponse = new UserResponse("id", "username", "email");
        UserCreateUpdateRequest request = new UserCreateUpdateRequest("john", "pass", "a@b.c", true);
        when(bcryptEncoder.encode(anyString())).thenReturn("SECRET");
        when(userRepository.save(any(User.class))).thenReturn(new User());
        when(mapper.map(any(User.class))).thenReturn(userResponse);
        ArgumentCaptor<User> argument = ArgumentCaptor.forClass(User.class);
        UserResponse response = adminUserService.createUser(request);
        verify(bcryptEncoder, times(1)).encode("pass");
        verify(userRepository, times(1)).save(argument.capture());
        assertEquals("john", argument.getValue().getUsername());
        assertEquals("SECRET", argument.getValue().getPassword());
        assertEquals("a@b.c", argument.getValue().getEmail());
        assertArrayEquals(List.of(UserRole.ROLE_USER, UserRole.ROLE_ADMIN).toArray(), argument.getValue().getRoles().toArray());
    }

    @Test
    void updateUserOnlyUsername() {
        UUID mockID = UUID.randomUUID();
        UserCreateUpdateRequest request = new UserCreateUpdateRequest("test", null, null, false);
        User mockUser = mock(User.class);
        when(userRepository.findById(mockID)).thenReturn(Optional.of(mockUser));
        when(userRepository.findByUsername("test")).thenReturn(Optional.empty());
        when(userRepository.save(mockUser)).thenReturn(mockUser);
        when(mapper.map(mockUser)).thenReturn(new UserResponse());
        UserResponse response = adminUserService.updateUser(mockID, request);
        verify(mockUser, times(1)).setUsername("test");
        verify(mockUser, times(1)).setRoles(List.of(UserRole.ROLE_USER));
        verify(mockUser, times(0)).setPassword(anyString());
        verify(mockUser, times(0)).setEmail(anyString());
    }

    @Test
    void updateUserOnlyUsernameThatExists() {
        UUID mockID = UUID.randomUUID();
        UserCreateUpdateRequest request = new UserCreateUpdateRequest("test", null, null, false);
        User mockUser = mock(User.class);
        when(userRepository.findById(mockID)).thenReturn(Optional.of(mockUser));
        when(userRepository.findByUsername("test")).thenReturn(Optional.of(new User()));
        assertThrows(CaffplaccException.class, () -> {adminUserService.updateUser(mockID, request);});
    }

    @Test
    void updateUserOnlyPassword() {
        UUID mockID = UUID.randomUUID();
        UserCreateUpdateRequest request = new UserCreateUpdateRequest(null, "pass", null, false);
        User mockUser = mock(User.class);
        when(userRepository.findById(mockID)).thenReturn(Optional.of(mockUser));
        when(bcryptEncoder.encode("pass")).thenReturn("SECRET");
        when(userRepository.save(mockUser)).thenReturn(mockUser);
        when(mapper.map(mockUser)).thenReturn(new UserResponse());
        UserResponse response = adminUserService.updateUser(mockID, request);
        verify(mockUser, times(0)).setUsername(anyString());
        verify(mockUser, times(1)).setRoles(List.of(UserRole.ROLE_USER));
        verify(mockUser, times(1)).setPassword("SECRET");
        verify(mockUser, times(0)).setEmail(anyString());
    }

    @Test
    void updateUserOnlyEmail() {
        UUID mockID = UUID.randomUUID();
        UserCreateUpdateRequest request = new UserCreateUpdateRequest(null, null, "a@b.c", false);
        User mockUser = mock(User.class);
        when(userRepository.findById(mockID)).thenReturn(Optional.of(mockUser));
        when(userRepository.save(mockUser)).thenReturn(mockUser);
        when(mapper.map(any(User.class))).thenReturn(new UserResponse());
        UserResponse response = adminUserService.updateUser(mockID, request);
        verify(mockUser, times(0)).setUsername(anyString());
        verify(mockUser, times(1)).setRoles(List.of(UserRole.ROLE_USER));
        verify(mockUser, times(0)).setPassword(anyString());
        verify(mockUser, times(1)).setEmail("a@b.c");
    }

    @Test
    void updateUserOnlyEmailAndIsAdmin() {
        UUID mockID = UUID.randomUUID();
        UserCreateUpdateRequest request = new UserCreateUpdateRequest(null, null, "a@b.c", true);
        User mockUser = mock(User.class);
        when(userRepository.findById(mockID)).thenReturn(Optional.of(mockUser));
        when(userRepository.save(mockUser)).thenReturn(mockUser);
        when(mapper.map(mockUser)).thenReturn(new UserResponse());
        UserResponse response = adminUserService.updateUser(mockID, request);
        verify(mockUser, times(0)).setUsername(anyString());
        verify(mockUser, times(1)).setRoles(List.of(UserRole.ROLE_USER, UserRole.ROLE_ADMIN));
        verify(mockUser, times(0)).setPassword(anyString());
        verify(mockUser, times(1)).setEmail("a@b.c");
    }

    @Test
    void deleteUser() {
        UUID mockID = UUID.randomUUID();
        adminUserService.deleteUser(mockID);
        verify(commentRepository, times(1)).deleteAllByUserId(mockID);
        verify(animationRepository, times(1)).deleteAllByUserId(mockID);
        verify(userRepository, times(1)).deleteById(mockID);
    }
}