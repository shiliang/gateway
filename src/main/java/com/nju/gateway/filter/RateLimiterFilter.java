package com.nju.gateway.filter;

import com.google.common.util.concurrent.RateLimiter;
import com.netflix.zuul.ZuulFilter;
import com.netflix.zuul.exception.ZuulException;
import com.nju.gateway.exception.RateLimiteException;
import org.springframework.stereotype.Component;

import static org.springframework.cloud.netflix.zuul.filters.support.FilterConstants.PRE_TYPE;
import static org.springframework.cloud.netflix.zuul.filters.support.FilterConstants.SERVLET_DETECTION_FILTER_ORDER;
/*
    令牌限流
 */
@Component
public class RateLimiterFilter extends ZuulFilter {

    private  final RateLimiter RATE_LIMITER = RateLimiter.create(100);  //令牌桶
    @Override
    public String filterType() {
        return PRE_TYPE;
    }

    @Override
    public int filterOrder() {
        return SERVLET_DETECTION_FILTER_ORDER - 1;
    }

    @Override
    public boolean shouldFilter() {
        return true;
    }

    @Override
    public Object run() throws ZuulException {
        if (RATE_LIMITER.tryAcquire()) {
            throw new RateLimiteException();
        }
        return null;
    }
}
