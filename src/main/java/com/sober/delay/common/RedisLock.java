package com.sober.delay.common;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import java.util.concurrent.TimeUnit;

/**
 * @author liweigao
 * @date 2018/12/10 下午7:40
 */
public class RedisLock {

    RedisTemplate redisTemplate;

    ValueOperations<String, String> valueOperations;
    /**
     * Lock key path.
     */
    String lockKey;

    /**
     * 锁超时时间
     * 锁超时，防止线程在入锁以后，无限的执行等待
     */
    int expireMsecs = 60 * 1000;


    private boolean locked = false;

    public RedisLock(RedisTemplate redisTemplate, String lockKey) {
        this.redisTemplate = redisTemplate;
        this.valueOperations = redisTemplate.opsForValue();
        this.lockKey = lockKey;
    }

    public RedisLock(RedisTemplate redisTemplate, String lockKey, int expireMsecs) {
        this(redisTemplate, lockKey);
        this.expireMsecs = expireMsecs;
    }

    public RedisLock(String lockKey) {
        this(null, lockKey);
    }

    public RedisLock(String lockKey, int expireMsecs) {
        this(null, lockKey, expireMsecs);
    }

    /**
     * @return lock key
     */
    public String getLockKey() {
        return lockKey;
    }

    /**
     * 执行锁
     */
    public synchronized boolean acquire() throws InterruptedException {
        return acquire(valueOperations);
    }


    /**
     * 获取锁不参与轮询逻辑
     *
     * @param expiresTime 过期时间
     * @return true / false
     */
    public boolean acquire(long expiresTime) {
        long expires = System.currentTimeMillis() + expireMsecs + 1;
        String expiresStr = String.valueOf(expires);
        //锁到期时间
        if (valueOperations.setIfAbsent(lockKey, expiresStr)) {
            redisTemplate.expire(lockKey, expiresTime, TimeUnit.SECONDS);
            locked = true;
            return true;
        }
        return false;
    }

    /**
     * 获取执行权
     */
    private synchronized boolean acquire(ValueOperations<String, String> valueOperations) throws InterruptedException {

        long expires = System.currentTimeMillis() + expireMsecs + 1;
        String expiresStr = String.valueOf(expires);
        //锁到期时间
        if (valueOperations.setIfAbsent(lockKey, expiresStr)) {
            locked = true;
            return true;
        }

        //轮询解锁
        while (!isRelease()) {

            String currentValueStr = valueOperations.get(lockKey);
            //redis里的时间
            if (currentValueStr != null && Long.parseLong(currentValueStr) < System.currentTimeMillis()) {
                //判断是否为空，不为空的情况下，如果被其他线程设置了值，则第二个条件判断是过不去的
                // lock is expired
                expiresStr = String.valueOf(System.currentTimeMillis() + expireMsecs + 1);
                String oldValueStr = valueOperations.getAndSet(lockKey, expiresStr);
                //获取上一个锁到期时间，并设置现在的锁到期时间，
                //只有一个线程才能获取上一个线上的设置时间，因为jedis.getSet是同步的
                if (oldValueStr != null && oldValueStr.equals(currentValueStr)) {
                    //如过这个时候，多个线程恰好都到了这里，但是只有一个线程的设置值和当前值相同，他才有权利获取锁
                    // lock acquired
                    locked = true;
                    return true;
                }
            }
            Thread.sleep(100);
        }
        locked = false;
        return false;
    }

    /**
     * 判断锁的状态 true 为有锁在执行 并且不是死锁
     */
    public boolean getLockIsAvailable() {
        String currentValueStr = valueOperations.get(lockKey);
        if (currentValueStr != null && Long.parseLong(currentValueStr) > System.currentTimeMillis()) {
            return false;
        } else {
            return true;
        }
    }

    /**
     * 释放锁
     */
    public synchronized void release() {
        release(redisTemplate);
    }

    private synchronized void release(RedisTemplate redisTemplate) {
        if (locked) {

            redisTemplate.delete(lockKey);
            locked = false;
        }
    }

    public boolean isRelease() {
        return valueOperations.get(lockKey) == null;
    }
}
