package com.ecommerce.app.module.fraud.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Index;
import jakarta.persistence.Lob;
import jakarta.persistence.Table;
import java.time.LocalDateTime;

@Entity
@Table(name = "fraud_device_identities", indexes = {
    @Index(name = "idx_fraud_device_identifier", columnList = "device_identifier"),
    @Index(name = "idx_fraud_device_fingerprint", columnList = "device_fingerprint_hash"),
    @Index(name = "idx_fraud_device_customer", columnList = "customer_id"),
    @Index(name = "idx_fraud_device_ip", columnList = "ip_address")
})
public class DeviceIdentity extends BaseFraudEntity {

    @Column(name = "customer_id")
    private Long customerId;

    @Column(name = "device_identifier", nullable = false, length = 160)
    private String deviceIdentifier;

    @Column(name = "device_fingerprint_hash", length = 128)
    private String deviceFingerprintHash;

    @Column(name = "user_agent", length = 500)
    private String userAgent;

    @Column(name = "session_identifier", length = 160)
    private String sessionIdentifier;

    @Column(name = "ip_address", length = 80)
    private String ipAddress;

    @Column(name = "ip_country", length = 80)
    private String ipCountry;

    @Column(name = "ip_location", length = 160)
    private String ipLocation;

    @Column(name = "vpn_indicator", nullable = false)
    private boolean vpnIndicator;

    @Column(name = "proxy_indicator", nullable = false)
    private boolean proxyIndicator;

    @Column(name = "hosting_indicator", nullable = false)
    private boolean hostingIndicator;

    @Column(name = "trusted", nullable = false)
    private boolean trusted;

    @Column(name = "blacklisted", nullable = false)
    private boolean blacklisted;

    @Column(name = "first_seen_at")
    private LocalDateTime firstSeenAt;

    @Column(name = "last_seen_at")
    private LocalDateTime lastSeenAt;

    @Lob
    @Column(name = "metadata_json", columnDefinition = "TEXT")
    private String metadataJson;

    public Long getCustomerId() { return customerId; }
    public void setCustomerId(Long customerId) { this.customerId = customerId; }
    public String getDeviceIdentifier() { return deviceIdentifier; }
    public void setDeviceIdentifier(String deviceIdentifier) { this.deviceIdentifier = deviceIdentifier; }
    public String getDeviceFingerprintHash() { return deviceFingerprintHash; }
    public void setDeviceFingerprintHash(String deviceFingerprintHash) { this.deviceFingerprintHash = deviceFingerprintHash; }
    public String getUserAgent() { return userAgent; }
    public void setUserAgent(String userAgent) { this.userAgent = userAgent; }
    public String getSessionIdentifier() { return sessionIdentifier; }
    public void setSessionIdentifier(String sessionIdentifier) { this.sessionIdentifier = sessionIdentifier; }
    public String getIpAddress() { return ipAddress; }
    public void setIpAddress(String ipAddress) { this.ipAddress = ipAddress; }
    public String getIpCountry() { return ipCountry; }
    public void setIpCountry(String ipCountry) { this.ipCountry = ipCountry; }
    public String getIpLocation() { return ipLocation; }
    public void setIpLocation(String ipLocation) { this.ipLocation = ipLocation; }
    public boolean isVpnIndicator() { return vpnIndicator; }
    public void setVpnIndicator(boolean vpnIndicator) { this.vpnIndicator = vpnIndicator; }
    public boolean isProxyIndicator() { return proxyIndicator; }
    public void setProxyIndicator(boolean proxyIndicator) { this.proxyIndicator = proxyIndicator; }
    public boolean isHostingIndicator() { return hostingIndicator; }
    public void setHostingIndicator(boolean hostingIndicator) { this.hostingIndicator = hostingIndicator; }
    public boolean isTrusted() { return trusted; }
    public void setTrusted(boolean trusted) { this.trusted = trusted; }
    public boolean isBlacklisted() { return blacklisted; }
    public void setBlacklisted(boolean blacklisted) { this.blacklisted = blacklisted; }
    public LocalDateTime getFirstSeenAt() { return firstSeenAt; }
    public void setFirstSeenAt(LocalDateTime firstSeenAt) { this.firstSeenAt = firstSeenAt; }
    public LocalDateTime getLastSeenAt() { return lastSeenAt; }
    public void setLastSeenAt(LocalDateTime lastSeenAt) { this.lastSeenAt = lastSeenAt; }
    public String getMetadataJson() { return metadataJson; }
    public void setMetadataJson(String metadataJson) { this.metadataJson = metadataJson; }
}
