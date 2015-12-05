package com.dianping.pigeon.remoting.provider.listener;

import com.dianping.pigeon.config.ConfigChangeListener;
import com.dianping.pigeon.config.ConfigManager;
import com.dianping.pigeon.config.ConfigManagerLoader;
import com.dianping.pigeon.registry.util.Utils;
import com.dianping.pigeon.remoting.common.util.Constants;
import com.dianping.pigeon.remoting.provider.config.ProviderConfig;
import com.dianping.pigeon.remoting.provider.config.ServerConfig;

import java.util.Date;
import java.util.HashSet;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Created by chenchongze on 15/12/4.
 */
public class HeartbeatListener extends Thread {

    private static ConfigManager configManager = ConfigManagerLoader.getConfigManager();

    private static Set<String> serviceHeartbeatCache = new HashSet<String>();

    private static volatile int REFRESH_INTERVAL = configManager.getIntValue(Constants.KEY_PROVIDER_HEARTBEAT_INTERNAL,
            Constants.DEFAULT_PROVIDER_HEARTBEAT_INTERNAL);

    private static volatile boolean isSendHeartbeat = false;

    static {
        configManager.registerConfigChangeListener(new ConfigChangeListener() {

            @Override
            public void onKeyUpdated(String key, String value) {
                if (Constants.KEY_PROVIDER_HEARTBEAT_INTERNAL.equals(key)) {
                    REFRESH_INTERVAL = Integer.parseInt(value);
                }
            }

            @Override
            public void onKeyAdded(String key, String value) {

            }

            @Override
            public void onKeyRemoved(String key) {

            }
        });
    }

    private static HeartbeatListener heartbeatListener = null;

    private String threadName;
    private Thread.UncaughtExceptionHandler uncaughtExceptionHandler;
    private boolean isDaemon;

    private HeartbeatListener(String threadName, Thread.UncaughtExceptionHandler uncaughtExceptionHandler, boolean isDaemon){
        this.threadName = threadName;
        this.uncaughtExceptionHandler = uncaughtExceptionHandler;
        this.isDaemon = isDaemon;
    }

    private Thread createThead() {
        Thread t = new Thread(this, threadName);
        t.setUncaughtExceptionHandler(uncaughtExceptionHandler);
        t.setDaemon(isDaemon);
        return t;
    }


    public static void registerHeartbeat(ProviderConfig<?> providerConfig) {
        isSendHeartbeat = true;
        serviceHeartbeatCache.add(providerConfig.getUrl());
        if(heartbeatListener == null) {
            initHeartbeat();
        }
        //TODO 写zk
        String path = getHeartbeatPath(providerConfig);
        System.out.println("add: " + path);

    }

    public static void unregisterHeartbeat(ProviderConfig<?> providerConfig) {
        serviceHeartbeatCache.remove(providerConfig.getUrl());
        //TODO 删除zk
        String path = getHeartbeatPath(providerConfig);
        System.out.println("del: " + path);

        if(serviceHeartbeatCache.size() == 0) {
            isSendHeartbeat = false;
        }

    }

    private static synchronized void initHeartbeat() {
        heartbeatListener = new HeartbeatListener("Pigeon-Heartbeat-Thread",new HeartbeatReboot(),true);
        heartbeatListener.createThead().start();
    }

    private static String getHeartbeatPath(ProviderConfig<?> providerConfig){
        return com.dianping.pigeon.registry.util.Constants.HEARTBEAT_PATH
                + com.dianping.pigeon.registry.util.Constants.PATH_SEPARATOR
                + configManager.getLocalIp() + ":"
                + providerConfig.getServerConfig().getActualPort()
                + com.dianping.pigeon.registry.util.Constants.PATH_SEPARATOR
                + Utils.escapeServiceName(providerConfig.getUrl());
    }

    @Override
    public void run() {
        Throwable thrown = null;
        try {
            while(!Thread.currentThread().isInterrupted()){
                if(isSendHeartbeat) {
                    long heartbeat = System.currentTimeMillis();
                    //TODO 写心跳
                    System.out.println(heartbeat);

                    long internal = REFRESH_INTERVAL - System.currentTimeMillis() + heartbeat;
                    if(internal > 0) {
                        Thread.sleep(internal);
                    }
                }
            }
        } catch (Throwable e) {
            thrown = e;
        } finally {
            tryRestartThread(this, thrown);
        }
    }

    private static void tryRestartThread(Thread t, Throwable e) {
        Logger logger = Logger.getAnonymousLogger();
        logger.log(Level.SEVERE,
                "heartbeat thread terminated with exception: " + t.getName(),
                e);
        logger.log(Level.INFO, "Thread status: " + t.getState());
        logger.log(Level.INFO, "trying to start a new thread.");

        // 等待之后重启心跳线程
        try {
            Thread.sleep(REFRESH_INTERVAL);
        } catch (InterruptedException ie) {
            ie.printStackTrace();
        }
        initHeartbeat();
    }

    private static class HeartbeatReboot implements Thread.UncaughtExceptionHandler {

        @Override
        public void uncaughtException(Thread t, Throwable e) {
            tryRestartThread(t, e);
        }
    }



    public static void main(String[] args) {
        ProviderConfig<Object> providerConfig1 = new ProviderConfig<Object>(new Object());
        ServerConfig serverConfig1 = new ServerConfig();
        serverConfig1.setActualPort(4040);
        providerConfig1.setUrl("http://service.dianping.com/wechatAPIService/wechatAPIRemoteService_1.0.0");
        providerConfig1.setServerConfig(serverConfig1);
        ProviderConfig<Object> providerConfig2 = new ProviderConfig<Object>(new Object());
        ServerConfig serverConfig2 = new ServerConfig();
        serverConfig2.setActualPort(4040);
        providerConfig2.setUrl("http://service.dianping.com/wechatAPIService/wechatAPIJobService_2.0.0");
        providerConfig2.setServerConfig(serverConfig2);

        registerHeartbeat(providerConfig1);
        registerHeartbeat(providerConfig2);

        try {
            Thread.sleep(10000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        registerHeartbeat(providerConfig2);
        unregisterHeartbeat(providerConfig1);


        try {
            Thread.sleep(10000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        unregisterHeartbeat(providerConfig1);
        unregisterHeartbeat(providerConfig2);


        try {
            Thread.sleep(10000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        registerHeartbeat(providerConfig1);

        try {
            Thread.sleep(300000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
