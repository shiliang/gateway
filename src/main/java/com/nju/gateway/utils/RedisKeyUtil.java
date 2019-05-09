package com.nju.gateway.utils;

public class RedisKeyUtil {
    private static String SPLIT = ":";
    private static String BIZ_TOKENLIMIT = "TOKENLIMIT";

    public static String getBizTokenlimitKey(String userId) {
        return BIZ_TOKENLIMIT + SPLIT + String.valueOf(userId);
    }
}
