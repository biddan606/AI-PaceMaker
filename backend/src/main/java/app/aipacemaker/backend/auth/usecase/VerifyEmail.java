package app.aipacemaker.backend.auth.usecase;

import app.aipacemaker.backend.auth.model.EmailVerificationToken;
import app.aipacemaker.backend.auth.model.EmailVerificationTokenRepository;
import app.aipacemaker.backend.auth.model.User;
import app.aipacemaker.backend.auth.model.UserRepository;
import app.aipacemaker.backend.auth.model.exception.ExpiredVerificationTokenException;
import app.aipacemaker.backend.auth.model.exception.InvalidVerificationTokenException;
import app.aipacemaker.backend.auth.model.exception.UserNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class VerifyEmail {

    private final UserRepository userRepository;
    private final EmailVerificationTokenRepository tokenRepository;

    @Transactional
    public Result execute(Command command) {
        // 1. 토큰 조회
        EmailVerificationToken token = tokenRepository.findByToken(command.token())
                .orElseThrow(() -> new InvalidVerificationTokenException("유효하지 않은 인증 토큰입니다."));

        // 2. 토큰 만료 검증
        if (token.isExpired()) {
            throw new ExpiredVerificationTokenException("만료된 인증 토큰입니다.");
        }

        // 3. 사용자 조회
        User user = userRepository.findById(token.getUserId())
                .orElseThrow(() -> new UserNotFoundException("사용자를 찾을 수 없습니다."));

        // 4. 사용자 이메일 인증
        user.verifyEmail();
        userRepository.save(user);

        // 5. 토큰 삭제
        tokenRepository.deleteByUserId(user.getId());

        return new Result(user.getId(), user.getEmail(), true);
    }

    public record Command(String token) {}

    public record Result(Long userId, String email, boolean verified) {}
}
