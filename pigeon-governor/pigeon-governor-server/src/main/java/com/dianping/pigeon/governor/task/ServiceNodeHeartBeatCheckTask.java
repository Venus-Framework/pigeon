package com.dianping.pigeon.governor.task;

import com.dianping.cat.Cat;
import com.dianping.cat.message.Transaction;
import com.dianping.lion.client.Lion;
import com.dianping.pigeon.config.ConfigManager;
import com.dianping.pigeon.config.ConfigManagerLoader;
import com.dianping.pigeon.governor.bean.ServiceNodeBean;
import com.dianping.pigeon.governor.exception.DbException;
import com.dianping.pigeon.governor.lion.LionKeys;
import com.dianping.pigeon.governor.model.OpLog;
import com.dianping.pigeon.governor.model.ServiceNode;
import com.dianping.pigeon.governor.service.OpLogService;
import com.dianping.pigeon.governor.service.ProjectService;
import com.dianping.pigeon.governor.service.ServiceNodeService;
import com.dianping.pigeon.governor.service.ServiceService;
import com.dianping.pigeon.governor.util.IPUtils;
import com.dianping.pigeon.governor.util.OpType;
import com.dianping.pigeon.registry.Registry;
import com.dianping.pigeon.registry.RegistryManager;
import com.dianping.pigeon.registry.zookeeper.CuratorClient;
import com.dianping.pigeon.registry.zookeeper.CuratorRegistry;
import com.dianping.pigeon.registry.zookeeper.Utils;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import org.apache.commons.lang.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by chenchongze on 16/7/7.
 */
public class ServiceNodeHeartBeatCheckTask extends Thread {

    private Logger logger = LogManager.getLogger(ServiceNodeHeartBeatCheckTask.class);

    @Autowired
    private ThreadPoolTaskExecutor threadPoolTaskExecutor;
    @Autowired
    private ServiceService serviceService;
    @Autowired
    private ServiceNodeService serviceNodeService;
    @Autowired
    private CheckAndSyncServiceNodeDB checkAndSyncServiceNodeDB;
    @Autowired
    private OpLogService opLogService;
    @Autowired
    private ProjectService projectService;

    private CuratorRegistry registry;
    private CuratorClient client;

    private final ConfigManager configManager = ConfigManagerLoader.getConfigManager();

    private Map<String, Long> heartBeatsMap = new ConcurrentHashMap<String, Long>();
    private Map<ServiceNodeBean, ServiceNode> serviceNodeMap = Maps.newHashMap();
    // host --> List<ServiceNode>
    private Map<String, List<ServiceNode>> hostIndex = new ConcurrentHashMap<String, List<ServiceNode>>();
    private final static long pickOffHeartBeatNodeInternal = 28800000L;

    public ServiceNodeHeartBeatCheckTask() {
        for (Registry registry : RegistryManager.getInstance().getRegistryList()) {
            if(registry instanceof CuratorRegistry) {
                client = ((CuratorRegistry) registry).getCuratorClient();
                break;
            }
        }
    }

    public void init() {
        boolean enable = false;
        String server = Lion.get("pigeon-governor-server.node.heart.check.enable.ip");

        if(org.codehaus.plexus.util.StringUtils.isBlank(server)) {
            logger.warn("服务ip为空");
            return;
        }
        if (IPUtils.getFirstNoLoopbackIP4Address().equals(server)) {
            enable = true;
        }
        if(enable) {
            this.start();
            logger.info("ServiceNodeHeartbeatCheck started");
        }
    }

    private void refreshDb() {
        try {
            checkAndSyncServiceNodeDB.loadFromDb();
        } catch (DbException e1) {
            logger.warn("load from db failed!try again!",e1);
            try {
                checkAndSyncServiceNodeDB.loadFromDb();
            } catch (DbException e2) {
                logger.error("load from db failed!!",e2);
            }
        }

        serviceNodeMap = checkAndSyncServiceNodeDB.getServiceNodeMap();
    }

    @Override
    public void run() {
        while("true".equals(Lion.get(LionKeys.HEARTBEAT_ENABLE.value()))) {

            long internal = 0;
            try {
                Long startTime = System.currentTimeMillis();
                Long refreshInternal = Lion.getLongValue(LionKeys.PROVIDER_HEARTBEAT_INTERNAL.value());
                Long checkInternal = refreshInternal + refreshInternal / 10;

                //载入心跳
                loadHeartBeats();
                //获取ip对应服务列表
                loadServiceList();
                //检查心跳
                checkHeartBeats(startTime, checkInternal);

                internal = refreshInternal - System.currentTimeMillis() + startTime;
            } catch (Throwable t) {
                logger.error("check provider heart task error!", t);
            } finally {
                if(internal < 1000) {
                    internal = 1000;
                }
                try {
                    Thread.sleep(internal);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
        logger.warn("break out check pigeon heartbeat check thread!");
    }

    private void loadHeartBeats() {

        Transaction transaction = Cat.newTransaction("PigeonGovernor.heartbeats", "");
        try {
            Map<String, Long> tmp_heartBeatsMap = new ConcurrentHashMap<String, Long>();

            List<String> heartBeats = client.getChildren("/DP/HEARTBEAT", false);
            for(String heartBeat : heartBeats) {
                String heartBeatStr = client.get("/DP/HEARTBEAT/" + heartBeat, false);
                if(StringUtils.isNotBlank(heartBeatStr)) {
                    Long heartBeatTime = Long.parseLong(heartBeatStr);
                    tmp_heartBeatsMap.put(heartBeat, heartBeatTime);
                }
            }

            heartBeatsMap = tmp_heartBeatsMap;

            transaction.setStatus(Transaction.SUCCESS);
        } catch (Throwable t) {
            logger.error("refresh heartbeats info error", t);
            transaction.setStatus(t);
        } finally {
            transaction.complete();
        }

    }

    private void loadServiceList() {

        Transaction transaction = Cat.newTransaction("PigeonGovernor.serviceList", "");
        try {
            //刷新数据库
            refreshDb();
            Map<String, List<ServiceNode>> tmp_hostIndex = Maps.newConcurrentMap();

            for (ServiceNodeBean serviceNodeBean : serviceNodeMap.keySet()) {
                ServiceNode serviceNodeDb = serviceNodeMap.get(serviceNodeBean);
                String host = IPUtils.getHost(serviceNodeDb.getIp(), serviceNodeDb.getPort());

                if(heartBeatsMap.containsKey(host)) { //只检查2.7.0及以上版本，写过心跳的hosts
                    List<ServiceNode> tmp_serviceNodeList = tmp_hostIndex.get(host);

                    if(tmp_serviceNodeList != null) {
                        tmp_serviceNodeList.add(serviceNodeDb);
                    } else {
                        tmp_serviceNodeList = Lists.newArrayList();
                        tmp_serviceNodeList.add(serviceNodeDb);

                        tmp_hostIndex.put(host, tmp_serviceNodeList);
                    }
                }

            }

            hostIndex = tmp_hostIndex;

            transaction.setStatus(Transaction.SUCCESS);
        } catch (Throwable t) {
            logger.error("refresh serviceList error", t);
            transaction.setStatus(t);
        } finally {
            transaction.complete();
        }

    }

    private void checkHeartBeats(long startTime, long checkInternal) {
        for(String heartBeatKey : heartBeatsMap.keySet()) {
            if (startTime - heartBeatsMap.get(heartBeatKey) > 3 * checkInternal) {
                //create a thread to take off service
                threadPoolTaskExecutor.submit(new DealHeartBeat(startTime, heartBeatKey));
            }
        }
    }

    class DealHeartBeat implements Runnable {

        private final long startTime;
        private final String host;

        public DealHeartBeat(long startTime, String host) {
            this.startTime = startTime;
            this.host = host;
        }

        @Override
        public void run() {
            try {
                List<ServiceNode> serviceNodeList = hostIndex.get(host);

                if(serviceNodeList != null) {

                    // 服务只剩一个host不摘除，探测3次，间隔一秒
                    if(!isPortAvailable(host, 3)) {

                        for(ServiceNode serviceNode : serviceNodeList) {
                            String serviceName = serviceNode.getServiceName();
                            String group = serviceNode.getGroup();
                            String ip = serviceNode.getIp();
                            String port = serviceNode.getPort();
                            String service_zk = Utils.escapeServiceName(serviceName);

                            /* 这里拉数据库的话可能导致缓存的数据和zk不一致，
                            直接更新zk导致有的host没写上去，所以直接拉zk */
                            String serviceHostAddress = "/DP/SERVER/" + service_zk;

                            if(StringUtils.isNotBlank(group)){
                                serviceHostAddress = serviceHostAddress + "/" + group;
                            }

                            String hosts_zk = client.get(serviceHostAddress, false);

                            if(StringUtils.isBlank(hosts_zk)) {
                                logger.warn("no data exists in zk: " + serviceName + "#" + group);
                                continue;
                            }

                            Set<String> set = Sets.newHashSet( hosts_zk.split(","));
                            set.remove(host);

                            int minProviderHeartbeat = Lion.getIntValue(LionKeys.MIN_PROVIDER_HEARTBEAT.value(), 2);
                            int hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY);
                            // 摘除心跳条件：不满足最小阈值条件时，判断心跳失联时间超过8小时，且当前系统时间为凌晨3点到5点之间，摘除
                            if (set.size() >= minProviderHeartbeat
                                    || ("qa".equals(configManager.getEnv()) && StringUtils.isNotBlank(group))
                                    || (startTime - heartBeatsMap.get(host) > pickOffHeartBeatNodeInternal
                                    && hour > 2 && hour < 6) ) { // 摘除心跳
                                String hosts = StringUtils.join(set, ",");
                                client.set(serviceHostAddress, hosts);
                                //update database
                                serviceNodeService.deleteServiceNode(serviceName, group, ip, port);

                                logger.warn("delete " + host + " from " + serviceName + "#" + group);
                                threadPoolTaskExecutor.submit(new LogOpRun(OpType.PICK_OFF_PROVIDER_HEARTBEAT,
                                        "delete " + host + " from " + serviceName + "#" + group));

                            } else { // 保留
                                logger.warn(host + " num of " + serviceName + "#" + group
                                        + " is less than min: " + minProviderHeartbeat);
                            }
                        }
                    } else {
                        logger.warn(host + " is still alive");
                        //TODO 告警心跳异常（即端口可通，心跳很久未更新，不正常）
                    }

                } else {// delete heartBeat nodes
                    int hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY);
                    if(startTime - heartBeatsMap.get(host) > pickOffHeartBeatNodeInternal
                            && hour > 2 && hour < 6) { // 心跳失联时间超过8小时，且当前系统时间为凌晨3点到5点之间，摘除孤单心跳节点
                        logger.warn("takeoff lonely heartbeat node: " + host);
                        String appname = client.get("/DP/APP/" + host, false);
                        if(StringUtils.isNotBlank(appname)) {
                            client.deleteIfExists("/DP/APPNAME/" + appname + "/" + host);
                        }
                        //还是容易误删，保留吧
                        //client.deleteIfExists("/DP/HEARTBEAT/" + host);
                        //client.deleteIfExists("/DP/WEIGHT/" + host);
                        //client.deleteIfExists("/DP/APP/" + host);
                        //client.deleteIfExists("/DP/VERSION/" + host);
                    }
                }

            } catch (Throwable t) {
                logger.error("failed to take off heartbeat of " + host, t);
            } finally {
                //release resources
            }

        }
    }

    private boolean isPortAvailable(String host, int count) {
        boolean isAlive;
        int idx = host.lastIndexOf(":");
        if(idx == -1) {
            return false;
        }

        int port;
        try {
            port = Integer.parseInt(host.substring(idx + 1));
        } catch (NumberFormatException e) {
            logger.warn("port error: " + host, e);
            return false;
        }
        String ip = host.substring(0, idx);

        Socket socket = null;
        int next = --count;

        try {
            socket = new Socket();
            socket.setReuseAddress(true);
            SocketAddress sa = new InetSocketAddress(ip, port);
            socket.connect(sa, 2000);
            isAlive = socket.isConnected();

            if (!isAlive && --next > 0) {
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    //e.printStackTrace();
                }
                return isPortAvailable(host, next);
            } else {
                return isAlive;
            }

        } catch (IOException ioe) {
            logger.warn(host + " socket read failed!");
            if (--next > 0) {
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    //e.printStackTrace();
                }
                return isPortAvailable(host, next);
            } else {
                return false;
            }

        } finally {
            if (socket != null) {
                try {
                    socket.close();
                } catch (IOException e) {
                    logger.warn("failed to close socket!", e);
                }
            }
        }
    }

    private class LogOpRun implements Runnable {


        private final OpType opType;
        private final String content;

        private LogOpRun(OpType opType,
                         String content) {
            this.opType = opType;
            this.content = content;
        }

        @Override
        public void run() {
            String reqIp = IPUtils.getFirstNoLoopbackIP4Address();
            OpLog opLog = new OpLog();
            opLog.setDpaccount(reqIp);
            opLog.setReqip(reqIp);
            opLog.setOptime(new Date());
            opLog.setContent(content);
            opLog.setOptype(opType.getValue());
            opLogService.create(opLog);
        }

    }
}
