package app.security.jwt;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Builder
@Getter
public class GeneratedToken {

    private String token;
    private LocalDateTime expiresAt;
}
