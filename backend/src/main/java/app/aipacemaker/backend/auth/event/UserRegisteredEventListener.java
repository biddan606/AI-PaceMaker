package app.aipacemaker.backend.auth.event;

import app.aipacemaker.backend.auth.usecase.SendVerificationEmail;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class UserRegisteredEventListener {

    private final SendVerificationEmail sendVerificationEmail;

    @EventListener
    public void handleUserRegisteredEvent(UserRegisteredEvent event) {
        log.info("사용자 등록 이벤트 수신: {}", event.email());

        SendVerificationEmail.Command command = new SendVerificationEmail.Command(
                event.email(),
                event.verificationToken()
        );

        sendVerificationEmail.execute(command);

        log.info("인증 이메일 발송 완료: {}", event.email());
    }
}
