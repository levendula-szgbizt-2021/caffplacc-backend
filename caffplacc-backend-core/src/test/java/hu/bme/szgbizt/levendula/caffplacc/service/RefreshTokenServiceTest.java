package hu.bme.szgbizt.levendula.caffplacc.service;

import hu.bme.szgbizt.levendula.caffplacc.data.entity.RefreshToken;
import hu.bme.szgbizt.levendula.caffplacc.data.entity.User;
import hu.bme.szgbizt.levendula.caffplacc.data.repository.RefreshTokenRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.Instant;
import java.util.Date;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RefreshTokenServiceTest {

    @Mock
    private RefreshTokenRepository refreshTokenRepository;

    @InjectMocks
    @Autowired
    private RefreshTokenService refreshTokenService;

    @Test
    void findByToken() {
        String token = "dsdfsdfsdfsdfvbcvbcvb";
        RefreshToken mockRefreshToken = mock(RefreshToken.class);
        when(refreshTokenRepository.findByToken(token)).thenReturn(Optional.of(mockRefreshToken));
        Optional<RefreshToken> response = refreshTokenService.findByToken(token);
        verify(refreshTokenRepository, times(1)).findByToken(token);
    }

    @Test
    void saveRefreshTokenWithNoStoredRefreshtoken() {
        UUID mockID = UUID.randomUUID();
        User mocKUser = new User();
        mocKUser.setId(mockID);
        String mockToken = "dsdfsdfsdfsdfvbcvbcvb";
        Date mockExpDate = Date.from(Instant.now());
        when(refreshTokenRepository.findByUserId(mockID)).thenReturn(Optional.empty());
        when(refreshTokenRepository.save(any(RefreshToken.class))).thenReturn(new RefreshToken());
        ArgumentCaptor<RefreshToken> argument = ArgumentCaptor.forClass(RefreshToken.class);
        UUID response = refreshTokenService.saveRefreshToken(mocKUser, mockToken, mockExpDate);
        verify(refreshTokenRepository, times(1)).findByUserId(mockID);
        verify(refreshTokenRepository, times(1)).save(argument.capture());
        assertEquals(mockID, argument.getValue().getUser().getId());
        assertEquals(mockToken, argument.getValue().getToken());
        assertEquals(mockExpDate.toInstant(), argument.getValue().getExpiryDate());
    }

    @Test
    void deleteById() {
        UUID mockID = UUID.randomUUID();
        refreshTokenService.deleteById(mockID);
        verify(refreshTokenRepository, times(1)).deleteById(mockID);
    }

    @Test
    void deleteByToken() {
        String mockToken = "dfsdfsdkdjflskdfjpoefjpoe";
        refreshTokenService.deleteByToken(mockToken);
        verify(refreshTokenRepository, times(1)).deleteRefreshTokenByToken(mockToken);
    }
}