package com.example.quartz.redis;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

@Slf4j
@Aspect
@Component
@RequiredArgsConstructor
public class RedisLockAspect {

    private final RedissonClient redissonClient;

    @Around("@annotation(distributedLock)")
    public Object lock(ProceedingJoinPoint joinPoint, DistributedLock distributedLock) throws Throwable {
        String lockKey = distributedLock.key();
        RLock lock = redissonClient.getLock(lockKey);

        if (tryGetLock(lock, distributedLock)) {
            try {
                log.info("Redis 락 획득: 키 = {}", lockKey);
                return joinPoint.proceed();
            } finally {
                releaseLock(lock, lockKey);
            }
        }
        throw new IllegalStateException("Redis 락을 획득하지 못했습니다.");
    }

    private boolean tryGetLock(RLock lock, DistributedLock distributedLock) throws InterruptedException {
        return lock.tryLock(distributedLock.waitTime(), distributedLock.leaseTime(), TimeUnit.SECONDS);
    }

    private void releaseLock(RLock lock, String lockKey) {
        lock.unlock();
        log.info("Redis 락 해제: 키 = {}", lockKey);
    }

}
