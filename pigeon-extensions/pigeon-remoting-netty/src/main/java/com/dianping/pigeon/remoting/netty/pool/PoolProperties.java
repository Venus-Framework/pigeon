package com.dianping.pigeon.remoting.netty.pool;

/**
 * @author qi.yin
 *         2016/07/21  上午11:09.
 */
public class PoolProperties {

    public static final int DEFAULT_INITIAL_SIZE = 1;

//    public static final int DEFAULT_MAX_IDLE = 5;

    public static final int DEFAULT_MIN_IDLE = 0;

    public static final int DEFAULT_MAX_ACTIVE = 5;

    public static final int DEFAULT_MAX_WAIT = 3000;

    private static final int DEFAULT_TIME_BETWEEN_EVICTION_RUNS_MILLIS = 60 * 1000;

    private static final int DEFAULT_MIN_EVICTABLE_IDLE_TIME_MILLIS = 120 * 1000;

    private int initialSize;

//    private int maxIdle;

    private int minIdle;

    private int maxActive;

    private int maxWait;

    private int timeBetweenEvictionRunsMillis;

    private int minEvictableIdleTimeMillis;


    public PoolProperties() {
        this(DEFAULT_INITIAL_SIZE,
//                DEFAULT_MAX_IDLE,
                DEFAULT_MIN_IDLE,
                DEFAULT_MAX_ACTIVE,
                DEFAULT_MAX_WAIT,
                DEFAULT_TIME_BETWEEN_EVICTION_RUNS_MILLIS,
                DEFAULT_MIN_EVICTABLE_IDLE_TIME_MILLIS);
    }

    public PoolProperties(int initialSize,
//                          int maxIdle,
                          int minIdle,
                          int maxActive,
                          int maxWait,
                          int timeBetweenEvictionRunsMillis,
                          int minEvictableIdleTimeMillis) {
        this.initialSize = initialSize;
//        this.maxIdle = maxIdle;
        this.minIdle = minIdle;
        this.maxActive = maxActive;
        this.maxWait = maxWait;
        this.timeBetweenEvictionRunsMillis = timeBetweenEvictionRunsMillis;
        this.minEvictableIdleTimeMillis=minEvictableIdleTimeMillis;
    }

    public int getInitialSize() {
        return initialSize;
    }

    public void setInitialSize(int initialSize) {
        this.initialSize = initialSize;
    }

//    public int getMaxIdle() {
//        return maxIdle;
//    }
//
//    public void setMaxIdle(int maxIdle) {
//        this.maxIdle = maxIdle;
//    }

    public int getMinIdle() {
        return minIdle;
    }

    public void setMinIdle(int minIdle) {
        this.minIdle = minIdle;
    }

    public int getMaxActive() {
        return maxActive;
    }

    public void setMaxActive(int maxActive) {
        this.maxActive = maxActive;
    }

    public int getMaxWait() {
        return maxWait;
    }

    public void setMaxWait(int maxWait) {
        this.maxWait = maxWait;
    }

    public int getMinEvictableIdleTimeMillis() {
        return minEvictableIdleTimeMillis;
    }

    public void setMinEvictableIdleTimeMillis(int minEvictableIdleTimeMillis) {
        this.minEvictableIdleTimeMillis = minEvictableIdleTimeMillis;
    }

    public int getTimeBetweenEvictionRunsMillis() {
        return timeBetweenEvictionRunsMillis;
    }

    public void setTimeBetweenEvictionRunsMillis(int timeBetweenEvictionRunsMillis) {
        this.timeBetweenEvictionRunsMillis = timeBetweenEvictionRunsMillis;
    }
}