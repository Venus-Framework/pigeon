package com.dianping.pigeon.governor.task;

import com.dianping.lion.client.Lion;
import com.dianping.pigeon.governor.lion.ConfigHolder;
import com.dianping.pigeon.governor.lion.LionKeys;
import com.dianping.pigeon.registry.RegistryManager;
import com.dianping.pigeon.registry.zookeeper.CuratorClient;
import com.dianping.pigeon.registry.zookeeper.CuratorRegistry;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.List;

/**
 * Created by chenchongze on 15/12/22.
 */
public class PigeonProviderHeartBeatCheck implements Runnable {

    private Logger logger = LogManager.getLogger();

    @Autowired
    private ThreadPoolTaskExecutor threadPoolTaskExecutor;

    private CuratorClient client;

    private volatile static String isCheckEnable = Lion.get("pigeon.heartbeat.enable","false");

    private CuratorRegistry registry = (CuratorRegistry)RegistryManager.getInstance().getRegistry();

    public void init() {
        client =  registry.getCuratorClient();
    }

    @Override
    public void run() {

        try {
            while("true".equals(isCheckEnable)) {
                Long startTime = System.currentTimeMillis();
                Long refreshInternal = Long.parseLong(ConfigHolder.get(LionKeys.PROVIDER_HEARTBEAT_INTERNAL));
                Long checkInternal = refreshInternal + refreshInternal / 10;

                //检查心跳
                List<String> heartBeats = client.getChildren("/DP/HEARTBEAT", false);



                Long internal = refreshInternal - System.currentTimeMillis() + startTime;
                if(internal > 0) {
                    Thread.sleep(internal);
                }
            }
        } catch (Throwable t) {
            logger.error("check provider heart task error!", t);
        } finally {

        }
    }

}
