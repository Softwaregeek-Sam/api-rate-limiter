package dev.sumit.apiratelimiter.core;

public record RateLimitResult(
        boolean allowed,
        long remainingTokens,
        long retryAfterMillis
) {


    public static RateLimitResult allowed(double remainingTokens) {
        return new RateLimitResult(
                true,
                (long) Math.floor(remainingTokens),
                0L
        );
    }

    public static RateLimitResult denied(double currentTokens,
                                         double refillRatePerSecond) {
        double tokensNeeded = 1.0 - currentTokens;
        double secondsToWait = tokensNeeded / refillRatePerSecond;
        long retryAfterMs = (long) Math.ceil(secondsToWait * 1000);
        return new RateLimitResult(
                false,
                0L,
                Math.max(retryAfterMs, 0L)
        );
    }

}
