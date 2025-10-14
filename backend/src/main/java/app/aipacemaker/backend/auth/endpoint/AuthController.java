package app.aipacemaker.backend.auth.endpoint;

import app.aipacemaker.backend.auth.usecase.RegisterUser;
import app.aipacemaker.backend.auth.usecase.VerifyEmail;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class AuthController {

    private final RegisterUser registerUser;
    private final VerifyEmail verifyEmail;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public RegisterResponse createUser(@Valid @RequestBody RegisterRequest request) {
        RegisterUser.Command command = new RegisterUser.Command(request.email, request.password);
        RegisterUser.Result result = registerUser.execute(command);

        return new RegisterResponse(
                result.userId(),
                result.email(),
                "회원가입이 완료되었습니다. 이메일을 확인하여 계정을 활성화해주세요."
        );
    }

    @PostMapping("/verification")
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
}
