package dev.sumit.apiratelimiter.core;

import dev.sumit.apiratelimiter.config.RateLimiterConfig;
import org.springframework.stereotype.Component;

import java.util.concurrent.ConcurrentHashMap;

@Component
public class TokenBucketRateLimiter implements RateLimiter{
    private final ConcurrentHashMap<String, Bucket> buckets=
            new ConcurrentHashMap<>();

    private final RateLimiterConfig config;

    public TokenBucketRateLimiter(RateLimiterConfig config) {
        this.config = config;
    }

    @Override
    public RateLimitResult tryAcquire(String key) {
        while(true){
            Bucket bucket = buckets.computeIfAbsent(key,
                    k-> new Bucket(config.getCapacity()));

            synchronized (bucket){
                if(bucket.removed){
                    continue;
                }

                long nowNanos = System.nanoTime();
                double elapsedSeconds = (double)(nowNanos - bucket.lastRefillNanos)/ 1_000_000_000.0;
                double tokensToAdd = elapsedSeconds * config.getRefillRatePerSecond();

                bucket.tokens = Math.min(config.getCapacity(), bucket.tokens + tokensToAdd);
                bucket.lastRefillNanos = nowNanos;
                bucket.lastAccessNanos = nowNanos;

                if(bucket.tokens >= 1.0){
                    bucket.tokens -= 1.0;
                    return  RateLimitResult.allowed(bucket.tokens);
                }else{
                    return RateLimitResult.denied(bucket.tokens, config.getRefillRatePerSecond());

                }

            }
        }



    }

   public ConcurrentHashMap<String,Bucket> getBuckets(){return buckets;}
    public int getBucketCount(){return buckets.size();}
}
