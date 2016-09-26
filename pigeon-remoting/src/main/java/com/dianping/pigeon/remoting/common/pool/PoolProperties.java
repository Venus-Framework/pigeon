package com.dianping.pigeon.remoting.common.pool;

/**
 * @author qi.yin
 *         2016/07/21  上午11:09.
 */
public class PoolProperties {

    public static final int DEFAULT_INITIAL_SIZE = 1;

    public static final int DEFAULT_MAX_ACTIVE = 5;

    public static final int DEFAULT_MAX_WAIT = 3000;

    private int initialSize;

    private int maxActive;

    private int maxWait;


    public PoolProperties() {
        this(DEFAULT_INITIAL_SIZE,
                DEFAULT_MAX_ACTIVE,
                DEFAULT_MAX_WAIT);
    }

    public PoolProperties(int initialSize,
                          int maxActive,
                          int maxWait) {
        this.initialSize = initialSize;
        this.maxActive = maxActive;
        this.maxWait = maxWait;
    }

    public int getInitialSize() {
        return initialSize;
    }

    public void setInitialSize(int initialSize) {
        this.initialSize = initialSize;
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

}