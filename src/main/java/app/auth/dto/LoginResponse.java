package app.auth.dto;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Builder
@Getter
public class LoginResponse {

    private String token;
    private LocalDateTime expiresAt;
}
