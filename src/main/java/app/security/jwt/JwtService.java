package app.security.jwt;

import app.security.UserPrincipal;
import app.user.model.UserRole;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.Optional;
import java.util.UUID;

@Component
public class JwtService {

    @Value("${app.jwt.secret}")
    private String secretKey;

    @Value("${app.jwt.access-expiration-ms}")
    private long accessExpirationMs;

    private SecretKey getSigningKey() {
        byte[] keyBytes = Decoders.BASE64.decode(secretKey);
        return Keys.hmacShaKeyFor(keyBytes);
    }

    public IssuedAccessToken generateAccessToken(UUID userId, String username, UserRole role) {
        Date issuedAt = new Date();
        Date expiration = new Date(issuedAt.getTime() + accessExpirationMs);

        String token = Jwts.builder()
                .subject(username)
                .claim("userId", userId.toString())
                .claim("role", role.name())
                .issuedAt(issuedAt)
                .expiration(expiration)
                .signWith(getSigningKey())
                .compact();

        LocalDateTime expiresAt = expiration.toInstant()
                .atZone(ZoneId.systemDefault())
                .toLocalDateTime();

        return IssuedAccessToken.builder()
                .token(token)
                .expiresAt(expiresAt)
                .build();
    }

    public Optional<UserPrincipal> resolvePrincipal(String token) {
        try {
            Claims claims = parseClaims(token);
            UUID userId = UUID.fromString(claims.get("userId", String.class));
            String username = claims.getSubject();
            UserRole role = UserRole.valueOf(claims.get("role", String.class));

            return Optional.of(UserPrincipal.fromClaims(userId, username, role));
        } catch (JwtException | IllegalArgumentException e) {
            return Optional.empty();
        }
    }

    private Claims parseClaims(String token) {
        return Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }
}
