package com.dianping.pigeon.remoting.invoker.client;

import com.dianping.dpsf.protocol.DefaultRequest;
import com.dianping.pigeon.config.ConfigManager;
import com.dianping.pigeon.config.ConfigManagerLoader;
import com.dianping.pigeon.log.Logger;
import com.dianping.pigeon.log.LoggerLoader;
import com.dianping.pigeon.registry.RegistryManager;
import com.dianping.pigeon.registry.util.HeartBeatSupport;
import com.dianping.pigeon.remoting.common.codec.SerializerFactory;
import com.dianping.pigeon.remoting.common.domain.InvocationRequest;
import com.dianping.pigeon.remoting.common.domain.InvocationResponse;
import com.dianping.pigeon.remoting.common.channel.Channel;
import com.dianping.pigeon.remoting.common.domain.generic.GenericRequest;
import com.dianping.pigeon.remoting.common.util.Constants;
import com.dianping.pigeon.remoting.invoker.concurrent.CallbackFuture;
import com.dianping.pigeon.remoting.invoker.util.InvokerUtils;

import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

/**
 * @author qi.yin
 *         2016/09/21  下午7:58.
 */
public class HeartbeatTask implements Runnable {

    private static final Logger logger = LoggerLoader.getLogger(HeartbeatTask.class);

    private static AtomicLong heartBeatSeq = new AtomicLong();

    private static ConfigManager configManager = ConfigManagerLoader.getConfigManager();

    private ChannelProvider channelProvider;

    private int timeout;

    private int maxFailedCount;

    private ConcurrentMap<Channel, HeartbeatStats> heartbeatStatses =
            new ConcurrentHashMap<Channel, HeartbeatStats>();


    public HeartbeatTask(ChannelProvider provider, int timeout, int maxFailedCount) {
        this.channelProvider = provider;
        this.timeout = timeout;
        this.maxFailedCount = maxFailedCount;
    }

    @Override
    public void run() {
        List<Channel> channels = channelProvider.getChannels();

        for (int index = 0; index < channels.size(); index++) {
            Channel channel = channels.get(index);
            if (channel != null) {
                if (channel.isActive()) {
                    HeartbeatStats heartBeatStat = sendHeartBeat(channel);

                    if (heartBeatStat.getFailedCount() > maxFailedCount) {
                        channel.connect();
                        heartBeatStat.resetFailedCount();
                    }
                } else {
                    channel.connect();
                }
            }
        }

    }


    public HeartbeatStats sendHeartBeat(Channel channel) {
        HeartbeatStats heartBeatStat = getOrCreateStats(channel);
        String address = channel.getRemoteAddress().getAddress().toString();

        InvocationRequest request = createHeartRequest(address);
        try {
            InvocationResponse response = null;
            CallbackFuture future = new CallbackFuture();
            response = InvokerUtils.sendRequest(channel, request, future);
            if (response == null) {
                response = future.getResponse(timeout);
            }
            if (response != null) {
                if (request.getSequence() == response.getSequence()) {
                    heartBeatStat.resetFailedCount();
                }
            } else {
                heartBeatStat.resetFailedCount();
            }
        } catch (Throwable e) {
            heartBeatStat.incFailedCount();
            logger.info("[heartbeat] send heartbeat to server[" + address + "] failed");
        }
        return heartBeatStat;
    }

    private boolean isSend(String address) {
        boolean supported = true;
        byte heartBeatSupport = RegistryManager.getInstance().getServerHeartBeatSupportFromCache(address);

        switch (HeartBeatSupport.findByValue(heartBeatSupport)) {
            case UNSUPPORT:
            case SCANNER:
                supported = false;
                break;

            case CLIENTTOSERVER:
            case BOTH:
            default:
                supported = true;
                break;
        }

        return supported;
    }

    private boolean supported(String address) {
        boolean supported = false;
        try {
            supported = RegistryManager.getInstance().isSupportNewProtocol(address);
        } catch (Throwable t) {
            supported = configManager.getBooleanValue("pigeon.mns.host.support.new.protocol", true);
            logger.warn("get protocol support failed, set support to: " + supported);
        }

        return supported;
    }

    private InvocationRequest createHeartRequest0(String address) {
        InvocationRequest request = new DefaultRequest(Constants.HEART_TASK_SERVICE + address, Constants.HEART_TASK_METHOD,
                null, SerializerFactory.SERIALIZE_HESSIAN, Constants.MESSAGE_TYPE_HEART, timeout, null);
        request.setSequence(generateHeartSeq());
        request.setCreateMillisTime(System.currentTimeMillis());
        request.setCallType(Constants.CALLTYPE_REPLY);
        return request;
    }

    private InvocationRequest createHeartRequest_(String address) {
        InvocationRequest request = new GenericRequest(Constants.HEART_TASK_SERVICE + address, Constants.HEART_TASK_METHOD,
                null, SerializerFactory.SERIALIZE_THRIFT, Constants.MESSAGE_TYPE_HEART, timeout);
        request.setSequence(generateHeartSeq());
        request.setCreateMillisTime(System.currentTimeMillis());
        request.setCallType(Constants.CALLTYPE_REPLY);
        return request;
    }

    private InvocationRequest createHeartRequest(String address) {
        if (supported(address)) {
            return createHeartRequest_(address);
        } else {
            return createHeartRequest0(address);
        }
    }

    private long generateHeartSeq() {
        return heartBeatSeq.getAndIncrement();
    }

    private HeartbeatStats getOrCreateStats(Channel channel) {
        HeartbeatStats heartbeatStats = heartbeatStatses.get(channel);

        if (heartbeatStats == null) {
            heartbeatStats = new HeartbeatStats(0);
            heartbeatStatses.putIfAbsent(channel, heartbeatStats);
        }

        return heartbeatStats;
    }


    public interface ChannelProvider {
        List<Channel> getChannels();
    }

    class HeartbeatStats {

        private AtomicInteger failedCount;

        public HeartbeatStats(int failedCount) {
            this.failedCount = new AtomicInteger(failedCount);
        }

        public int getFailedCount() {
            return failedCount.get();
        }

        public void incFailedCount() {
            failedCount.incrementAndGet();
        }

        public void resetFailedCount() {
            failedCount.set(0);
        }
    }
}
