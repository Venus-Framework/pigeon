package com.dianping.pigeon.governor.util;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * Created by chenchongze on 15/10/29.
 */
public class ThreadPoolFactory {

    private static ExecutorService proOwnerThreadPool = new ThreadPoolExecutor(2, 4, 1L, TimeUnit.SECONDS, new LinkedBlockingQueue<Runnable>());

    public static ExecutorService getProOwnerThreadPool() {
        return proOwnerThreadPool;
    }
}
