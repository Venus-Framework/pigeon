package com.dianping.pigeon.governor.task;

import com.dianping.cat.Cat;
import com.dianping.cat.message.Transaction;
import com.dianping.lion.client.Lion;
import com.dianping.pigeon.governor.lion.ConfigHolder;
import com.dianping.pigeon.governor.lion.LionKeys;
import com.dianping.pigeon.governor.model.Service;
import com.dianping.pigeon.governor.service.ServiceService;
import com.dianping.pigeon.governor.util.IPUtils;
import com.dianping.pigeon.registry.RegistryManager;
import com.dianping.pigeon.registry.zookeeper.CuratorClient;
import com.dianping.pigeon.registry.zookeeper.CuratorRegistry;
import com.dianping.pigeon.registry.zookeeper.Utils;
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
 * Created by chenchongze on 15/12/22.
 */
public class PigeonProviderHeartBeatCheck extends Thread {

    private Logger logger = LogManager.getLogger(PigeonProviderHeartBeatCheck.class);

    @Autowired
    private ThreadPoolTaskExecutor threadPoolTaskExecutor;
    @Autowired
    private ServiceService serviceService;

    private CuratorClient client;

    private volatile static String isCheckEnable = Lion.get("pigeon.heartbeat.enable","false");

    public PigeonProviderHeartBeatCheck() {
        CuratorRegistry registry = (CuratorRegistry) RegistryManager.getInstance().getRegistry();
        client =  registry.getCuratorClient();
    }

    public void init() {
        boolean enable = false;
        String server = Lion.get("pigeon-governor-server.heartbeatcheck.enable.ip");

        if(org.codehaus.plexus.util.StringUtils.isBlank(server)) {
            logger.warn("服务ip为空");
            return;
        }
        if (IPUtils.getFirstNoLoopbackIP4Address().equals(server)) {
            enable = true;
        }
        if(enable) {
            this.start();
            logger.info("PigeonProviderHeartBeatCheck started");
        }
    }

    private Map<String, Long> heartBeatsMap = new ConcurrentHashMap<String, Long>();
    private Map<String, String> serverCache = new ConcurrentHashMap<String, String>();
    private Map<String, Vector<String>> serviceList = new ConcurrentHashMap<String, Vector<String>>();

    @Override
    public void run() {
        while("true".equals(isCheckEnable)) {
            Long startTime = System.currentTimeMillis();
            Long refreshInternal = Long.parseLong(ConfigHolder.get(LionKeys.PROVIDER_HEARTBEAT_INTERNAL));
            Long checkInternal = refreshInternal + refreshInternal / 10;

            try {
                //载入心跳
                loadHeartBeats();
                //获取ip对应服务列表
                loadServiceList();
                //检查心跳
                for(String heartBeatKey : heartBeatsMap.keySet()) {
                    if(startTime - heartBeatsMap.get(heartBeatKey) < checkInternal) {
                        //heartbeat ok
                    } else if (startTime - heartBeatsMap.get(heartBeatKey) > 3 * checkInternal) {
                        //create a thread to take off service
                        threadPoolTaskExecutor.submit(new DealHeartBeat(heartBeatKey));
                    }
                }

            } catch (Throwable t) {
                logger.error("check provider heart task error!", t);
            } finally {
                Long internal = refreshInternal - System.currentTimeMillis() + startTime;
                if(internal > 0) {
                    try {
                        Thread.sleep(internal);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
        logger.warn("break out check pigeon heartbeat check thread!");
    }

    private void loadHeartBeats() {

        Transaction transaction = Cat.newTransaction("PigeonHeartBeats", "");
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

        Transaction transaction = Cat.newTransaction("PigeonServiceList", "");
        try {
            Map<String, Vector<String>> tmp_serviceList = new ConcurrentHashMap<String, Vector<String>>();
            Map<String, String> tmp_serverCache = new ConcurrentHashMap<String, String>();

            List<String> services = client.getChildren("/DP/SERVER", false);
            for (String service_zk : services) {
                if(!service_zk.startsWith("@HTTP@")) {
                    String hosts = client.get("/DP/SERVER/" + service_zk, false);
                    if(StringUtils.isNotBlank(hosts)) {
                        //缓存列表
                        tmp_serverCache.put(service_zk, hosts);

                        for(String host : hosts.split(",")) {
                            if(heartBeatsMap.containsKey(host)) {
                                if(tmp_serviceList.containsKey(host)) {
                                    tmp_serviceList.get(host).add(service_zk);
                                } else {
                                    Vector<String> serviceVec = new Vector<String>();
                                    serviceVec.add(service_zk);
                                    tmp_serviceList.put(host, serviceVec);
                                }
                            }
                        }
                    }
                }
            }

            serviceList = tmp_serviceList;
            serverCache = tmp_serverCache;

            transaction.setStatus(Transaction.SUCCESS);
        } catch (Throwable t) {
            logger.error("refresh serviceList error", t);
            transaction.setStatus(t);
        } finally {
            transaction.complete();
        }

    }

    class DealHeartBeat implements Runnable {

        private final String host;

        public DealHeartBeat(String host) {
            this.host = host;
        }

        @Override
        public void run() {
            try {
                Vector<String> serviceVec = serviceList.get(host);
                if(serviceVec != null) {
                    boolean deleteHeartBeatNode = false;

                    for(String service_zk : serviceVec) {
                        String[] hostArr = serverCache.get(service_zk).split(",");
                        HashSet<String> set = new HashSet<String>();
                        set.addAll(Arrays.asList(hostArr));
                        set.remove(host);
                        // 服务只剩一个host不摘除
                        if (set.size() > 0) {
                            if(!isPortAvailable(host)) {
                                String hosts = StringUtils.join(set, ",");
                                client.set("/DP/SERVER/" + service_zk, hosts);
                                //TODO @HTTP@的节点摘不摘？

                                //update database
                                String serviceName = Utils.unescapeServiceName(service_zk);
                                Service service = serviceService.getService(serviceName, "");
                                if(service != null) {
                                    service.setHosts(hosts);
                                    serviceService.updateById(service);
                                } else {
                                    //TODO 创建数据项
                                }

                                deleteHeartBeatNode = true;
                                logger.warn("delete " + host + " from " + service_zk);
                                //TODO 操作日志
                                //TODO 告警服务摘除
                            } else {
                                logger.warn(host + " of " + service_zk + " is still alive");
                                //TODO 告警心跳异常（即端口可通，心跳很久未更新）
                            }
                        } else {
                            logger.warn(host + " is the only host of " + service_zk);
                        }
                    }

                    if(deleteHeartBeatNode) {
                        // delete heartBeat nodes
                        client.deleteIfExists("/DP/HEARTBEAT/" + host);
                        String appname = client.get("/DP/APP/" + host, false);
                        if(StringUtils.isNotBlank(appname)) {
                            client.deleteIfExists("/DP/APPNAME/" + appname + "/" + host);
                        }
                    }
                } else {
                    // delete heartBeat nodes
                    client.deleteIfExists("/DP/HEARTBEAT/" + host);
                }

            } catch (Throwable t) {
                logger.error("failed to take off heartbeat of " + host, t);
            } finally {
                //release resources
            }

        }
    }

    private boolean isPortAvailable(String host) {
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
        try {
            socket = new Socket();
            socket.setReuseAddress(true);
            SocketAddress sa = new InetSocketAddress(ip, port);
            socket.connect(sa, 2000);
            return socket.isConnected();
        } catch (IOException e) {
            logger.warn(host + " socket read failed!", e);
            return false;
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

    public static void main(String[] args) {
        System.out.println(new PigeonProviderHeartBeatCheck().isPortAvailable("192.168.227.43:2055"));
    }

}
