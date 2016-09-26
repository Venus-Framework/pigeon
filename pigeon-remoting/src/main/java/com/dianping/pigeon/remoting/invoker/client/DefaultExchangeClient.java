//package com.dianping.pigeon.remoting.invoker.client;
//
//import com.dianping.pigeon.log.Logger;
//import com.dianping.pigeon.log.LoggerLoader;
//import com.dianping.pigeon.remoting.common.domain.InvocationRequest;
//import com.dianping.pigeon.remoting.common.domain.InvocationResponse;
//import com.dianping.pigeon.remoting.common.channel.Channel;
//import com.dianping.pigeon.remoting.common.exception.NetworkException;
//import com.dianping.pigeon.remoting.invoker.Client;
//import com.dianping.pigeon.remoting.invoker.ClientSelector;
//import com.dianping.pigeon.remoting.invoker.domain.ConnectInfo;
//import com.dianping.pigeon.remoting.invoker.process.ResponseProcessor;
//import com.dianping.pigeon.remoting.invoker.process.ResponseProcessorFactory;
//import com.dianping.pigeon.remoting.invoker.route.region.Region;
//import com.dianping.pigeon.remoting.invoker.route.region.RegionPolicyManager;
//import com.dianping.pigeon.threadpool.NamedThreadFactory;
//import com.dianping.pigeon.util.NetUtils;
//
//import java.util.List;
//import java.util.concurrent.ScheduledFuture;
//import java.util.concurrent.ScheduledThreadPoolExecutor;
//import java.util.concurrent.TimeUnit;
//import java.util.concurrent.atomic.AtomicBoolean;
//
///**
// * @author qi.yin
// *         2016/09/21  上午10:56.
// */
//public class DefaultExchangeClient implements ExchangeClient {
//
//    private static final Logger logger = LoggerLoader.getLogger(DefaultExchangeClient.class);
//
//    private Client client;
//
//    protected volatile Region region;
//
//    private ConnectInfo connectInfo;
//
//    private String remoteHost;
//
//    private int remotePort;
//
//    private String remoteAddress;
//
//    private volatile boolean active = true;
//
//    private boolean heartbeated = false;
//
//    private ScheduledFuture<?> heatbeatTimer;
//
//    private int heartbeatTimeout;
//
//    private int maxFailedCount;
//
//    private int heartbeatInterval;
//
//    protected AtomicBoolean isClosed = new AtomicBoolean(true);
//
//    private ResponseProcessor responseProcessor = ResponseProcessorFactory.selectProcessor();
//
//    private static final ScheduledThreadPoolExecutor scheduled = new ScheduledThreadPoolExecutor(
//            4, new NamedThreadFactory("Pigeon-Client-HeartBeat-ThreadPool"));
//
//    public DefaultExchangeClient(ConnectInfo connectInfo) {
//        this.connectInfo = connectInfo;
//        this.remoteHost = connectInfo.getHost();
//        this.remotePort = connectInfo.getPort();
//        this.remoteAddress = NetUtils.toAddress(remoteHost, remotePort);
//        this.client = ClientSelector.selectClient(connectInfo);
//    }
//
//    @Override
//    public ConnectInfo getConnectInfo() {
//        return connectInfo;
//    }
//
//    @Override
//    public void open() {
//        if (isClosed.compareAndSet(true, false)) {
//            client.open();
//            startHeatbeat();
//        }
//    }
//
//    @Override
//    public void close() {
//        if (isClosed.compareAndSet(false, true)) {
//            stopHeartbeat();
//            client.close();
//        }
//    }
//
//    @Override
//    public boolean isActive() {
//        return active && client.isActive();
//    }
//
//    @Override
//    public void setActive(boolean active) {
//        this.active = active;
//    }
//
//    @Override
//    public boolean isClosed() {
//        return isClosed.get();
//    }
//
//    @Override
//    public String getHost() {
//        return remoteHost;
//    }
//
//    @Override
//    public String getAddress() {
//        return remoteAddress;
//    }
//
//    @Override
//    public int getPort() {
//        return remotePort;
//    }
//
//    @Override
//    public Region getRegion() {
//        if (region == null) {
//            region = RegionPolicyManager.INSTANCE.getRegion(getHost());
//        }
//        return region;
//    }
//
//    @Override
//    public void clearRegion() {
//        region = null;
//    }
//
//    @Override
//    public boolean equals(Object o) {
//        if (this == o) return true;
//        if (o == null || getClass() != o.getClass()) return false;
//
//        DefaultExchangeClient that = (DefaultExchangeClient) o;
//
//        if (remotePort != that.remotePort) return false;
//        return !(remoteHost != null ? !remoteHost.equals(that.remoteHost) : that.remoteHost != null);
//
//    }
//
//    @Override
//    public int hashCode() {
//        int result = remoteHost != null ? remoteHost.hashCode() : 0;
//        result = 31 * result + remotePort;
//        return result;
//    }
//
//    private void startHeatbeat() {
//        stopHeartbeat();
//        if (heartbeated) {
//            heatbeatTimer = scheduled.scheduleWithFixedDelay(
//                    new HeartbeatTask(new HeartbeatTask.ChannelProvider() {
//                        public List<Channel> getChannels() {
//                            return this.getChannels();
//                        }
//                    }, heartbeatTimeout, maxFailedCount),
//                    heartbeatInterval, heartbeatInterval, TimeUnit.MILLISECONDS);
//        }
//    }
//
//    private void stopHeartbeat() {
//        if (heatbeatTimer != null && !heatbeatTimer.isCancelled()) {
//            try {
//                heatbeatTimer.cancel(true);
//                scheduled.purge();
//            } catch (Throwable e) {
//                logger.warn(e.getMessage(), e);
//            }
//        }
//        heatbeatTimer = null;
//    }
//
//    @Override
//    public InvocationResponse write(InvocationRequest request) throws NetworkException {
//        return client.write(request);
//    }
//
//    @Override
//    public void processResponse(InvocationResponse response) {
//
//    }
//
//    @Override
//    public String getProtocol() {
//        return client.getProtocol();
//    }
//}
