package com.study.limiter;

import java.io.Serializable;
import java.util.LinkedList;

/**
 * 滑动窗口法进行限流
 * 滑动窗口法可以解决计数器在固定窗口下无法应对突发流量的问题.
 * 计数器方法是以第一个请求为窗口开始时期,并向后截取intervalWindow长度,只有当窗口时间流逝完,才开辟新的窗口.
 * 滑动窗口法是以每个请求为窗口结束期,向前截取intervalWindow长度,检查该范围内的总和,相当于会为每个请求开辟一个新窗口.
 *
 * 当然,也可以使用Redis的List或Zset实现,大致步骤与以上demo类似.
 *
 * 缺点:
 * 在滑动窗口法中,因为要推导窗口的开始期,所以需要记录每个请求的开始时间,会额外占用一些内存.
 * 此外,此算法会频繁的removeFirst和addLast,在选择错误的数据结构下(如数组),可能会造成很大的移动开销.
 *
 * Program Name: limiter
 * Created by yanlp on 2022-03-23
 *
 * @author yanlp
 * @version 1.0
 */
public class SlidingWindowLimiter implements Serializable {
    private static final long serialVersionUID = -6780094192976841690L;

    /**
     * 窗口间隔时间(毫秒)
     */
    private long intervalWindow;
    /**
     * 当前窗口允许通过最大请求个数
     */
    private int max;
    /**
     * 限流容器
     * 队列尾部保存最新通过的请求时间
     */
    private LinkedList<Long> list = new LinkedList<>();

    public SlidingWindowLimiter(long intervalWindow, int max) {
        this.intervalWindow = intervalWindow;
        this.max = max;
    }

    /**
     * 限流
     *
     * @return true:依然可以接收请求;false:被限流
     */
    public boolean limit() {
        long now = System.currentTimeMillis();
        // 队列未满,说明当前窗口还可以进行接收请求
        if (list.size() < max) {
            list.addLast(now);
            return true;
        }
        // 队列已满,获取该窗口第一个请求时间
        Long firstReqTime = list.getFirst();
        if (now - firstReqTime <= intervalWindow) {
            // 说明当前请求与第一个请求处在同一个窗口,进行限流
            return false;
        }
        // 当前请求与第一个请求不在同一个窗口期
        list.removeFirst();
        list.addLast(now);
        return true;
    }


    public static void main(String[] args) {
        SlidingWindowLimiter limiter = new SlidingWindowLimiter(60000, 100);
        for (int i = 0; i < 200; i++) {
            boolean limit = limiter.limit();
            if (!limit) {
                System.out.println("该请求不能被通过: " + i);
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

    public LinkedList<Long> getList() {
        return list;
    }

    public void setList(LinkedList<Long> list) {
        this.list = list;
    }
}
