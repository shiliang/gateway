local key, intervalPerPermit, refillTime = KEYS[1], tonumber(ARGV[1]), tonumber(ARGV[2])
local limit, interval = tonumber(ARGV[3]), tonumber(ARGV[4])
--bucket[1] lastRefillTime bucket[2] lastRefillTime的值
--bucket[3] tokensRemaining bucket[4] tokensRemaining的值
local bucket = redis.call('hgetall', key)

local currentTokens = 0

-- table.maxn(bucket)数组的最大索引下标从1开始 不存在key值为正数的值 = 0, 即bucket不存在
if bucket == nil then
    -- 设置令牌数为最大,refillTime添加令牌的时间
    currentTokens = limit
    redis.call('hset', key, 'lastRefillTime', refillTime)
elseif table.maxn(bucket) == 4 then
    --桶存在，先计算需要添加的令牌
    local lastRefillTime, tokensRemaining = tonumber(bucket[2]), tonumber(bucket[4])

    if refillTime > lastRefillTime then
        --计算差值
        --1.过了整个周期，需要补到最大值
        --2.如果到了至少补充一个的周期，那么需要补充部分，否则不补充
        local intervalSinceLast = refillTime - lastRefillTime
        if intervalSinceLast > interval then
            currentTokens = limit
            redis.call('hset', key, 'lastRefillTime', refillTime)
        else
            --需要添加的数量
            local grantedTokens = math.floor(intervalSinceLast / intervalPerPermit)
            if grantedTokens > 0 then
                local padMillis = math.fmod(intervalSinceLast, intervalPerPermit)

                --把余数的时间还回去
                redis.call('hset', key, 'lastRefillTime', refillTime - padMillis)

            end
            currentTokens = math.min(grantedTokens + tokensRemaining, limit)

        end
    else
        -- 有别的线程已添加过
        currentTokens = tokensRemaining


    end

end

if currentTokens == 0 then
    --无令牌可用
    redis.call('hset', key, 'tokensRemaining', currentTokens)
    return "0"
else
    redis.call('hset', key, 'tokensRemaining', currentTokens - 1)
    return "1"

end


