package com.dianping.pigeon.governor.task;

import com.dianping.cat.Cat;
import com.dianping.cat.message.Transaction;
import com.dianping.lion.client.Lion;
import com.dianping.pigeon.governor.bean.ServiceNodeBean;
import com.dianping.pigeon.governor.exception.DbException;
import com.dianping.pigeon.governor.model.ServiceNode;
import com.dianping.pigeon.governor.service.ServiceNodeService;
import com.dianping.pigeon.governor.util.IPUtils;
import com.dianping.pigeon.registry.Registry;
import com.dianping.pigeon.registry.RegistryManager;
import com.dianping.pigeon.registry.zookeeper.CuratorClient;
import com.dianping.pigeon.registry.zookeeper.CuratorRegistry;
import com.dianping.pigeon.registry.zookeeper.Utils;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import org.apache.commons.lang.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by chenchongze on 16/7/7.
 */
public class CheckAndSyncServiceNodeDB {

    private Logger logger = LogManager.getLogger(CheckAndSyncServiceNodeDB.class);

    @Autowired
    private ServiceNodeService serviceNodeService;

    private CuratorClient client;

    // serviceNodeBean --> serviceNodeDb
    private Map<ServiceNodeBean, ServiceNode> serviceNodeMap = Maps.newHashMap();

    // host --> appname
    private Map<String, String> hostAppMapping = Maps.newHashMap();

    // serviceNodeBean from zk
    Set<ServiceNodeBean> serviceNodeBeanSet = Sets.newHashSet();

    public Map<ServiceNodeBean, ServiceNode> getServiceNodeMap() {
        return serviceNodeMap;
    }

    public CheckAndSyncServiceNodeDB() {
        for (Registry registry : RegistryManager.getInstance().getRegistryList()) {
            if(registry instanceof CuratorRegistry) {
                client = ((CuratorRegistry) registry).getCuratorClient();
                break;
            }
        }
    }

    /**
     * 定时每日凌晨2点30分启动
     */
    public void schedule() {
        boolean enable = false;
        String server = Lion.get("pigeon-governor-server.sync.servicenode.enable.ip");

        if(StringUtils.isBlank(server)) {
            logger.warn("服务ip为空");
            return;
        }
        if (IPUtils.getFirstNoLoopbackIP4Address().equals(server)) {
            enable = true;
        }
        if(enable) {
            logger.info("CheckAndSyncServiceNodeDB start...");
            checkAndSyncDB();
        }
    }

    //todo 临时改为public
    public void checkAndSyncDB() {
        Transaction transaction = Cat.newTransaction("PigeonGovernor.checkAndSyncDB", "");

        try {
            // load from db, db异常就用之前的db缓存
            loadFromDb();

            //load appname form zk, 不存在就用NULL
            loadAppFromZk();

            // load from zk, zk异常记得把exception带出线程再抛出来
            loadFromZk();

            // sync from zk to db
            syncZkToDb();

            // clear cache objects
            reInitCache();

            // fake host heartbeat
            fakeServiceHeart();

            transaction.setStatus(Transaction.SUCCESS);
        } catch (DbException e) {
            logger.error("Load from db error!", e);
            transaction.setStatus(e);
            //todo use last db cache or exit?
        } catch (Throwable t) {
            logger.error("checkAndSyncDB error!", t);
            transaction.setStatus(t);
        } finally {
            transaction.complete();
        }
    }

    public void loadFromDb() throws DbException {
        List<ServiceNode> serviceNodeList = serviceNodeService.retrieveAll();
        Map<ServiceNodeBean, ServiceNode> tmp_serviceNodeMap = Maps.newHashMap();

        for (ServiceNode serviceNode : serviceNodeList) {
            ServiceNodeBean serviceNodeBean = new ServiceNodeBean(serviceNode.getServiceName(),
                    serviceNode.getGroup(), serviceNode.getIp(), serviceNode.getPort(), serviceNode.getProjectName());
            tmp_serviceNodeMap.put(serviceNodeBean, serviceNode);
        }

        serviceNodeMap = tmp_serviceNodeMap;
    }

    private void loadAppFromZk() throws Exception {
        List<String> hostsZk = client.getChildren("/DP/APP", false);

        if (hostsZk != null && hostsZk.size() > 0) {
            Map<String, String> tmp_hostAppMapping = Maps.newHashMap();

            for (String hostZk : hostsZk) {
                String tmp_app = null;

                try {
                    tmp_app = client.get("/DP/APP/" + hostZk, false);
                } catch (Exception e) {
                    logger.error("Get app from zk error: "+ hostZk + ", goto next loop.", e);
                    continue;
                }

                if(StringUtils.isBlank(tmp_app)) {
                    tmp_app = "NULL";
                }

                tmp_hostAppMapping.put(hostZk, tmp_app);

            }

            hostAppMapping = tmp_hostAppMapping;
        }
    }

    private void loadFromZk() throws Exception {
        Set<ServiceNodeBean> tmp_serviceNodeBeanSet = Sets.newHashSet();
        List<String> servicesZk = client.getChildren("/DP/SERVER", false);

        if (servicesZk == null) {
            //never happened.
            logger.error("no service existed error!");
            return ;
        }

        for (String serviceZk : servicesZk) {

            if(!serviceZk.startsWith("@HTTP@")) { // 忽略@HTTP@开头的http服务名
                String serviceDb = Utils.unescapeServiceName(serviceZk);
                //查看无泳道服务节点
                String servicePath = "/DP/SERVER/" + serviceZk;
                String hosts = client.get(servicePath, false);

                if (StringUtils.isNotBlank(hosts)) {

                    for (String host : IPUtils.getValidHosts(hosts.split(","))) { //缓存无泳道服务
                        String projectName = hostAppMapping.get(host);

                        if (StringUtils.isNotBlank(projectName)) {
                            int index = host.lastIndexOf(":");
                            String ip = host.substring(0, index);
                            String port = host.substring(index + 1);

                            ServiceNodeBean tmp_serviceNodeBean = new ServiceNodeBean();
                            tmp_serviceNodeBean.setIp(ip);
                            tmp_serviceNodeBean.setPort(port);
                            tmp_serviceNodeBean.setServiceName(serviceDb);
                            tmp_serviceNodeBean.setGroup("");
                            tmp_serviceNodeBean.setProjectName(projectName);

                            tmp_serviceNodeBeanSet.add(tmp_serviceNodeBean);
                        }
                    }

                }

                List<String> serviceZkGroups = client.getChildren(servicePath, false);

                if(serviceZkGroups != null && serviceZkGroups.size() > 0) {

                    for(String serviceZkGroup : serviceZkGroups) {
                        //查看泳道服务节点
                        String hostsInGroup = client.get(servicePath + "/" + serviceZkGroup, false);

                        if(StringUtils.isNotBlank(hostsInGroup)) {

                            for (String hostInGroup : IPUtils.getValidHosts(hostsInGroup.split(","))) { //缓存带泳道服务
                                String projectName = hostAppMapping.get(hostInGroup);

                                if (StringUtils.isNotBlank(projectName)) {
                                    int index = hostInGroup.lastIndexOf(":");
                                    String ip = hostInGroup.substring(0, index);
                                    String port = hostInGroup.substring(index + 1);

                                    ServiceNodeBean tmp_serviceNodeBean = new ServiceNodeBean();
                                    tmp_serviceNodeBean.setIp(ip);
                                    tmp_serviceNodeBean.setPort(port);
                                    tmp_serviceNodeBean.setServiceName(serviceDb);
                                    tmp_serviceNodeBean.setGroup(serviceZkGroup);
                                    tmp_serviceNodeBean.setProjectName(projectName);

                                    tmp_serviceNodeBeanSet.add(tmp_serviceNodeBean);
                                }
                            }
                        }
                    }
                }
            }
        }

        serviceNodeBeanSet = tmp_serviceNodeBeanSet;
    }

    private void syncZkToDb() throws Exception {
        for (ServiceNodeBean serviceNodeBean : serviceNodeBeanSet) {
            ServiceNode serviceNodeDb = serviceNodeMap.get(serviceNodeBean);

            if (serviceNodeDb == null) {
                ServiceNode newServiceNode = serviceNodeBean.newServiceNode();
                serviceNodeService.createServiceNode(newServiceNode);
            }
        }
    }

    private void reInitCache() {
        serviceNodeMap = Maps.newHashMap();
        hostAppMapping = Maps.newHashMap();
        serviceNodeBeanSet = Sets.newHashSet();
    }

    private void fakeServiceHeart() {
        Transaction transaction = Cat.newTransaction("PigeonGovernor.fakeHeartbeat", "");
        try {
            List<ServiceNode> serviceNodeList = serviceNodeService.retrieveAll();
            Set<String> hostSet = Sets.newHashSet();

            for (ServiceNode serviceNode : serviceNodeList) {
                String host = IPUtils.getHost(serviceNode.getIp(), serviceNode.getPort());
                hostSet.add(host);
            }

            long now = System.currentTimeMillis();

            for(String host : hostSet) {
                client.set("/DP/HEARTBEAT/" + host, now);
            }

            transaction.setStatus(Transaction.SUCCESS);
        } catch (Throwable t) {
            logger.error("fake heartbeats error", t);
            transaction.setStatus(t);
        } finally {
            transaction.complete();
        }
    }

}
