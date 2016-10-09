package com.dianping.pigeon.remoting.common.pool;

/**
 * @author qi.yin
 *         2016/07/21  上午11:09.
 */
public class PoolProperties {

    public static final int DEFAULT_INITIAL_SIZE = 1;

    public static final int DEFAULT_NORMAL_SIZE = DEFAULT_INITIAL_SIZE;

    public static final int DEFAULT_MAX_ACTIVE = 5;

    public static final int DEFAULT_MAX_WAIT = 3000;

    public static final int DEFAULT_TIME_BETWEEN_CHECKER_MILLIS = 2000;

    private int initialSize;

    private int normalSize;

    private int maxActive;

    private int maxWait;

    private int timeBetweenCheckerMillis;


    public PoolProperties() {
        this(DEFAULT_INITIAL_SIZE,
                DEFAULT_NORMAL_SIZE,
                DEFAULT_MAX_ACTIVE,
                DEFAULT_MAX_WAIT,
                DEFAULT_TIME_BETWEEN_CHECKER_MILLIS);
    }

    public PoolProperties(int initialSize,
                          int normalSize,
                          int maxActive,
                          int maxWait,
                          int timeBetweenCheckerMillis) {
        this.initialSize = initialSize;
        this.normalSize = normalSize;
        this.maxActive = maxActive;
        this.maxWait = maxWait;
        this.timeBetweenCheckerMillis = timeBetweenCheckerMillis;
    }

    public int getInitialSize() {
        return initialSize;
    }

    public void setInitialSize(int initialSize) {
        this.initialSize = initialSize;
    }

    public int getNormalSize() {
        return normalSize;
    }

    public void setNormalSize(int normalSize) {
        this.normalSize = normalSize;
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

    public int getTimeBetweenCheckerMillis() {
        return timeBetweenCheckerMillis;
    }

    public void setTimeBetweenCheckerMillis(int timeBetweenCheckerMillis) {
        this.timeBetweenCheckerMillis = timeBetweenCheckerMillis;
    }
}