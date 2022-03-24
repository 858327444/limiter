package com.study.limiter;

import java.io.Serializable;

/**
 * 令牌桶算法进行限流.
 * 首先有一个令牌桶,系统以固定的速率向桶中生成令牌.当桶满时,会丢弃生成的令牌.每有一个请求过来时,拿到令牌就可以执行,否则阻塞获取或者被直接丢弃.
 * 看的出来,令牌桶和漏桶的原理有些类似.
 * 漏桶是以恒定的速率出水,即处理请求的速率是恒定的.而令牌桶是以恒定的速率向桶中放入令牌,在桶中令牌用完之前,并不限制处理请求的速率.
 * 令牌桶的一个优势在于,可以允许短时间内的一次突发流量.但不会允许在短时间内的多次突发流量,因为令牌的填充也是需要时间的.
 *
 *
 * Program Name: limiter
 * Created by yanlp on 2022-03-23
 *
 * @author yanlp
 * @version 1.0
 */
public class TokenBucketLimiter implements Serializable {
    private static final long serialVersionUID = 3674691969286879698L;
    /**
     * 上次请求到来的时间
     */
    private long preTime = System.currentTimeMillis();

    /**
     * 令牌生成的速率 (n/s)
     */
    private int putRate;

    /**
     * 令牌桶容量
     */
    private int capacity;

    /**
     * 当前令牌桶的令牌个数
     */
    private int bucket;

    public TokenBucketLimiter(int putRate, int capacity) {
        this.putRate = putRate;
        this.capacity = capacity;
    }

    public boolean limit() {
        long now = System.currentTimeMillis();
        // 先放入令牌,再获取令牌
        bucket = Math.min(capacity, bucket + (int) ((now - preTime) / 1000) * putRate);
        preTime = now;
        if (bucket == 0) {
            return false;
        }
        bucket--;
        return true;
    }


    public static void main(String[] args) {
        TokenBucketLimiter limiter = new TokenBucketLimiter(1, 100);
        for (int i = 0; i < 200; i++) {
            boolean limit = limiter.limit();
            if (!limit) {
                System.out.println("该请求不能被通过: " + i);
            }
        }
    }


    public long getPreTime() {
        return preTime;
    }

    public void setPreTime(long preTime) {
        this.preTime = preTime;
    }

    public int getPutRate() {
        return putRate;
    }

    public void setPutRate(int putRate) {
        this.putRate = putRate;
    }

    public int getCapacity() {
        return capacity;
    }

    public void setCapacity(int capacity) {
        this.capacity = capacity;
    }

    public int getBucket() {
        return bucket;
    }

    public void setBucket(int bucket) {
        this.bucket = bucket;
    }
}
