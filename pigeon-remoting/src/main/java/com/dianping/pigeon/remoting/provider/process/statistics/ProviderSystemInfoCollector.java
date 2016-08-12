package com.dianping.pigeon.remoting.provider.process.statistics;

import com.dianping.pigeon.config.ConfigManager;
import com.dianping.pigeon.config.ConfigManagerLoader;
import com.dianping.pigeon.log.LoggerLoader;
import com.dianping.pigeon.remoting.common.util.Constants;
import com.dianping.pigeon.remoting.provider.ProviderBootStrap;
import com.dianping.pigeon.remoting.provider.Server;
import com.dianping.pigeon.remoting.provider.process.RequestProcessor;
import com.dianping.pigeon.remoting.provider.publish.ServicePublisher;
import com.google.common.collect.Maps;
import org.apache.logging.log4j.Logger;
import org.apache.thrift.TEnum;

import java.lang.management.GarbageCollectorMXBean;
import java.lang.management.ManagementFactory;
import java.lang.management.OperatingSystemMXBean;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * Created by chenchongze on 16/7/26.
 */
public enum ProviderSystemInfoCollector {

    INSTANCE;

    private final Logger logger = LoggerLoader.getLogger(this.getClass());

    private final List<GarbageCollectorMXBean> gcMXBeans = ManagementFactory.getGarbageCollectorMXBeans();
    private final OperatingSystemMXBean osMXBean = ManagementFactory.getOperatingSystemMXBean();

    private volatile long lastOldGcCount = 0;
    private volatile int currentOldGcCount;
    private volatile double avgLoad;

    private ProviderSystemInfoCollector() {
        Runnable r = new Runnable() {
            @Override
            public void run() {
                doTask();
            }
        };
        Executors.newSingleThreadScheduledExecutor().scheduleAtFixedRate(r, 10, 10, TimeUnit.SECONDS);
    }

    private void doTask() {
        updateOldGcCount();
        updateProcessCpuLoad();
    }

    private void updateOldGcCount() {
        GarbageCollectorMXBean majorGC = gcMXBeans.get(1);
        long oldGcTotalCount = majorGC.getCollectionCount();
        currentOldGcCount = (int) (oldGcTotalCount - lastOldGcCount);
        lastOldGcCount = oldGcTotalCount;
    }

    private void updateProcessCpuLoad() {
        avgLoad = osMXBean.getSystemLoadAverage();
    }

    public int getOldGC() {
        return currentOldGcCount;
    }

    public double getSystemLoadAverage() {
        return avgLoad;
    }

    public int getThreadNum() {

        for (Server server : ProviderBootStrap.getServersMap().values()) {

            if (Constants.PROTOCOL_DEFAULT.equals(server.getProtocol())) {
                RequestProcessor processor = server.getRequestProcessor();

                if (processor != null) {
                    return processor.getRequestProcessThreadPool().getExecutor().getActiveCount();
                }
            }
        }

        return 0;
    }

    public int getQueueSize() {

        for (Server server : ProviderBootStrap.getServersMap().values()) {

            if (Constants.PROTOCOL_DEFAULT.equals(server.getProtocol())) {
                RequestProcessor processor = server.getRequestProcessor();

                if (processor != null) {
                    return processor.getRequestProcessThreadPool().getExecutor().getQueue().size();
                }
            }
        }

        return 0;
    }

    public Map<String,Double> getQpsMap() {
        Map<String,Double> methodQpsMap = Maps.newHashMap();

        // total
        int totalQps = 0;
        Map<String, ProviderCapacityBucket> appCapacityBuckets = ProviderStatisticsHolder.getCapacityBuckets();

        for (String app : appCapacityBuckets.keySet()) {
            ProviderCapacityBucket appCapacity = appCapacityBuckets.get(app);

            if (appCapacity != null) {
                totalQps += appCapacity.getRequestsInLastMinute();
            }
        }

        methodQpsMap.put("all", totalQps / 60.0);

        // method
        Map<String, ProviderCapacityBucket> methodCapacityBuckets = ProviderStatisticsHolder.getMethodCapacityBuckets();

        for (String requestMethod : methodCapacityBuckets.keySet()) {
            ProviderCapacityBucket methodCapacity = methodCapacityBuckets.get(requestMethod);

            if (methodCapacity != null) {
                methodQpsMap.put(requestMethod, methodCapacity.getRequestsInLastMinute() / 60.0);
            }
        }

        return methodQpsMap;
    }

    public int getStatus(int port) {
        Integer weight = ServicePublisher.getServerWeight().get(ConfigManagerLoader.getConfigManager().getLocalIp()
                + ":" + port);

        if (weight == null) {
            logger.warn("failed to get local weight cache, return default weight: " + Constants.WEIGHT_DEFAULT);
            weight = Constants.WEIGHT_DEFAULT;
        }

        return weightToStatus(weight);
    }

    private int weightToStatus(int weight) {
        int status;

        if (weight == Constants.DEFAULT_WEIGHT_INITIAL) {
            status = Status.DEAD.getValue();//dead
        } else if (weight > Constants.DEFAULT_WEIGHT_INITIAL) {
            status = Status.ALIVE.getValue();//alive
        } else {
            status = Status.ALIVE.getValue();
        }

        return status;
    }

    private enum Status implements TEnum {
        DEAD(0),
        STARTING(1),
        ALIVE(2),
        STOPPING(3),
        STOPPED(4),
        WARNING(5);

        private final int value;

        private Status(int value) {
            this.value = value;
        }

        public int getValue() {
            return this.value;
        }
    }

    public static void main(String[] args) {
        List<GarbageCollectorMXBean> beans = ManagementFactory.getGarbageCollectorMXBeans();

        for (GarbageCollectorMXBean mxbean : beans) {
            if (mxbean.isValid()) {
                System.out.println(mxbean.getName());
                System.out.println(mxbean.getCollectionCount());
                System.out.println(mxbean.getCollectionTime());
                System.out.println();
            }
        }
    }
}
