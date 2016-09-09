package com.dianping.pigeon.governor.task;

import com.dianping.cat.Cat;
import com.dianping.cat.message.Transaction;
import com.dianping.lion.client.Lion;
import com.dianping.pigeon.governor.bean.ServiceWithGroup;
import com.dianping.pigeon.governor.exception.DbException;
import com.dianping.pigeon.governor.model.Project;
import com.dianping.pigeon.governor.model.Service;
import com.dianping.pigeon.governor.service.ProjectService;
import com.dianping.pigeon.governor.service.ServiceService;
import com.dianping.pigeon.governor.util.CommonUtils;
import com.dianping.pigeon.governor.util.IPUtils;
import com.dianping.pigeon.registry.RegistryManager;
import com.dianping.pigeon.registry.zookeeper.CuratorClient;
import com.dianping.pigeon.registry.zookeeper.CuratorRegistry;
import com.dianping.pigeon.registry.zookeeper.Utils;
import org.apache.commons.lang.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by chenchongze on 16/1/20.
 */
@Deprecated
public class CheckAndSyncServiceDB {

    private Logger logger = LogManager.getLogger(CheckAndSyncServiceDB.class);

    @Autowired
    private ServiceService serviceService;
    @Autowired
    private ProjectService projectService;

    private CuratorClient client;

    private static Map<ServiceWithGroup, Service> serviceGroupDbIndex = new ConcurrentHashMap<ServiceWithGroup, Service>();
    private static Map<ServiceWithGroup, Service> serviceGroupZkIndex = new ConcurrentHashMap<ServiceWithGroup, Service>();

    public CheckAndSyncServiceDB() {
        client = ((CuratorRegistry) RegistryManager.getInstance().getRegistry()).getCuratorClient();
    }

    public static Map<ServiceWithGroup, Service> getServiceGroupDbIndex() {
        return serviceGroupDbIndex;
    }

    public void schedule() {
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
            logger.info("CheckAndSyncServiceDB start...");
            checkAndSyncDB();
        }
    }

    public void checkAndSyncDB() {
        Transaction transaction = Cat.newTransaction("checkAndSyncDB", "");
        try {
            // load from db, db异常就用之前的db缓存
            try {
                loadFromDb();
            } catch (DbException e) {
                logger.warn("Load from db error, use last db cache", e);
            }

            // load from zk, zk异常记得把exception带出线程再抛出来
            loadFromZk();

            // sync from zk to db
            syncZkToDb();

            transaction.setStatus(Transaction.SUCCESS);
        } catch (Throwable t) {
            logger.error("checkAndSyncDB error", t);
            transaction.setStatus(t);
        } finally {
            transaction.complete();
        }
    }

    private void syncZkToDb() {
        for(ServiceWithGroup serviceWithGroup : serviceGroupZkIndex.keySet()) {
            try {
                Service serviceZk = serviceGroupZkIndex.get(serviceWithGroup);
                String hostsZk = serviceZk.getHosts();
                if(serviceGroupDbIndex.containsKey(serviceWithGroup)) {
                    Service serviceDb = serviceGroupDbIndex.get(serviceWithGroup);
                    if(!hostsZk.equalsIgnoreCase(serviceDb.getHosts())) {
                        serviceDb.setHosts(hostsZk);
                        serviceService.updateById(serviceDb);
                        logger.info("update exists db serviceCache: " + serviceWithGroup + " with hosts: " + hostsZk);
                    }
                } else {
                    Service serviceDb = serviceService.getService(serviceWithGroup.getService(), serviceWithGroup.getGroup());
                    if(serviceDb != null ) {
                        if(!hostsZk.equalsIgnoreCase(serviceDb.getHosts())) {
                            serviceDb.setHosts(hostsZk);
                            serviceService.updateById(serviceDb);
                            logger.info("update exists db service: " + serviceWithGroup + " with hosts: " + hostsZk);
                        }
                        serviceGroupDbIndex.put(serviceWithGroup, serviceDb);
                    } else { // 数据库查不到，创建新service
                        String app = null;
                        for(String host : hostsZk.split(",")) {
                            if(StringUtils.isBlank(host)) {
                                continue;
                            }
                            String tmp_app = null;
                            try {
                                tmp_app = client.get("/DP/APP/" + host, false);
                            } catch (Exception e) {
                                logger.warn("Get app from zk error: "+ host, e);
                            }
                            if(StringUtils.isNotBlank(tmp_app)) {
                                app = tmp_app;
                                break;
                            }
                        }
                        if(StringUtils.isBlank(app)) {
                            logger.warn("No exists appname for service: " + serviceZk.getName() + " in group: " + serviceZk.getGroup());
                            continue;
                        }
                        Project project = projectService.findProject(app);
                        if(project == null) {
                            project = projectService.createProject(app, false);
                        }
                        serviceZk.setProjectid(project.getId());
                        serviceService.create(serviceZk);
                        logger.info("create db service: " + serviceWithGroup + " with hosts: " + hostsZk + " with project: " + app);
                    }
                }
            } catch (Throwable t) {
                logger.error(t);
            } finally {
            }
        }
    }

    public void loadFromDb() throws DbException {
        List<Service> services = serviceService.retrieveAll();
        Map<ServiceWithGroup, Service> tmp_serviceGroupDbIndex = new ConcurrentHashMap<ServiceWithGroup, Service>();
        for(Service service : services) {
            tmp_serviceGroupDbIndex.put(new ServiceWithGroup(service.getName(), service.getGroup()), service);
        }
        serviceGroupDbIndex = tmp_serviceGroupDbIndex;
    }

    private void loadFromZk() throws Exception {
        Map<ServiceWithGroup, Service> tmp_serviceGroupZkIndex = new ConcurrentHashMap<ServiceWithGroup, Service>();
        List<String> servicesZk = client.getChildren("/DP/SERVER", false);
        for (String service_zk : servicesZk) {
            if(!service_zk.startsWith("@HTTP@")) {
                String servicePath = "/DP/SERVER/" + service_zk;
                String hosts = client.get(servicePath, false);
                if(hosts != null) {
                    String service_db = Utils.unescapeServiceName(service_zk);
                    //缓存服务
                    Service tmp_service = new Service();
                    tmp_service.setHosts(CommonUtils.normalizeHosts(hosts));
                    tmp_service.setName(service_db);
                    tmp_serviceGroupZkIndex.put(new ServiceWithGroup(service_db, ""), tmp_service);

                    //查看泳道
                    List<String> service_zk_groups = client.getChildren(servicePath, false);
                    if(service_zk_groups != null) {
                        for(String service_zk_group : service_zk_groups) {
                            String hosts_group = client.get(servicePath + "/" + service_zk_group, false);
                            if(hosts_group != null) {
                                //缓存带泳道服务
                                Service tmp_service_group = new Service();
                                tmp_service_group.setHosts(CommonUtils.normalizeHosts(hosts_group));
                                tmp_service_group.setName(service_db);
                                tmp_service_group.setGroup(service_zk_group);
                                tmp_serviceGroupZkIndex.put(new ServiceWithGroup(service_db, service_zk_group), tmp_service_group);
                            }
                        }
                    }
                }
            }
        }
        serviceGroupZkIndex = tmp_serviceGroupZkIndex;
    }

}
