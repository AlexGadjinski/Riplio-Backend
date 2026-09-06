package app.auth.service;

import app.auth.dto.*;
import app.common.exception.InvalidRefreshTokenException;
import app.common.exception.ResourceConflictException;
import app.security.UserPrincipal;
import app.security.jwt.IssuedAccessToken;
import app.security.jwt.JwtService;
import app.user.model.User;
import app.user.model.UserRole;
import app.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final RefreshTokenService refreshTokenService;

    public void register(RegisterRequest request) {

        if (userRepository.existsByUsername(request.getUsername())) {
            throw new ResourceConflictException("Username [%s] is already taken.".formatted(request.getUsername()));
        }

        if (userRepository.existsByEmail(request.getEmail())) {
            throw new ResourceConflictException("Email [%s] is already registered.".formatted(request.getEmail()));
        }

        LocalDateTime now = LocalDateTime.now();
        User user = User.builder()
                .username(request.getUsername())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .role(UserRole.USER)
                .createdOn(now)
                .updatedOn(now)
                .build();

        userRepository.save(user);
        log.info("User with id [{}] and username [{}] registered successfully.", user.getId(), user.getUsername());
    }

    public AuthTokensResponse login(LoginRequest request) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword()));
        UserPrincipal principal = (UserPrincipal) authentication.getPrincipal();

        IssuedAccessToken accessToken = jwtService.generateAccessToken(
                principal.getUserId(), principal.getUsername(), principal.getRole());
        IssuedRefreshToken refreshToken = refreshTokenService.issue(principal.getUserId());

        return buildAuthTokensResponse(accessToken, refreshToken);
    }

    public AuthTokensResponse refresh(RefreshTokenRequest request) {
        IssuedRefreshToken rotatedRefreshToken = refreshTokenService.rotate(request.getRefreshToken());
        User user = userRepository.findById(rotatedRefreshToken.getUserId())
                .orElseThrow(InvalidRefreshTokenException::new);

        IssuedAccessToken accessToken = jwtService.generateAccessToken(user.getId(), user.getUsername(), user.getRole());

        return buildAuthTokensResponse(accessToken, rotatedRefreshToken);
    }

    public void logout(RefreshTokenRequest request) {
        refreshTokenService.revoke(request.getRefreshToken());
    }

    private AuthTokensResponse buildAuthTokensResponse(IssuedAccessToken accessToken, IssuedRefreshToken refreshToken) {
        return AuthTokensResponse.builder()
                .accessToken(accessToken.getToken())
                .accessTokenExpiresAt(accessToken.getExpiresAt())
                .refreshToken(refreshToken.getToken())
                .refreshTokenExpiresAt(refreshToken.getExpiresAt())
                .build();
    }
}
