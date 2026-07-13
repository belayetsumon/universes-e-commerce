package com.ecommerce.app.module.checkout.guest.repository;

import com.ecommerce.app.module.checkout.guest.model.OtpPurpose;
import com.ecommerce.app.module.checkout.guest.model.OtpStatus;
import com.ecommerce.app.module.checkout.guest.model.OtpVerification;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OtpVerificationRepository extends JpaRepository<OtpVerification, Long> {

    Optional<OtpVerification> findBySessionToken(String sessionToken);

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

    long countByMobileNumberAndCreatedAtAfter(String mobileNumber, LocalDateTime createdAt);

    long countByIpAddressHashAndCreatedAtAfter(String ipAddressHash, LocalDateTime createdAt);

    long countByDeviceFingerprintHashAndCreatedAtAfter(String deviceFingerprintHash, LocalDateTime createdAt);

    long countByHttpSessionIdAndCreatedAtAfter(String httpSessionId, LocalDateTime createdAt);
}
