package com.dianping.pigeon.remoting.provider.listener;

import com.dianping.pigeon.config.ConfigChangeListener;
import com.dianping.pigeon.config.ConfigManager;
import com.dianping.pigeon.config.ConfigManagerLoader;
import com.dianping.pigeon.log.LoggerLoader;
import com.dianping.pigeon.monitor.Monitor;
import com.dianping.pigeon.monitor.MonitorLoader;
import com.dianping.pigeon.registry.RegistryManager;
import com.dianping.pigeon.remoting.common.util.Constants;
import com.dianping.pigeon.remoting.provider.config.ProviderConfig;
import org.apache.logging.log4j.Logger;

import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

/**
 * Created by chenchongze on 15/12/4.
 */
public class HeartbeatListener extends Thread {

    private static final Logger logger = LoggerLoader.getLogger(HeartbeatListener.class);

    private static ConfigManager configManager = ConfigManagerLoader.getConfigManager();

    private static RegistryManager registryManager = RegistryManager.getInstance();

    private static Monitor monitor = MonitorLoader.getMonitor();

    private static Set<String> serviceHeartbeatCache = Collections.synchronizedSet(new HashSet<String>());

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

    private static String serviceAddress;
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
        try {
            isSendHeartbeat = true;
            String serviceName = providerConfig.getUrl();
            serviceHeartbeatCache.add(serviceName);
            if(heartbeatListener == null) {
                serviceAddress = configManager.getLocalIp() + ":" + providerConfig.getServerConfig().getActualPort();
                initHeartbeat();
                monitor.logEvent("Pigeon.Heartbeat", "ON", new Date()+"");
            }
            registryManager.registerServiceHeartbeat(serviceAddress, serviceName);
        } catch (Throwable t) {
            logger.error("Error while register heartbeat of service.", t);
        }
    }

    public static void unregisterHeartbeat(ProviderConfig<?> providerConfig) {
        try {
            String serviceName = providerConfig.getUrl();
            serviceHeartbeatCache.remove(serviceName);
            registryManager.unregisterServiceHeartbeat(serviceAddress, serviceName);

            if(serviceHeartbeatCache.size() == 0) {
                // 删除zk心跳，销毁心跳线程
                isSendHeartbeat = false;
                registryManager.deleteHeartbeat(serviceAddress);
                monitor.logEvent("Pigeon.Heartbeat", "OFF", new Date()+"");
            }
        } catch (Throwable t) {
            logger.error("Error while unregister heartbeat of service.", t);
        }
    }

    private static synchronized void initHeartbeat() {
        heartbeatListener = new HeartbeatListener("Pigeon-Heartbeat-Thread",new HeartbeatReboot(), true);
        heartbeatListener.createThead().start();
    }

    @Override
    public void run() {
        try {
            while(isSendHeartbeat) {
                Long heartbeat = System.currentTimeMillis();
                // 写心跳
                if(serviceHeartbeatCache.size() > 0) {
                    registryManager.updateHeartbeat(serviceAddress, heartbeat);
                }

                Long internal = REFRESH_INTERVAL - System.currentTimeMillis() + heartbeat;
                if(internal > 0) {
                    Thread.sleep(internal);
                }
            }
            heartbeatListener = null;
        } catch (Throwable e) {
            tryRestartThread(this, e);
        } finally {
            // release resources if needed
        }
    }

    private static void tryRestartThread(Thread t, Throwable e) {
        logger.error("heartbeat thread terminated with exception: " + t.getName(), e);
        logger.info("Thread status: " + t.getState());
        logger.info("trying to start a new thread.");

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

}
