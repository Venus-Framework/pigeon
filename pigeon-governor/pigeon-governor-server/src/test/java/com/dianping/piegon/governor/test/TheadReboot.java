package com.dianping.piegon.governor.test;

//import com.dianping.pigeon.threadpool.HeartbeatThreadFactory;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Created by chenchongze on 15/12/4.
 */
public class TheadReboot {

    //private static ExecutorService heartbeartThread = Executors.newSingleThreadExecutor(new HeartbeatThreadFactory("Pigeon-Heartbeat-Thread"));

    public static void main(String[] args) {
        Thread t = new Thread(new Run());
        t.setUncaughtExceptionHandler(new Reboot());
        t.setName("test-heart");
        t.setDaemon(false);
        t.start();
        //heartbeartThread.execute(new Run());

        /*try {
            Thread.sleep(300000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }*/
    }

    public static class Run implements Runnable {
        @Override
        public void run() {
            //Thread.currentThread().setUncaughtExceptionHandler(new Reboot());
            System.out.println(Integer.parseInt("345"));
            System.out.println(Integer.parseInt("XYZ"));
        }
    }

    public static class Reboot implements Thread.UncaughtExceptionHandler {

        @Override
        public void uncaughtException(Thread t, Throwable e) {
            Logger logger = Logger.getAnonymousLogger();
            logger.log(Level.SEVERE,
                    "heartbeat thread terminated with exception: " + t.getName(),
                    e);
            logger.log(Level.INFO, "Thread status: " + t.getState());
            logger.log(Level.INFO, "trying to start a new thread.");
            //TODO 邮件告警
            new Thread(new Run()).start();
        }
    }

}
