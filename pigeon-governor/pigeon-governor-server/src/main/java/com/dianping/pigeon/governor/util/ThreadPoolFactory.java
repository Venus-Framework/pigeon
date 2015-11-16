package com.dianping.pigeon.governor.util;

import com.dianping.pigeon.threadpool.NamedThreadFactory;

import java.util.concurrent.*;

/**
 * Created by chenchongze on 15/10/29.
 */
public class ThreadPoolFactory {

    private static ExecutorService workThreadPool = new ThreadPoolExecutor(20, 40, 30L, TimeUnit.SECONDS,
            new LinkedBlockingQueue<Runnable>(5000),
            new NamedThreadFactory("Pigeon-Governor-Shared"),
            new ThreadPoolExecutor.CallerRunsPolicy());

    public static ExecutorService getWorkThreadPool() {
        return workThreadPool;
    }
}
