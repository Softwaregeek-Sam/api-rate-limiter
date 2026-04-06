package dev.sumit.apiratelimiter.core;

public interface RateLimiter {
   RateLimitResult tryAcquire(String key);
}
