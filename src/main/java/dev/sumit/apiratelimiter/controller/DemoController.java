package dev.sumit.apiratelimiter.controller;

import dev.sumit.apiratelimiter.core.TokenBucketRateLimiter;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;
import java.util.Map;

@RestController
public class DemoController {

    private final TokenBucketRateLimiter rateLimiter;

    public DemoController(TokenBucketRateLimiter rateLimiter) {
        this.rateLimiter = rateLimiter;
    }

    /**
     * Primary test endpoint.
     * Hit this repeatedly to observe rate limiting in action.
     * Watch X-RateLimit-Remaining decrease, then see 429 responses.
     */
    @GetMapping("/api/hello")
    public ResponseEntity<Map<String, Object>> hello() {
        return ResponseEntity.ok(Map.of(
                "message", "Request allowed",
                "timestamp", Instant.now().toString(),
                "status", "success"
        ));
    }

    /**
     * Health check endpoint — also rate limited.
     * Useful for seeing how rate limits apply across different paths.
     */
    @GetMapping("/api/ping")
    public ResponseEntity<Map<String, String>> ping() {
        return ResponseEntity.ok(Map.of(
                "status", "ok",
                "timestamp", Instant.now().toString()
        ));
    }

    /**
     * Observability endpoint — shows current bucket count.
     * Useful for monitoring memory growth during load tests.
     * In production, expose this via Actuator with proper auth.
     */
    @GetMapping("/api/admin/stats")
    public ResponseEntity<Map<String, Object>> stats() {
        return ResponseEntity.ok(Map.of(
                "activeBuckets", rateLimiter.getBucketCount(),
                "timestamp", Instant.now().toString()
        ));
    }
}