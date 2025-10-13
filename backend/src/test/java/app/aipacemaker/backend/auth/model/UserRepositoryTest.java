package app.aipacemaker.backend.auth.model;

import app.aipacemaker.backend.TestcontainersConfiguration;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@Import(TestcontainersConfiguration.class)
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@ActiveProfiles("test")
@DisplayName("UserRepository 통합 테스트")
class UserRepositoryTest {

    @Autowired
    private UserRepository userRepository;

    @Test
    @DisplayName("사용자를 저장하고 조회할 수 있다")
    void saveAndFindUser() {
        // given
        User user = User.builder()
                .email("test@example.com")
                .password("encodedPassword123")
                .emailVerified(false)
                .build();

        // when
        User savedUser = userRepository.save(user);
        Optional<User> foundUser = userRepository.findById(savedUser.getId());

        // then
        assertThat(foundUser).isPresent();
        assertThat(foundUser.get().getEmail()).isEqualTo("test@example.com");
        assertThat(foundUser.get().getPassword()).isEqualTo("encodedPassword123");
        assertThat(foundUser.get().isEmailVerified()).isFalse();
        assertThat(foundUser.get().getCreatedAt()).isNotNull();
    }

    @Test
    @DisplayName("이메일로 사용자를 조회할 수 있다")
    void findByEmail() {
        // given
        User user = User.builder()
                .email("test@example.com")
                .password("encodedPassword123")
                .emailVerified(false)
                .build();
        userRepository.save(user);

        // when
        Optional<User> foundUser = userRepository.findByEmail("test@example.com");

        // then
        assertThat(foundUser).isPresent();
        assertThat(foundUser.get().getEmail()).isEqualTo("test@example.com");
    }

    @Test
    @DisplayName("존재하지 않는 이메일로 조회 시 빈 Optional을 반환한다")
    void findByEmailNotFound() {
        // when
        Optional<User> foundUser = userRepository.findByEmail("notfound@example.com");

        // then
        assertThat(foundUser).isEmpty();
    }

    @Test
    @DisplayName("이메일 중복 여부를 확인할 수 있다")
    void existsByEmail() {
        // given
        User user = User.builder()
                .email("test@example.com")
                .password("encodedPassword123")
                .emailVerified(false)
                .build();
        userRepository.save(user);

        // when
        boolean exists = userRepository.existsByEmail("test@example.com");
        boolean notExists = userRepository.existsByEmail("notfound@example.com");

        // then
        assertThat(exists).isTrue();
        assertThat(notExists).isFalse();
    }
}
