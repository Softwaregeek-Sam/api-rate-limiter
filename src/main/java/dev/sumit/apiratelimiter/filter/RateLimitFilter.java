package dev.sumit.apiratelimiter.filter;

import dev.sumit.apiratelimiter.core.RateLimitResult;
import dev.sumit.apiratelimiter.core.RateLimiter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class RateLimitFilter extends OncePerRequestFilter {
    private static final Logger log = LoggerFactory.getLogger(RateLimitFilter.class);
    public static final String CLIENT_ID_HEADER = "X-Client-Id";
    public static final String HEADER_REMAINING = "X-RateLimit-Remaining";
    public static final String HEADER_RETRY_AFTER = "Retry-After";

    private final RateLimiter rateLimiter;

    public RateLimitFilter(RateLimiter rateLimiter) {
        this.rateLimiter = rateLimiter;
    }


    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {

        String clientId = request.getHeader(CLIENT_ID_HEADER);

        if(clientId == null || clientId.isBlank()){
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            response.setContentType("application/json");
            response.getWriter().write(
                    "{\"error\":\"Missing required header: " + CLIENT_ID_HEADER + "\"}"
            );

            log.debug("Rejected request: missing {} header from {}",
             CLIENT_ID_HEADER, request.getRemoteAddr());
            return;

        }

        RateLimitResult result;

        try{
            result = rateLimiter.tryAcquire(clientId);
        }catch (Exception e){
            log.error("Rate limiter internal error for clientId='{}': {}",
                    clientId, e.getMessage(), e);

            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            response.setContentType("application/json");
            response.getWriter().write(
                    "{\"error\":\"Internal rate limiter error\"}"
            );
            return;
        }

        response.setHeader(HEADER_REMAINING,
                String.valueOf(result.remainingTokens()));

        if(!result.allowed()){
            long retryAfterSeconds  = (long) Math.ceil(result.retryAfterMillis()/ 1000.0);

            response.setStatus(429);
            response.setHeader(HEADER_RETRY_AFTER, String.valueOf(retryAfterSeconds));
            response.setContentType("application/json");
            response.getWriter().write(String.format(
                    "{\"error\":\"Rate limit exceeded\"," +
                            "\"retryAfterSeconds\":%d," +
                            "\"remainingTokens\":%d}",
                    retryAfterSeconds,
                    result.remainingTokens()
            ));

            log.debug("Rate limited clientId='{}': retryAfter={}s",
                    clientId, retryAfterSeconds);
            return;

        }
        filterChain.doFilter(request, response);

    }
}
