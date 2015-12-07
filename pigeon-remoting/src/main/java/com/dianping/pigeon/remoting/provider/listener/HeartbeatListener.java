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

    private static final ConfigManager configManager = ConfigManagerLoader.getConfigManager();

    private static final RegistryManager registryManager = RegistryManager.getInstance();

    private static final Monitor monitor = MonitorLoader.getMonitor();

    private static final Set<String> serviceHeartBeatCache = Collections.synchronizedSet(new HashSet<String>());

    private static volatile int REFRESH_INTERVAL = configManager.getIntValue(Constants.KEY_PROVIDER_HEARTBEAT_INTERNAL,
            Constants.DEFAULT_PROVIDER_HEARTBEAT_INTERNAL);

    private static volatile HeartBeatListener heartBeatListener = null;

    static {
        registerConfigChangeListener();
    }

    private boolean isSendHeartBeat;

    private final String serviceAddress;

    private HeartBeatListener(String threadName, Thread.UncaughtExceptionHandler uncaughtExceptionHandler, boolean isDaemon, String serviceAddress){
        this.setName(threadName);
        this.setUncaughtExceptionHandler(uncaughtExceptionHandler);
        this.setDaemon(isDaemon);
        this.serviceAddress = serviceAddress;
    }

    public synchronized static void registerHeartBeat(ProviderConfig<?> providerConfig) {
        try {
            String serviceName = providerConfig.getUrl();
            serviceHeartBeatCache.add(serviceName);

            if(heartBeatListener == null) {
                initHeartBeat(configManager.getLocalIp() + ":" + providerConfig.getServerConfig().getActualPort());
            }

        } catch (Throwable t) {
            logger.error("Error while register heartbeat of service.", t);
        }
    }

    public synchronized static void unregisterHeartBeat(ProviderConfig<?> providerConfig) {
        try {
            String serviceName = providerConfig.getUrl();
            serviceHeartBeatCache.remove(serviceName);

            if(serviceHeartBeatCache.size() == 0 && heartBeatListener != null) {
                stopHeartBeat(heartBeatListener.serviceAddress);
            }

        } catch (Throwable t) {
            logger.error("Error while unregister heartbeat of service.", t);
        }
    }

    private static synchronized void initHeartBeat(String serviceAddress) {
        heartBeatListener = new HeartBeatListener("Pigeon-Provider-HeartBeat",new HeartBeatReboot(), true, serviceAddress);
        heartBeatListener.isSendHeartBeat = true;
        heartBeatListener.start();
        registryManager.registerAppHostList(serviceAddress, configManager.getAppName(), ProviderBootStrap.getHttpServer().getPort());
        monitor.logEvent("PigeonService.heartbeat", "ON", new Date()+"");
    }

    private static synchronized void stopHeartBeat(String serviceAddress) {
        heartBeatListener.isSendHeartBeat = false;
        heartBeatListener = null;
        registryManager.deleteHeartBeat(serviceAddress);
        registryManager.unregisterAppHostList(serviceAddress, configManager.getAppName());
        monitor.logEvent("PigeonService.heartbeat", "OFF", new Date()+"");
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

    private static void tryRestartThread(Thread t, Throwable thrown) {
        logger.error("heartbeat thread terminated with exception: " + t.getName(), thrown);
        logger.info("Thread status: " + t.getState());
        logger.info("trying to start a new thread.");

        // 等待之后重启心跳线程
        try {
            Thread.sleep(REFRESH_INTERVAL);
            initHeartBeat(heartBeatListener.serviceAddress);
        } catch (Exception e) {
            logger.fatal("HeartBeat restart failed! Please check!", e);
        }
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
