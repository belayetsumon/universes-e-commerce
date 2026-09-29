package com.ecommerce.app.module.user.ripository;

import com.ecommerce.app.module.user.model.PasswordResetToken;
import java.time.LocalDateTime;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.jpa.repository.EntityGraph;

public interface PasswordResetTokenRepository extends JpaRepository<PasswordResetToken, Long> {

    @EntityGraph(attributePaths = "user")
    Optional<PasswordResetToken> findByTokenHash(String tokenHash);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("""
            update PasswordResetToken token
            set token.usedAt = :usedAt
            where token.user.id = :userId
              and token.usedAt is null
              and token.expiresAt > :now
            """)
    int invalidateActiveTokens(
            @Param("userId") Long userId,
            @Param("now") LocalDateTime now,
            @Param("usedAt") LocalDateTime usedAt);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("""
            update PasswordResetToken token
            set token.usedAt = :usedAt
            where token.id = :id
              and token.usedAt is null
              and token.expiresAt > :now
            """)
    int consumeIfValid(
            @Param("id") Long id,
            @Param("now") LocalDateTime now,
            @Param("usedAt") LocalDateTime usedAt);
}
