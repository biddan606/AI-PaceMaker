package app.aipacemaker.backend.auth.fixture;

import app.aipacemaker.backend.auth.model.User;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

/**
 * User 엔티티 테스트 픽스처
 */
public class UserFixture {

    private static final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder(4);

    public static User.UserBuilder defaultUser() {
        return User.builder()
                .name("테스트유저")
                .email("test@example.com")
                .password(passwordEncoder.encode("password123"))
                .emailVerified(false);
    }

    public static User.UserBuilder verifiedUser() {
        return defaultUser()
                .emailVerified(true);
    }

    public static User.UserBuilder unverifiedUser() {
        return defaultUser()
                .emailVerified(false);
    }

    public static User.UserBuilder withName(String name) {
        return defaultUser()
                .name(name);
    }

    public static User.UserBuilder withEmail(String email) {
        return defaultUser()
                .email(email);
    }

    public static User.UserBuilder withPassword(String rawPassword) {
        return defaultUser()
                .password(passwordEncoder.encode(rawPassword));
    }
}
