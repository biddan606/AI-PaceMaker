package app.aipacemaker.backend.auth.model;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * 비밀번호 검증 단위 테스트
 */
@DisplayName("비밀번호 검증 단위 테스트")
class PasswordValidatorTest {

    private PasswordValidator passwordValidator;

    @BeforeEach
    void setUp() {
        passwordValidator = new PasswordValidator();
    }

    @Test
    @DisplayName("비밀번호가 8자 미만이면 false를 반환한다")
    void tooShortPassword() {
        // given
        String password = "abc123";

        // when
        boolean result = passwordValidator.isValid(password);

        // then
        assertThat(result).isFalse();
    }

    @Test
    @DisplayName("비밀번호가 영문자를 포함하지 않으면 false를 반환한다")
    void missingLetters() {
        // given
        String password = "12345678";

        // when
        boolean result = passwordValidator.isValid(password);

        // then
        assertThat(result).isFalse();
    }

    @Test
    @DisplayName("비밀번호가 숫자를 포함하지 않으면 false를 반환한다")
    void missingDigits() {
        // given
        String password = "abcdefgh";

        // when
        boolean result = passwordValidator.isValid(password);

        // then
        assertThat(result).isFalse();
    }

    @Test
    @DisplayName("비밀번호가 null이면 false를 반환한다")
    void nullPassword() {
        // when
        boolean result = passwordValidator.isValid(null);

        // then
        assertThat(result).isFalse();
    }

    @Test
    @DisplayName("비밀번호가 8자 이상이고 영문자와 숫자를 포함하면 true를 반환한다")
    void validPassword() {
        // given
        String password = "password123";

        // when
        boolean result = passwordValidator.isValid(password);

        // then
        assertThat(result).isTrue();
    }

    @Test
    @DisplayName("유효하지 않은 비밀번호로 validate 호출 시 IllegalArgumentException이 발생한다")
    void invalidPasswordThrowsException() {
        // given
        String password = "short1";

        // when & then
        assertThatThrownBy(() -> passwordValidator.validate(password))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("비밀번호는 8자 이상이며 영문자와 숫자를 포함해야 합니다.");
    }

    @Test
    @DisplayName("유효한 비밀번호로 validate 호출 시 예외가 발생하지 않는다")
    void validPasswordNoException() {
        // given
        String password = "validPass123";

        // when & then
        passwordValidator.validate(password); // 예외가 발생하지 않아야 함
    }
}
