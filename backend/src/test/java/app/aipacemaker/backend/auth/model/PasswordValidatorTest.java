package app.aipacemaker.backend.auth.model;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("PasswordValidator 단위 테스트")
class PasswordValidatorTest {

    private PasswordValidator passwordValidator;

    @BeforeEach
    void setUp() {
        passwordValidator = new PasswordValidator();
    }

    @Test
    @DisplayName("8자 미만 비밀번호는 유효하지 않다")
    void invalidWhenLessThan8Characters() {
        // given
        String password = "abc123";

        // when
        boolean result = passwordValidator.isValid(password);

        // then
        assertThat(result).isFalse();
    }

    @Test
    @DisplayName("영문자가 없는 비밀번호는 유효하지 않다")
    void invalidWhenNoLetter() {
        // given
        String password = "12345678";

        // when
        boolean result = passwordValidator.isValid(password);

        // then
        assertThat(result).isFalse();
    }

    @Test
    @DisplayName("숫자가 없는 비밀번호는 유효하지 않다")
    void invalidWhenNoDigit() {
        // given
        String password = "abcdefgh";

        // when
        boolean result = passwordValidator.isValid(password);

        // then
        assertThat(result).isFalse();
    }

    @Test
    @DisplayName("null 비밀번호는 유효하지 않다")
    void invalidWhenNull() {
        // when
        boolean result = passwordValidator.isValid(null);

        // then
        assertThat(result).isFalse();
    }

    @Test
    @DisplayName("8자 이상이고 영문자와 숫자를 포함한 비밀번호는 유효하다")
    void validPassword() {
        // given
        String password = "password123";

        // when
        boolean result = passwordValidator.isValid(password);

        // then
        assertThat(result).isTrue();
    }

    @Test
    @DisplayName("유효하지 않은 비밀번호로 validate 호출 시 예외가 발생한다")
    void validateThrowsExceptionForInvalidPassword() {
        // given
        String password = "short1";

        // when & then
        assertThatThrownBy(() -> passwordValidator.validate(password))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("비밀번호는 8자 이상이며 영문자와 숫자를 포함해야 합니다.");
    }

    @Test
    @DisplayName("유효한 비밀번호로 validate 호출 시 예외가 발생하지 않는다")
    void validateDoesNotThrowForValidPassword() {
        // given
        String password = "validPass123";

        // when & then
        passwordValidator.validate(password); // 예외가 발생하지 않아야 함
    }
}
