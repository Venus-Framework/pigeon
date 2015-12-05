package com.dianping.pigeon.threadpool;

import java.util.concurrent.ThreadFactory;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Created by chenchongze on 15/12/4.
 */
@Deprecated
public class HeartbeatThreadFactory implements ThreadFactory {

    private final String threadName;

    private final ThreadGroup mGroup;

    public HeartbeatThreadFactory(String threadName) {
        this.threadName = threadName;
        SecurityManager s = System.getSecurityManager();
        mGroup = (s == null) ? Thread.currentThread().getThreadGroup() : s.getThreadGroup();
    }

    @Override
    public Thread newThread(Runnable runnable) {
        Thread ret = new Thread(mGroup, runnable, threadName, 0);
        ret.setDaemon(true);
        ret.setUncaughtExceptionHandler(new HeartbeatReboot());
        return ret;
    }

    public class HeartbeatReboot implements Thread.UncaughtExceptionHandler {

        @Override
        public void uncaughtException(Thread t, Throwable e) {
            Logger logger = Logger.getAnonymousLogger();
            logger.log(Level.SEVERE,
                    "heartbeat thread terminated with exception: " + t.getName(),
                    e);
            logger.log(Level.INFO, "Thread status: " + t.getState());
            logger.log(Level.INFO, "trying to start a new thread.");
            //TODO 邮件告警

        }
    }
}
