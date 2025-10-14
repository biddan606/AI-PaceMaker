package app.aipacemaker.backend.auth.usecase;

import app.aipacemaker.backend.auth.event.UserRegisteredEvent;
import app.aipacemaker.backend.auth.model.*;
import app.aipacemaker.backend.auth.model.exception.DuplicateEmailException;
import app.aipacemaker.backend.auth.model.exception.InvalidPasswordException;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class RegisterUser {

    private final UserRepository userRepository;
    private final EmailVerificationTokenRepository tokenRepository;
    private final PasswordValidator passwordValidator;
    private final ApplicationEventPublisher eventPublisher;
    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    @Transactional
    public Result execute(Command command) {
        // 1. 비밀번호 검증
        if (!passwordValidator.isValid(command.password)) {
            throw new InvalidPasswordException("비밀번호는 8자 이상이며 영문자와 숫자를 포함해야 합니다.");
        }

        // 2. 중복 이메일 체크
        if (userRepository.existsByEmail(command.email)) {
            throw new DuplicateEmailException("이미 사용 중인 이메일입니다: " + command.email);
        }

        // 3. 비밀번호 암호화 및 사용자 저장
        String encodedPassword = passwordEncoder.encode(command.password);
        User user = User.builder()
                .email(command.email)
                .password(encodedPassword)
                .emailVerified(false)
                .build();
        User savedUser = userRepository.save(user);

        // 4. 인증 토큰 생성 및 저장
        String token = UUID.randomUUID().toString();
        EmailVerificationToken verificationToken = EmailVerificationToken.builder()
                .userId(savedUser.getId())
                .token(token)
                .expiresAt(LocalDateTime.now().plusHours(24))
                .build();
        tokenRepository.save(verificationToken);

        // 5. 이벤트 발행
        eventPublisher.publishEvent(new UserRegisteredEvent(
                savedUser.getId(),
                savedUser.getEmail(),
                token
        ));

        return new Result(savedUser.getId(), savedUser.getEmail(), true);
    }

    public record Command(String email, String password) {}

    public record Result(Long userId, String email, boolean verificationTokenCreated) {}
}
