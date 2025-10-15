package app.aipacemaker.backend.auth.endpoint;

import app.aipacemaker.backend.auth.usecase.LoginUser;
import app.aipacemaker.backend.auth.usecase.RegisterUser;
import app.aipacemaker.backend.auth.usecase.RenewAccessToken;
import app.aipacemaker.backend.auth.usecase.VerifyEmail;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
public class AuthController {

    private final RegisterUser registerUser;
    private final VerifyEmail verifyEmail;
    private final LoginUser loginUser;
    private final RenewAccessToken renewAccessToken;

    @PostMapping("/api/users")
    @ResponseStatus(HttpStatus.CREATED)
    public RegisterResponse createUser(@Valid @RequestBody RegisterRequest request) {
        RegisterUser.Command command = new RegisterUser.Command(request.name, request.email, request.password);
        RegisterUser.Result result = registerUser.execute(command);

        return new RegisterResponse(
                result.userId(),
                result.email(),
                "회원가입이 완료되었습니다. 이메일을 확인하여 계정을 활성화해주세요."
        );
    }

    @PostMapping("/api/users/verification")
    @ResponseStatus(HttpStatus.OK)
    public VerifyEmailResponse verifyEmail(@Valid @RequestBody VerifyEmailRequest request) {
        VerifyEmail.Command command = new VerifyEmail.Command(request.token);
        VerifyEmail.Result result = verifyEmail.execute(command);

        return new VerifyEmailResponse(
                result.userId(),
                result.email(),
                result.verified(),
                "이메일 인증이 완료되었습니다."
        );
    }

    record RegisterRequest(
            @NotBlank(message = "이름은 필수입니다")
            String name,

            @NotBlank(message = "이메일은 필수입니다")
            @Email(message = "올바른 이메일 형식이 아닙니다")
            String email,

            @NotBlank(message = "비밀번호는 필수입니다")
            String password
    ) {}

    record RegisterResponse(
            Long userId,
            String email,
            String message
    ) {}

    record VerifyEmailRequest(
            @NotBlank(message = "토큰은 필수입니다")
            String token
    ) {}

    record VerifyEmailResponse(
            Long userId,
            String email,
            boolean verified,
            String message
    ) {}

    @PostMapping("/api/auth/login")
    @ResponseStatus(HttpStatus.OK)
    public LoginResponse login(@Valid @RequestBody LoginRequest request) {
        LoginUser.Command command = new LoginUser.Command(request.email, request.password, request.deviceId);
        LoginUser.Result result = loginUser.execute(command);

        return new LoginResponse(
                result.userId(),
                result.email(),
                result.emailVerified(),
                result.accessToken(),
                result.refreshToken()
        );
    }

    @PostMapping("/api/auth/refresh")
    @ResponseStatus(HttpStatus.OK)
    public RefreshTokenResponse refreshToken(@Valid @RequestBody RefreshTokenRequest request) {
        RenewAccessToken.Command command = new RenewAccessToken.Command(request.refreshToken);
        RenewAccessToken.Result result = renewAccessToken.execute(command);

        return new RefreshTokenResponse(result.accessToken());
    }

    record LoginRequest(
            @NotBlank(message = "이메일은 필수입니다")
            @Email(message = "올바른 이메일 형식이 아닙니다")
            String email,

            @NotBlank(message = "비밀번호는 필수입니다")
            String password,

            @NotBlank(message = "디바이스 ID는 필수입니다")
            String deviceId
    ) {}

    record LoginResponse(
            Long userId,
            String email,
            boolean emailVerified,
            String accessToken,
            String refreshToken
    ) {}

    record RefreshTokenRequest(
            @NotBlank(message = "Refresh Token은 필수입니다")
            String refreshToken
    ) {}

    record RefreshTokenResponse(
            String accessToken
    ) {}
}
