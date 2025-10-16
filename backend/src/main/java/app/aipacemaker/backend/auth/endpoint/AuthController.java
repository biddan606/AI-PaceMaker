package app.aipacemaker.backend.auth.endpoint;

import app.aipacemaker.backend.auth.usecase.LoginUser;
import app.aipacemaker.backend.auth.usecase.RegisterUser;
import app.aipacemaker.backend.auth.usecase.RenewAccessToken;
import app.aipacemaker.backend.auth.usecase.VerifyEmail;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
public class AuthController {

    private final RegisterUser registerUser;
    private final VerifyEmail verifyEmail;
    private final LoginUser loginUser;
    private final RenewAccessToken renewAccessToken;

    /**
     * HttpOnly Cookie 생성 헬퍼 메서드
     */
    private Cookie createHttpOnlyCookie(String name, String value, int maxAgeInSeconds) {
        Cookie cookie = new Cookie(name, value);
        cookie.setHttpOnly(true);
        cookie.setSecure(false); // HTTPS 환경에서는 true로 설정
        cookie.setPath("/");
        cookie.setMaxAge(maxAgeInSeconds);
        return cookie;
    }

    @PostMapping("/api/users")
    public ResponseEntity<RegisterResponse> createUser(@Valid @RequestBody RegisterRequest request) {
        RegisterUser.Command command = new RegisterUser.Command(request.name, request.email, request.password);
        RegisterUser.Result result = registerUser.execute(command);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new RegisterResponse(result.userId(), result.email()));
    }

    @PostMapping("/api/users/verification")
    public ResponseEntity<VerifyEmailResponse> verifyEmail(@Valid @RequestBody VerifyEmailRequest request) {
        VerifyEmail.Command command = new VerifyEmail.Command(request.token);
        VerifyEmail.Result result = verifyEmail.execute(command);

        return ResponseEntity.ok(new VerifyEmailResponse(
                result.userId(),
                result.email(),
                result.verified()
        ));
    }

    public record RegisterRequest(
            @NotBlank(message = "이름은 필수입니다")
            String name,

            @NotBlank(message = "이메일은 필수입니다")
            @Email(message = "올바른 이메일 형식이 아닙니다")
            String email,

            @NotBlank(message = "비밀번호는 필수입니다")
            String password
    ) {}

    public record RegisterResponse(
            Long userId,
            String email
    ) {}

    public record VerifyEmailRequest(
            @NotBlank(message = "토큰은 필수입니다")
            String token
    ) {}

    public record VerifyEmailResponse(
            Long userId,
            String email,
            boolean verified
    ) {}

    @PostMapping("/api/auth/login")
    public ResponseEntity<LoginResponse> login(@Valid @RequestBody LoginRequest request, HttpServletResponse response) {
        LoginUser.Command command = new LoginUser.Command(request.email, request.password, request.deviceId);
        LoginUser.Result result = loginUser.execute(command);

        // Access Token을 HttpOnly Cookie로 설정 (1시간 = 3600초)
        Cookie accessTokenCookie = createHttpOnlyCookie("accessToken", result.accessToken(), 3600);
        response.addCookie(accessTokenCookie);

        // Refresh Token을 HttpOnly Cookie로 설정 (30일 = 2592000초)
        Cookie refreshTokenCookie = createHttpOnlyCookie("refreshToken", result.refreshToken(), 2592000);
        response.addCookie(refreshTokenCookie);

        // 응답 JSON에는 토큰을 제외하고 사용자 정보만 반환
        return ResponseEntity.ok(new LoginResponse(
                result.userId(),
                result.email(),
                result.emailVerified()
        ));
    }

    @PostMapping("/api/auth/refresh")
    public ResponseEntity<RefreshTokenResponse> refreshToken(@CookieValue(value = "refreshToken", required = false) String refreshTokenFromCookie,
                                              @RequestBody(required = false) RefreshTokenRequest request,
                                              HttpServletResponse response) {
        // Cookie 또는 RequestBody에서 refreshToken 가져오기
        String refreshToken = refreshTokenFromCookie != null ? refreshTokenFromCookie :
                             (request != null ? request.refreshToken : null);

        if (refreshToken == null || refreshToken.isBlank()) {
            throw new IllegalArgumentException("Refresh Token은 필수입니다");
        }

        RenewAccessToken.Command command = new RenewAccessToken.Command(refreshToken);
        RenewAccessToken.Result result = renewAccessToken.execute(command);

        // 새로운 Access Token을 HttpOnly Cookie로 설정
        Cookie accessTokenCookie = createHttpOnlyCookie("accessToken", result.accessToken(), 3600);
        response.addCookie(accessTokenCookie);

        return ResponseEntity.ok(new RefreshTokenResponse(result.accessToken()));
    }

    public record LoginRequest(
            @NotBlank(message = "이메일은 필수입니다")
            @Email(message = "올바른 이메일 형식이 아닙니다")
            String email,

            @NotBlank(message = "비밀번호는 필수입니다")
            String password,

            @NotBlank(message = "디바이스 ID는 필수입니다")
            String deviceId
    ) {}

    public record LoginResponse(
            Long userId,
            String email,
            boolean emailVerified
    ) {}

    public record RefreshTokenRequest(
            @NotBlank(message = "Refresh Token은 필수입니다")
            String refreshToken
    ) {}

    public record RefreshTokenResponse(
            String accessToken
    ) {}
}
