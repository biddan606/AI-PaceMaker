package app.aipacemaker.backend.auth.model;

import org.springframework.stereotype.Component;

import java.util.regex.Pattern;

@Component
public class PasswordValidator {

    private static final int MIN_LENGTH = 8;
    private static final Pattern LETTER_PATTERN = Pattern.compile("[a-zA-Z]");
    private static final Pattern DIGIT_PATTERN = Pattern.compile("\\d");

    public boolean isValid(String password) {
        if (password == null || password.length() < MIN_LENGTH) {
            return false;
        }

        boolean hasLetter = LETTER_PATTERN.matcher(password).find();
        boolean hasDigit = DIGIT_PATTERN.matcher(password).find();

        return hasLetter && hasDigit;
    }

    public void validate(String password) {
        if (!isValid(password)) {
            throw new IllegalArgumentException("비밀번호는 8자 이상이며 영문자와 숫자를 포함해야 합니다.");
        }
    }
}
