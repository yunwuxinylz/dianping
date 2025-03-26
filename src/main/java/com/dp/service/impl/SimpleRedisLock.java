package com.dp.service.impl;

import java.util.concurrent.TimeUnit;

import org.springframework.data.redis.core.StringRedisTemplate;

import com.dp.service.ILock;

import cn.hutool.core.util.BooleanUtil;

public class SimpleRedisLock implements ILock {

    private String name;
    private StringRedisTemplate stringRedisTemplate;

    private static final String KEY_PREFIX = "lock:";

    public SimpleRedisLock(String name, StringRedisTemplate stringRedisTemplate) {
        this.name = name;
        this.stringRedisTemplate = stringRedisTemplate;
    }

    @Override
    public boolean tryLock(long timeoutSec) {
        String threadId = Thread.currentThread().getId() + "";
        Boolean success = stringRedisTemplate.opsForValue().setIfAbsent(KEY_PREFIX + name, threadId, timeoutSec,
                TimeUnit.SECONDS);
        return BooleanUtil.isTrue(success);

    }

    @Override
    public void unlock() {

        stringRedisTemplate.delete(KEY_PREFIX + name);
    }

}
