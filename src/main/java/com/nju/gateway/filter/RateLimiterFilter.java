package com.nju.gateway.filter;

import com.google.common.io.CharStreams;
import com.google.common.util.concurrent.RateLimiter;
import com.netflix.zuul.ZuulFilter;
import com.netflix.zuul.context.RequestContext;
import com.netflix.zuul.exception.ZuulException;
import com.nju.gateway.config.RedisConfig;
import com.nju.gateway.exception.RateLimiteException;
import com.nju.gateway.utils.RedisKeyUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Component;
import redis.clients.jedis.Client;

import javax.servlet.http.HttpServletRequest;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.springframework.cloud.netflix.zuul.filters.support.FilterConstants.PRE_TYPE;
import static org.springframework.cloud.netflix.zuul.filters.support.FilterConstants.SERVLET_DETECTION_FILTER_ORDER;
/*
    令牌限流
 */
@Component
public class RateLimiterFilter extends ZuulFilter {

    @Autowired
    private RedisTemplate<String, String> redisTemplate;

    private long intervalInMills = 10000;  //一个周期的时间
    private long limit = 3;  //最大令牌数
    private double intervalPerPermit = intervalInMills * 1.0 / limit;  //往令牌桶里面加令牌的时间间隔

    //每秒限流100次请求,只适合单机版，如果是分布式?
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
        HttpServletRequest request = RequestContext.getCurrentContext().getRequest();
        String userId = request.getParameterValues("userId")[0];
        try {
            boolean res = access(userId);
        } catch (Exception e) {
            System.out.println(e);
            System.out.println("操作太频繁啦");
        }
        return null;
    }

    public boolean access(String userId) throws Exception {
        String key = RedisKeyUtil.getBizTokenlimitKey(userId);
        Reader reader = new InputStreamReader(Client.class.getClassLoader().getResourceAsStream("rateLimit.lua"));
        String luaScript = CharStreams.toString(reader);
        DefaultRedisScript<Long> redisScript = new DefaultRedisScript<>();
        redisScript.setScriptText(luaScript);
        redisScript.setResultType(Long.class);

        /**
         * keys[1] = key;
         * arvg[1] = intervalPerPermit;
         * arvg[2] = System.currentTimeMillis() 当前时间
         * avrg[3] = 总令牌数
         * avrg[4] = 一个周期的时间毫秒
         */
        redisTemplate.execute(redisScript, Collections.singletonList(key),
                intervalPerPermit,
                System.currentTimeMillis(),
                limit,
                intervalInMills);
        return true;
    }

}
