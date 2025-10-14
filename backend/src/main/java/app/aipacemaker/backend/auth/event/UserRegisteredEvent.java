package app.aipacemaker.backend.auth.event;

public record UserRegisteredEvent(
        Long userId,
        String email,
        String verificationToken
) {
}
