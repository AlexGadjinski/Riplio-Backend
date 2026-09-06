package app.security.jwt;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Builder
@Getter
public class IssuedAccessToken {

    private String token;
    private LocalDateTime expiresAt;
}
