package com.dp.utils;



public class RedisConstants {
    public static final String LOGIN_CODE_KEY = "login:code:";
    public static final Long LOGIN_CODE_TTL = 2L;


    public static final String LOGIN_USER_KEY = "login:token:";
    public static final Long LOGIN_USER_TTL = 30L;


    public static final String CACHE_SHOP_KEY = "cache:shop:";
    // 30分钟加随机0到10分钟
    public static final long CACHE_SHOP_TTL = 30L + (long) (Math.random() * 600);
    
    public static final long CACHE_NULL_TTL = 2L;

    public static final String LOCK_SHOP_KEY = "lock:shop:";
    public static final String LOGIN_USER_ID_KEY = "login:userid:";

    public static final String SECKILL_STOCK_KEY = "seckill:stock:";
    
}
