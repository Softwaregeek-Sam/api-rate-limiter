package dev.sumit.apiratelimiter.cleanup;

import dev.sumit.apiratelimiter.config.RateLimiterConfig;
import dev.sumit.apiratelimiter.core.Bucket;
import dev.sumit.apiratelimiter.core.RateLimiter;
import dev.sumit.apiratelimiter.core.TokenBucketRateLimiter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

public class BucketCleanupTask {
    private static final Logger log = LoggerFactory.getLogger(BucketCleanupTask.class);

    private final TokenBucketRateLimiter rateLimiter;
    private final RateLimiterConfig config;

    public BucketCleanupTask(TokenBucketRateLimiter rateLimiter, RateLimiterConfig config) {
        this.rateLimiter = rateLimiter;
        this.config = config;
    }

    public void evictedIdleBuckets(){
        long idleThresholdNanos = (long)(config.getIdleThresholdSeconds() * 1_000_000_000L);
        long nowNanos = System.nanoTime();

        ConcurrentHashMap<String, Bucket> buckets = rateLimiter.getBuckets();

        AtomicInteger evictedCount = new AtomicInteger(0);
        int totalBefore = buckets.size();

       buckets.forEach((String key, Bucket bucket) -> {
          if(nowNanos -bucket.getLastAccessNanos() <= idleThresholdNanos){
               return;
          }

          synchronized (bucket){
               if(nowNanos - bucket.getLastAccessNanos() <= idleThresholdNanos){
                    return;
               }

               if(bucket.isRemoved()){
                   return;

               }

               bucket.setRemoved(true);

               boolean removed = buckets.remove(key, bucket);

               if(removed){
                   evictedCount.incrementAndGet();
                   log.debug("Evicted idle bucket for key='{}' (idle > {}s)",
                           key, config.getIdleThresholdSeconds());
               }
          }
       });


       int evicted = evictedCount.get();
       if(evicted >0 || log.isDebugEnabled()){
           log.info("Cleanup complete: evicted={}, remaining={}, scanned={}",
                   evicted,
                   buckets.size(),
                   totalBefore);
       }
    }



}
