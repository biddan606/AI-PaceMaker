package app.aipacemaker.backend.auth.usecase;

import app.aipacemaker.backend.auth.infrastructure.JwtTokenProvider;
import app.aipacemaker.backend.auth.model.RefreshToken;
import app.aipacemaker.backend.auth.model.RefreshTokenRepository;
import app.aipacemaker.backend.auth.model.User;
import app.aipacemaker.backend.auth.model.UserRepository;
import app.aipacemaker.backend.auth.model.exception.InvalidCredentialsException;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class LoginUser {

    private final UserRepository userRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final JwtTokenProvider jwtTokenProvider;
    private final BCryptPasswordEncoder passwordEncoder;

    @Value("${jwt.refresh-token-expiry}")
    private long refreshTokenExpiry;

    @Transactional
    public Result execute(Command command) {
        // 1. 사용자 존재 여부 확인
        User user = userRepository.findByEmail(command.email)
                .orElseThrow(() -> new InvalidCredentialsException("이메일 또는 비밀번호가 올바르지 않습니다."));

        // 2. 비밀번호 검증
        if (!passwordEncoder.matches(command.password, user.getPassword())) {
            throw new InvalidCredentialsException("이메일 또는 비밀번호가 올바르지 않습니다.");
        }

        // 3. Access Token 생성
        String accessToken = jwtTokenProvider.generateAccessToken(user.getId(), user.getEmail());

        // 4. Refresh Token 생성 (deviceId 포함)
        String refreshToken = jwtTokenProvider.generateRefreshToken(user.getId(), command.deviceId);

        // 5. 기존 Refresh Token 조회 및 갱신 또는 신규 생성
        refreshTokenRepository.findByUserIdAndDeviceId(user.getId(), command.deviceId)
                .ifPresentOrElse(
                        existingToken -> {
                            // 기존 토큰 업데이트
                            existingToken.updateToken(refreshToken, LocalDateTime.now().plusSeconds(refreshTokenExpiry / 1000));
                        },
                        () -> {
                            // 신규 토큰 생성
                            RefreshToken newRefreshToken = RefreshToken.builder()
                                    .userId(user.getId())
                                    .deviceId(command.deviceId)
                                    .token(refreshToken)
                                    .expiresAt(LocalDateTime.now().plusSeconds(refreshTokenExpiry / 1000))
                                    .build();
                            refreshTokenRepository.save(newRefreshToken);
                        }
                );

        return new Result(
                user.getId(),
                user.getEmail(),
                user.isEmailVerified(),
                accessToken,
                refreshToken
        );
    }

    public record Command(String email, String password, String deviceId) {}

    public record Result(
            Long userId,
            String email,
            boolean emailVerified,
            String accessToken,
            String refreshToken
    ) {}
}
