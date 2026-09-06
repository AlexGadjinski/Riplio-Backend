package app.auth.repository;

import app.auth.model.RefreshToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface RefreshTokenRepository extends JpaRepository<RefreshToken, UUID> {

    Optional<RefreshToken> findByTokenHash(String tokenHash);

    @Query("""
            UPDATE RefreshToken rt SET rt.revoked = true
            WHERE rt.userId = :userId AND rt.revoked = false
            """)
    @Modifying
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    void revokeAllByUserId(UUID userId);
}
