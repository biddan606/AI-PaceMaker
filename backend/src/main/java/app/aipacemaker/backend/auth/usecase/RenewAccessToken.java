package app.aipacemaker.backend.auth.usecase;

import app.aipacemaker.backend.auth.infrastructure.JwtTokenProvider;
import app.aipacemaker.backend.auth.model.RefreshToken;
import app.aipacemaker.backend.auth.model.RefreshTokenRepository;
import app.aipacemaker.backend.auth.model.User;
import app.aipacemaker.backend.auth.model.UserRepository;
import app.aipacemaker.backend.auth.model.exception.ExpiredRefreshTokenException;
import app.aipacemaker.backend.auth.model.exception.InvalidRefreshTokenException;
import app.aipacemaker.backend.auth.model.exception.UserNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class RenewAccessToken {

    private final RefreshTokenRepository refreshTokenRepository;
    private final UserRepository userRepository;
    private final JwtTokenProvider jwtTokenProvider;

    @Transactional(readOnly = true)
    public Result execute(Command command) {
        // 1. Refresh Token 형식 검증
        if (!jwtTokenProvider.validateToken(command.refreshToken)) {
            throw new InvalidRefreshTokenException("유효하지 않은 Refresh Token입니다.");
        }

        // 2. Refresh Token에서 userId와 deviceId 추출
        Long userId = jwtTokenProvider.extractUserId(command.refreshToken);
        String deviceId = jwtTokenProvider.extractDeviceId(command.refreshToken);

        // 3. DB에서 Refresh Token 조회
        RefreshToken storedToken = refreshTokenRepository.findByUserIdAndDeviceId(userId, deviceId)
                .orElseThrow(() -> new InvalidRefreshTokenException("유효하지 않은 Refresh Token입니다."));

        // 4. 토큰 일치 여부 확인
        if (!storedToken.getToken().equals(command.refreshToken)) {
            throw new InvalidRefreshTokenException("유효하지 않은 Refresh Token입니다.");
        }

        // 5. 만료 여부 확인
        if (storedToken.isExpired()) {
            throw new ExpiredRefreshTokenException("만료된 Refresh Token입니다.");
        }

        // 6. 사용자 조회
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("사용자를 찾을 수 없습니다."));

        // 7. 새로운 Access Token 발급
        String newAccessToken = jwtTokenProvider.generateAccessToken(user.getId(), user.getEmail());

        return new Result(newAccessToken);
    }

    public record Command(String refreshToken) {}

    public record Result(String accessToken) {}
}
