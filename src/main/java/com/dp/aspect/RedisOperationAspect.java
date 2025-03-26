package com.dp.aspect;

import io.micrometer.core.instrument.MeterRegistry;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;

@Aspect
@Component
public class RedisOperationAspect {
    
    private final MeterRegistry registry;
    
    public RedisOperationAspect(MeterRegistry registry) {
        this.registry = registry;
    }
    
    @Around("execution(* org.springframework.data.redis.core.RedisTemplate.*(..))")
    public Object monitorRedisOperation(ProceedingJoinPoint joinPoint) throws Throwable {
        String methodName = joinPoint.getSignature().getName();
        String operationType = getOperationType(methodName);
        long startTime = System.currentTimeMillis();
        boolean isSuccess = true;
        
        try {
            Object result = joinPoint.proceed();
            // 记录命中率
            if (result != null) {
                registry.counter("redis.hits", 
                    "operation", operationType).increment();
            } else {
                registry.counter("redis.misses", 
                    "operation", operationType).increment();
            }
            return result;
        } catch (Exception e) {
            isSuccess = false;
            registry.counter("redis.errors", 
                "operation", operationType,
                "error", e.getClass().getSimpleName()).increment();
            throw e;
        } finally {
            long duration = System.currentTimeMillis() - startTime;
            registry.timer("redis.operation.duration",
                "operation", operationType,
                "success", String.valueOf(isSuccess))
                .record(duration, java.util.concurrent.TimeUnit.MILLISECONDS);
        }
    }

    private String getOperationType(String methodName) {
        if (methodName.startsWith("opsFor")) {
            return methodName.substring(6).toLowerCase();
        }
        return methodName;
    }
}