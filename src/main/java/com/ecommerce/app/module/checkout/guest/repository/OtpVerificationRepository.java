package com.ecommerce.app.module.checkout.guest.repository;

import com.ecommerce.app.module.checkout.guest.model.OtpPurpose;
import com.ecommerce.app.module.checkout.guest.model.OtpStatus;
import com.ecommerce.app.module.checkout.guest.model.OtpVerification;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

public interface OtpVerificationRepository extends JpaRepository<OtpVerification, Long> {

    Optional<OtpVerification> findBySessionToken(String sessionToken);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select verification from OtpVerification verification where verification.sessionToken = :sessionToken")
    Optional<OtpVerification> findBySessionTokenForUpdate(@Param("sessionToken") String sessionToken);

    Optional<OtpVerification> findByUuid(String uuid);

    Optional<OtpVerification> findTopByMobileNumberAndPurposeAndStatusOrderByCreatedAtDesc(
            String mobileNumber,
            OtpPurpose purpose,
            OtpStatus status
    );

    Optional<OtpVerification> findTopByMobileNumberAndPurposeOrderByIdDesc(
            String mobileNumber,
            OtpPurpose purpose
    );

    List<OtpVerification> findByMobileNumberAndPurposeAndStatus(
            String mobileNumber,
            OtpPurpose purpose,
            OtpStatus status
    );

    Optional<OtpVerification> findTopByUserIdAndPurposeAndStatusOrderByCreatedAtDesc(
            Long userId,
            OtpPurpose purpose,
            OtpStatus status
    );

    Optional<OtpVerification> findTopByUserIdAndPurposeOrderByIdDesc(
            Long userId,
            OtpPurpose purpose
    );

    List<OtpVerification> findByUserIdAndPurposeAndStatus(
            Long userId,
            OtpPurpose purpose,
            OtpStatus status
    );

    long countByUserIdAndPurposeAndCreatedAtAfter(
            Long userId,
            OtpPurpose purpose,
            LocalDateTime createdAt
    );

    long countByMobileNumberAndPurposeAndCreatedAtAfter(
            String mobileNumber,
            OtpPurpose purpose,
            LocalDateTime createdAt
    );

    long countByMobileNumberAndCreatedAtAfter(String mobileNumber, LocalDateTime createdAt);

    long countByIpAddressHashAndCreatedAtAfter(String ipAddressHash, LocalDateTime createdAt);

    long countByDeviceFingerprintHashAndCreatedAtAfter(String deviceFingerprintHash, LocalDateTime createdAt);

    long countByHttpSessionIdAndCreatedAtAfter(String httpSessionId, LocalDateTime createdAt);

    @Transactional(readOnly = true)
    @Query("""
            select verification.id
            from OtpVerification verification
            where verification.status in :statuses
              and verification.createdAt < :cutoff
            order by verification.id
            """)
    List<Long> findTerminalRetentionCandidateIds(
            @Param("statuses") Collection<OtpStatus> statuses,
            @Param("cutoff") LocalDateTime cutoff,
            Pageable pageable
    );

    @Transactional
    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("""
            delete from OtpVerification verification
            where verification.id in :ids
              and verification.status in :statuses
              and verification.createdAt < :cutoff
            """)
    int deleteTerminalRetentionCandidates(
            @Param("ids") Collection<Long> ids,
            @Param("statuses") Collection<OtpStatus> statuses,
            @Param("cutoff") LocalDateTime cutoff
    );

    @Transactional(readOnly = true)
    @Query("""
            select verification.id
            from OtpVerification verification
            where verification.status = :pendingStatus
              and verification.expiresAt < :now
              and verification.createdAt < :cutoff
            order by verification.id
            """)
    List<Long> findExpiredPendingRetentionCandidateIds(
            @Param("pendingStatus") OtpStatus pendingStatus,
            @Param("now") LocalDateTime now,
            @Param("cutoff") LocalDateTime cutoff,
            Pageable pageable
    );

    @Transactional
    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("""
            delete from OtpVerification verification
            where verification.id in :ids
              and verification.status = :pendingStatus
              and verification.expiresAt < :now
              and verification.createdAt < :cutoff
            """)
    int deleteExpiredPendingRetentionCandidates(
            @Param("ids") Collection<Long> ids,
            @Param("pendingStatus") OtpStatus pendingStatus,
            @Param("now") LocalDateTime now,
            @Param("cutoff") LocalDateTime cutoff
    );
}
