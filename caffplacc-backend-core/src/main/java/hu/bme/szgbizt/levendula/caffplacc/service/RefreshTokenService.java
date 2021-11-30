package hu.bme.szgbizt.levendula.caffplacc.service;

import hu.bme.szgbizt.levendula.caffplacc.data.entity.RefreshToken;
import hu.bme.szgbizt.levendula.caffplacc.data.entity.User;
import hu.bme.szgbizt.levendula.caffplacc.data.repository.RefreshTokenRepository;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.util.Date;
import java.util.Optional;
import java.util.UUID;

@Service
@Transactional
public class RefreshTokenService {

    private final RefreshTokenRepository refreshTokenRepository;

    public RefreshTokenService(RefreshTokenRepository refreshTokenRepository) {
        this.refreshTokenRepository = refreshTokenRepository;
    }

    public Optional<RefreshToken> findByToken(String token) {
        return refreshTokenRepository.findByToken(token);
    }

    public UUID saveRefreshToken(User user, String refreshToken, Date expDate) {
        RefreshToken token = refreshTokenRepository.findByUserId(user.getId()).orElse(new RefreshToken());
        token.setUser(user);
        token.setToken(refreshToken);
        token.setExpiryDate(expDate.toInstant());
        token = refreshTokenRepository.save(token);
        return token.getId();
    }

    public void deleteById(UUID id) {
        refreshTokenRepository.deleteById(id);
    }

    public void deleteByToken(String token) {
        refreshTokenRepository.deleteRefreshTokenByToken(token);
    }
}
