package com.study.limiter;

import java.io.Serializable;
import java.util.concurrent.TimeUnit;

/**
 * 漏桶法
 * 水龙头可以通过松紧来控制水的速率,下方有一个储蓄桶来保存当前的水.储蓄桶底部有一个出口,内部的水会以恒定的速率从出口漏掉.
 * 如果储蓄桶满了以后,再进来的水会全部溢出.只有当出水速率和进水速率相同时,储蓄桶才会在不漏水的前提下达到最大的吞吐量.
 * 我们把水比作请求,水龙头就是客户端.请求产生的速率是不固定的,但处理请求的速率是恒定的.
 * 仔细一想,储蓄桶能够把不是恒定速率的请求转为恒定速率的请求,和消息队列一样,具有削风削谷的作用
 *
 * 缺点:
 * 使用漏桶法去做限流,在业务平稳期其实已经够用了.但是在业务高峰期,我们又希望动态的去调整处理请求的速率,而不是一成不变的速率.
 * 我们大可以动态的去改变参数leakRate的值,不过在计算剩余水量的时候,将会十分复杂.
 * 因此,如果要考虑到对突发流量的控制,就不太推荐漏桶法了.
 *
 * Program Name: limiter
 * Created by yanlp on 2022-03-23
 *
 * @author yanlp
 * @version 1.0
 */
public class LeakyBucketLimiter implements Serializable {
    private static final long serialVersionUID = 8371370549444885883L;

    /**
     * 上次请求到来的时间
     */
    private long preTime = System.currentTimeMillis();

    /**
     * 漏水流速(n/s)
     */
    private int leakRate;

    /**
     * 桶的容量
     */
    private int capacity;

    /**
     * 当前桶里水量
     */
    private int water;

    public LeakyBucketLimiter(int leakyRate, int capacity) {
        this.leakRate = leakyRate;
        this.capacity = capacity;
    }

    /**
     * 限制
     *
     * @return true:依然可以接收请求;false:被限流
     */
    public boolean limit() {
        long now = System.currentTimeMillis();
        // 期间会漏水,计算剩余的水量
        water = Math.max(0, water - (int) ((now - preTime) / 1000) * leakRate);
        preTime = now;
        // 桶里的水未满
        if (water + 1 <= capacity) {
            water++;
            return true;
        }
        // 说明桶里的水满了,需要进行限流
        return false;
    }

    public static void main(String[] args) {
        LeakyBucketLimiter limiter = new LeakyBucketLimiter(1, 5);
        for (int i = 0; i < 10; i++) {
            boolean limit = limiter.limit();
            try {
                TimeUnit.MILLISECONDS.sleep(100);} catch (InterruptedException e) {e.printStackTrace();}
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

    public int getLeakRate() {
        return leakRate;
    }

    public void setLeakRate(int leakRate) {
        this.leakRate = leakRate;
    }

    public int getCapacity() {
        return capacity;
    }

    public void setCapacity(int capacity) {
        this.capacity = capacity;
    }

    public int getWater() {
        return water;
    }

    public void setWater(int water) {
        this.water = water;
    }
}
