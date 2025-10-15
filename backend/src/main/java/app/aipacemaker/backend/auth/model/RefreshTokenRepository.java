package app.aipacemaker.backend.auth.model;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {
    Optional<RefreshToken> findByToken(String token);
    Optional<RefreshToken> findByUserIdAndDeviceId(Long userId, String deviceId);
    void deleteByToken(String token);
    void deleteByUserIdAndDeviceId(Long userId, String deviceId);
}
