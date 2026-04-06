package dev.sumit.apiratelimiter.config;

import jakarta.annotation.PostConstruct;
import jakarta.validation.constraints.Positive;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@ConfigurationProperties(prefix = "ratelimiter")
@Validated
public class RateLimiterConfig {

    @Positive
    private long capacity = 10;


    @Positive
    private double refillRatePerSecond = 2.0;


    @Positive
    private long idleThresholdSeconds = 300;


    @Positive
    private long cleanupIntervalMs = 60_000;


    @PostConstruct
    public void validateInvariants() {
        double minimumIdleSeconds = (double) capacity / refillRatePerSecond;
        if (idleThresholdSeconds < minimumIdleSeconds) {
            throw new IllegalStateException(String.format(
                    "Invalid rate limiter configuration: " +
                            "idleThresholdSeconds (%d) must be >= capacity / refillRatePerSecond (%.2f). " +
                            "Current values: capacity=%d, refillRatePerSecond=%.2f, minimum idle=%.2fs. " +
                            "Increase idleThresholdSeconds or decrease capacity/refillRatePerSecond.",
                    idleThresholdSeconds,
                    minimumIdleSeconds,
                    capacity,
                    refillRatePerSecond,
                    minimumIdleSeconds
            ));
        }
    }




    public long getCapacity() {
        return capacity;
    }

    public double getRefillRatePerSecond() {
        return refillRatePerSecond;
    }

    public long getIdleThresholdSeconds() {
        return idleThresholdSeconds;
    }

    public long getCleanupIntervalMs() {
        return cleanupIntervalMs;
    }

    // ── Setters (required by Spring Boot configuration binding) ──────────────

    public void setCapacity(long capacity) {
        this.capacity = capacity;
    }

    public void setRefillRatePerSecond(double refillRatePerSecond) {
        this.refillRatePerSecond = refillRatePerSecond;
    }

    public void setIdleThresholdSeconds(long idleThresholdSeconds) {
        this.idleThresholdSeconds = idleThresholdSeconds;
    }

    public void setCleanupIntervalMs(long cleanupIntervalMs) {
        this.cleanupIntervalMs = cleanupIntervalMs;
    }

}
