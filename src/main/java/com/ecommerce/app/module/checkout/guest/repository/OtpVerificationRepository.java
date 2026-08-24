package com.ecommerce.app.module.checkout.guest.repository;

import com.ecommerce.app.module.checkout.guest.model.OtpPurpose;
import com.ecommerce.app.module.checkout.guest.model.OtpStatus;
import com.ecommerce.app.module.checkout.guest.model.OtpVerification;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

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
}
