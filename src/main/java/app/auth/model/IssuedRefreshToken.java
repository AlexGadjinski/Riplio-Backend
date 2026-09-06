package app.auth.model;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.UUID;

@Builder
@Getter
public class IssuedRefreshToken {

    private UUID userId;
    private String token;
    private LocalDateTime expiresAt;
}
