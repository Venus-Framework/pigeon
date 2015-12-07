package com.dianping.pigeon.remoting.provider.listener;

import com.dianping.pigeon.config.ConfigChangeListener;
import com.dianping.pigeon.config.ConfigManager;
import com.dianping.pigeon.config.ConfigManagerLoader;
import com.dianping.pigeon.log.LoggerLoader;
import com.dianping.pigeon.monitor.Monitor;
import com.dianping.pigeon.monitor.MonitorLoader;
import com.dianping.pigeon.registry.RegistryManager;
import com.dianping.pigeon.remoting.common.util.Constants;
import com.dianping.pigeon.remoting.provider.ProviderBootStrap;
import com.dianping.pigeon.remoting.provider.Server;
import com.dianping.pigeon.remoting.provider.config.ProviderConfig;
import org.apache.logging.log4j.Logger;

import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

/**
 * Created by chenchongze on 15/12/4.
 */
public class HeartBeatListener extends Thread {

    private static final Logger logger = LoggerLoader.getLogger(HeartBeatListener.class);

    private static ConfigManager configManager = ConfigManagerLoader.getConfigManager();

    private static RegistryManager registryManager = RegistryManager.getInstance();

    private static Monitor monitor = MonitorLoader.getMonitor();

    private static Set<String> serviceHeartBeatCache = Collections.synchronizedSet(new HashSet<String>());

    private static volatile int REFRESH_INTERVAL = configManager.getIntValue(Constants.KEY_PROVIDER_HEARTBEAT_INTERNAL,
            Constants.DEFAULT_PROVIDER_HEARTBEAT_INTERNAL);

    private static HeartBeatListener heartBeatListener = null;

    private static String serviceAddress;

    static {
        registerConfigChangeListener();
    }

    private  boolean isSendHeartBeat;

    private HeartBeatListener(String threadName, Thread.UncaughtExceptionHandler uncaughtExceptionHandler, boolean isDaemon){
        this.setName(threadName);
        this.setUncaughtExceptionHandler(uncaughtExceptionHandler);
        this.setDaemon(isDaemon);
    }

    public static void registerHeartBeat(ProviderConfig<?> providerConfig) {
        try {
            String serviceName = providerConfig.getUrl();
            serviceHeartBeatCache.add(serviceName);

            if(heartBeatListener == null) {
                initHeartBeat();
                serviceAddress = configManager.getLocalIp() + ":" + providerConfig.getServerConfig().getActualPort();
                registryManager.registerAppHostList(serviceAddress, configManager.getAppName(), ProviderBootStrap.getHttpServer().getPort());
                monitor.logEvent("PigeonService.heartbeat", "ON", new Date()+"");
            }

        } catch (Throwable t) {
            logger.error("Error while register heartbeat of service.", t);
        }
    }

    public static void unregisterHeartBeat(ProviderConfig<?> providerConfig) {
        try {
            String serviceName = providerConfig.getUrl();
            serviceHeartBeatCache.remove(serviceName);

            if(serviceHeartBeatCache.size() == 0) {
                stopHeartBeat();
                registryManager.deleteHeartBeat(serviceAddress);
                registryManager.unregisterAppHostList(serviceAddress, configManager.getAppName());
                monitor.logEvent("PigeonService.heartbeat", "OFF", new Date()+"");
            }

        } catch (Throwable t) {
            logger.error("Error while unregister heartbeat of service.", t);
        }
    }

    private static synchronized void initHeartBeat() {
        heartBeatListener = new HeartBeatListener("Pigeon-Provider-HeartBeat",new HeartBeatReboot(), true);
        heartBeatListener.isSendHeartBeat = true;
        heartBeatListener.start();
    }

    private static synchronized void stopHeartBeat() {
        heartBeatListener.isSendHeartBeat = false;
        heartBeatListener = null;
    }

    @Override
    public void run() {
        try {
            while (this.equals(heartBeatListener) && isSendHeartBeat) {
                Long heartbeat = System.currentTimeMillis();
                // 写心跳
                if(serviceHeartBeatCache.size() > 0) {
                    registryManager.updateHeartBeat(serviceAddress, heartbeat);
                }

                Long internal = REFRESH_INTERVAL - System.currentTimeMillis() + heartbeat;
                if(internal > 0) {
                    Thread.sleep(internal);
                }
            }
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
        initHeartBeat();
    }

    private static class HeartBeatReboot implements Thread.UncaughtExceptionHandler {

        @Override
        public void uncaughtException(Thread t, Throwable e) {
            tryRestartThread(t, e);
        }
    }

    private static void registerConfigChangeListener(){

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

}
