package com.study.limiter;

import java.io.Serializable;

/**
 * 计数器方式进行限流
 * 计数器法,也称固定窗口法.可以控制在固定时间范围内,允许通过的最大请求数量.
 * 例如,我们设定时间间隔窗口intervalWindow为1分钟,允许通过的最大数max为100.
 * 当第一个请求到来时,我们记录下当前窗口内的第一个请求时间firstReqTime,那么之后的请求到来时,先判断是否进入下一个窗口.
 * 如果进入,则直接重置firstReqTime为当前时间,该请求通过;如果没在,再判断当前窗口请求个数加1后是否超过max.(demo如下,并没有考虑线程安全)
 *
 * 计数器法非常简单粗暴,以下demo只适用于单机模式.
 * 如果需要进行分布式限流,可以使用Redis,接口名称作为key,max作为value,intervalWindow作为过期时间,
 * 当请求过来时,如果请求key不存在,则代表该接口第一次被调用或者已经进入到下一个窗口,value赋值为max-1,并允许请求通过.
 * 如果key存在,再判断value是否大于0,如果大于0,代表请求通过,否则进行限流.
 * 使用Redis进行分布式限流,需要注意保证代码的原子性,可以直接使用lua脚本.
 *
 * 缺点:
 * 无法应对突发的流量,因为计数器法是固定窗口的.
 * 例如第一个请求是10:00:00到来,那么第一个时间窗口为10:00:00~10:01:00,之后在10:00:59时,突然来了59个请求,又在下一个窗口的10:01:01来了100个请求,也就是说在短短的几秒内,共有199个请求到来,
 * 可能会瞬间压垮我们的应用.
 *
 * Program Name: limiter
 * Created by yanlp on 2022-03-23
 *
 * @author yanlp
 * @version 1.0
 */
public class CountLimiter implements Serializable {

    private static final long serialVersionUID = 2272191895553120884L;

    /**
     * 时间间隔窗口(毫秒)
     */
    private long intervalWindow;

    /**
     * 该窗口内最大请求数
     */
    private int max;

    /**
     * 当前窗口已存在请求个数
     */
    private int count;

    /**
     * 当前窗口第一个请求时间
     */
    private long firstReqTime = System.currentTimeMillis();

    public CountLimiter(long intervalWindow, int max) {
        this.intervalWindow = intervalWindow;
        this.max = max;
    }

    /**
     * 限流
     * @return true:依然可以接收请求;false:被限流
     */
    public boolean limit() {
        long now = System.currentTimeMillis();
        if (now > firstReqTime + intervalWindow) {
            firstReqTime = now;
            count = 1;
            return true;
        }
        if (count + 1 <= max) {
            count++;
            return true;
        }
        return false;
    }


    public static void main(String[] args) {
        CountLimiter limiter = new CountLimiter(60000, 100);
        for (int i = 0; i < 200; i++) {
            boolean limit = limiter.limit();
            if (!limit) {
                System.out.println("该请求不能被通过: "+ i);
            }
        }


    }


    public long getIntervalWindow() {
        return intervalWindow;
    }

    public void setIntervalWindow(long intervalWindow) {
        this.intervalWindow = intervalWindow;
    }

    public int getMax() {
        return max;
    }

    public void setMax(int max) {
        this.max = max;
    }

    public int getCount() {
        return count;
    }

    public void setCount(int count) {
        this.count = count;
    }

    public long getFirstReqTime() {
        return firstReqTime;
    }

    public void setFirstReqTime(long firstReqTime) {
        this.firstReqTime = firstReqTime;
    }





}
