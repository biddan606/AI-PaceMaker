package app.aipacemaker.backend.auth.endpoint;

import app.aipacemaker.backend.auth.model.exception.DuplicateEmailException;
import app.aipacemaker.backend.auth.model.exception.ExpiredRefreshTokenException;
import app.aipacemaker.backend.auth.model.exception.ExpiredVerificationTokenException;
import app.aipacemaker.backend.auth.model.exception.InvalidCredentialsException;
import app.aipacemaker.backend.auth.model.exception.InvalidPasswordException;
import app.aipacemaker.backend.auth.model.exception.InvalidRefreshTokenException;
import app.aipacemaker.backend.auth.model.exception.InvalidVerificationTokenException;
import app.aipacemaker.backend.auth.model.exception.UserNotFoundException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@Slf4j
@RestControllerAdvice(basePackages = "app.aipacemaker.backend.auth")
public class AuthExceptionHandler {

    // 회원가입 관련 예외 (Bad Request)
    @ExceptionHandler({InvalidPasswordException.class, ExpiredVerificationTokenException.class})
    public ProblemDetail handleRegistrationBadRequestException(RuntimeException e) {
        log.warn("회원가입 요청 오류: {}", e.getMessage());
        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, e.getMessage());
        problemDetail.setTitle("잘못된 요청");
        return problemDetail;
    }

    // 리소스 중복 예외 (Conflict)
    @ExceptionHandler(DuplicateEmailException.class)
    public ProblemDetail handleDuplicateResourceException(DuplicateEmailException e) {
        log.warn("리소스 중복: {}", e.getMessage());
        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(HttpStatus.CONFLICT, e.getMessage());
        problemDetail.setTitle("리소스 중복");
        return problemDetail;
    }

    // 리소스 없음 예외 (Not Found)
    @ExceptionHandler({UserNotFoundException.class, InvalidVerificationTokenException.class})
    public ProblemDetail handleResourceNotFoundException(RuntimeException e) {
        log.warn("리소스 없음: {}", e.getMessage());
        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND, e.getMessage());
        problemDetail.setTitle("리소스를 찾을 수 없음");
        return problemDetail;
    }

    // 인증 실패 예외 (Unauthorized)
    @ExceptionHandler({InvalidCredentialsException.class, InvalidRefreshTokenException.class, ExpiredRefreshTokenException.class})
    public ProblemDetail handleUnauthorizedException(RuntimeException e) {
        log.warn("인증 실패: {}", e.getMessage());
        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(HttpStatus.UNAUTHORIZED, e.getMessage());
        problemDetail.setTitle("인증 실패");
        return problemDetail;
    }
}
