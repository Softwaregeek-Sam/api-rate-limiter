package dev.sumit.apiratelimiter.core;


public class Bucket {

    volatile double tokens ;
    volatile double lastRefillNanos;
    volatile double lastAccessNanos;

    volatile boolean removed;


    public Bucket(double capacity) {
        this.tokens = capacity;
        long now = System.nanoTime();
        this.lastRefillNanos = now;
        this.lastAccessNanos = now;
        this.removed = false;
    }

    public double getTokens() {
        return tokens;
    }

    public void setTokens(double tokens) {
        this.tokens = tokens;
    }

    public double getLastRefillNanos() {
        return lastRefillNanos;
    }

    public void setLastRefillNanos(double lastRefillNanos) {
        this.lastRefillNanos = lastRefillNanos;
    }

    public double getLastAccessNanos() {
        return lastAccessNanos;
    }

    public void setLastAccessNanos(double lastAccessNanos) {
        this.lastAccessNanos = lastAccessNanos;
    }

    public boolean isRemoved() {
        return removed;
    }

    public void setRemoved(boolean removed) {
        this.removed = removed;
    }
}
