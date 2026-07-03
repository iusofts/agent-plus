package com.iusofts.basic.redis;

import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.stereotype.Component;

import jakarta.annotation.Resource;
import java.util.concurrent.TimeUnit;

/**
 * Redis分布式锁（Redisson实现，替换原RedisTemplate写法）
 */
@Slf4j
@Component
public class RedisLock {

    @Resource
    private RedissonClient redissonClient;

    /**
     * 获取锁（与原方法签名完全一致）
     * 原逻辑：立即尝试获取锁，获取不到直接返回false，锁过期后自动释放
     *
     * @param key        锁名称
     * @param expireTime 锁过期时间
     * @param timeUnit   时间单位
     * @return 是否获取成功
     */
    public boolean tryLock(String key, long expireTime, TimeUnit timeUnit) {
        if (key == null || expireTime <= 0) {
            log.warn("获取锁失败：key为空或过期时间非法，key={}", key);
            return false;
        }
        try {
            // 获取Redisson分布式锁（默认公平锁，可根据需要改为getFairLock/getReadWriteLock）
            RLock lock = redissonClient.getLock(key);
            // 核心：立即尝试获取锁（等待时间0），获取成功则持有锁至expireTime过期
            // 替代原RedisTemplate的setIfAbsent逻辑，更安全（自动防死锁）
            return lock.tryLock(0, expireTime, timeUnit);
        } catch (InterruptedException e) {
            // 捕获中断异常，恢复线程中断状态
            Thread.currentThread().interrupt();
            log.error("获取锁异常（线程中断），key={}", key, e);
            return false;
        } catch (Exception e) {
            log.error("获取锁异常，key={}", key, e);
            return false;
        }
    }

    /**
     * 获取锁
     * 原逻辑：循环等待指定时间，直到获取锁或超时
     *
     * @param key        锁名称
     * @param second     锁过期时间（秒）
     * @param waitSecond 最大等待时间（秒）
     * @return 是否获取成功
     */
    public boolean tryLock(String key, long second, long waitSecond) {
        if (key == null || second <= 0 || waitSecond < 0) {
            log.warn("获取锁失败：参数非法，key={}, second={}, waitSecond={}", key, second, waitSecond);
            return false;
        }
        try {
            RLock lock = redissonClient.getLock(key);
            // 核心：最多等待waitSecond秒，获取成功后持有锁second秒，自动释放
            // 替代原手动轮询+Thread.sleep的逻辑，更高效、无空轮询损耗
            return lock.tryLock(waitSecond, second, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.error("获取锁异常（线程中断），key={}", key, e);
            return false;
        } catch (Exception e) {
            log.error("获取锁异常，key={}", key, e);
            return false;
        }
    }

    /**
     * 释放锁
     *
     * @param key 锁名称
     */
    public void releaseLock(String key) {
        if (key == null) {
            log.warn("释放锁失败：key为空");
            return;
        }
        try {
            RLock lock = redissonClient.getLock(key);
            // 仅当当前线程持有该锁时才释放，避免误删其他线程的锁
            if (lock.isHeldByCurrentThread()) {
                lock.unlock();
                log.debug("释放锁成功，key={}", key);
            } else {
                log.warn("释放锁失败：当前线程未持有该锁，key={}", key);
            }
        } catch (Exception e) {
            log.error("释放锁异常，key={}", key, e);
        }
    }

}