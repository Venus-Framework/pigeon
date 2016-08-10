package com.dianping.pigeon.governor.controller;

import com.dianping.pigeon.governor.bean.Result;
import com.dianping.pigeon.governor.bean.ServiceWithGroup;
import com.dianping.pigeon.governor.exception.DbException;
import com.dianping.pigeon.governor.model.Service;
import com.dianping.pigeon.governor.service.ServiceService;
import com.dianping.pigeon.governor.task.CheckAndSyncServiceDB;
import com.dianping.pigeon.governor.task.CheckAndSyncServiceNodeDB;
import com.dianping.pigeon.governor.util.IPUtils;
import com.dianping.pigeon.governor.util.OpType;
import com.dianping.pigeon.registry.Registry;
import com.dianping.pigeon.registry.RegistryManager;
import com.dianping.pigeon.registry.zookeeper.CuratorClient;
import com.dianping.pigeon.registry.zookeeper.CuratorRegistry;
import com.dianping.pigeon.registry.zookeeper.Utils;
import org.apache.commons.lang.StringUtils;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.*;

/**
 * Created by chenchongze on 16/1/26.
 */
@Controller
@RequestMapping("/test")
public class TestController {

    private Logger logger = LogManager.getLogger();

    private CheckAndSyncServiceDB checkAndSyncServiceDB;
    @Autowired
    private CheckAndSyncServiceNodeDB checkAndSyncServiceNodeDB;
    @Autowired
    private ThreadPoolTaskExecutor threadPoolTaskExecutor;
    @Autowired
    private ServiceService serviceService;

    private Map<ServiceWithGroup, Service> serviceGroupDbIndex = CheckAndSyncServiceDB.getServiceGroupDbIndex();

    private CuratorClient client;

    public TestController() {
        for (Registry registry : RegistryManager.getInstance().getRegistryList()) {
            if(registry instanceof CuratorRegistry) {
                client = ((CuratorRegistry) registry).getCuratorClient();
                break;
            }
        }
    }

    @RequestMapping(value = "/syncdb2zk", method = {RequestMethod.POST})
    @ResponseBody
    public Result syncDbToZk(@RequestParam(value="validate") final String validate) {

        if(IPUtils.getFirstNoLoopbackIP4Address().equalsIgnoreCase(validate)) {

            threadPoolTaskExecutor.execute(new Runnable() {
                @Override
                public void run() {
                    try {
                        checkAndSyncServiceDB.loadFromDb();
                    } catch (DbException e1) {
                        logger.warn("load from db failed!try again!",e1);
                        try {
                            checkAndSyncServiceDB.loadFromDb();
                        } catch (DbException e2) {
                            logger.error("load from db failed!!",e2);
                        }
                    }

                    serviceGroupDbIndex = CheckAndSyncServiceDB.getServiceGroupDbIndex();
                    Set<String> hostSet = new HashSet<String>();

                    for (ServiceWithGroup serviceWithGroup : serviceGroupDbIndex.keySet()) {
                        final Service serviceDb = serviceGroupDbIndex.get(serviceWithGroup);
                        String hosts = serviceDb.getHosts();
                        String group = serviceDb.getGroup();
                        if (StringUtils.isNotBlank(hosts)) { // hosts有数据

                            // 更新zk server列表
                            String service_zk = Utils.escapeServiceName(serviceWithGroup.getService());
                            String serviceHostAddress = "/DP/SERVER/" + service_zk;
                            if (StringUtils.isNotBlank(group)) {
                                serviceHostAddress += "/" + group;
                            }

                            try {
                                client.set(serviceHostAddress, hosts);
                                logger.warn("update zk: " + serviceWithGroup + " with: " + hosts);
                            } catch (Throwable e) {
                                logger.error("write zk service error!!", e);
                            }

                            hostSet.addAll(Arrays.asList(hosts.split(",")));

                        }
                    }

                    // 更新zk weight列表
                    for (String host : hostSet) {
                        try {
                            String weightPath = "/DP/WEIGHT/" + host;
                            client.set(weightPath, 1);
                            logger.warn("update weight: " + host + " with: 1.");
                        } catch (Throwable e) {
                            logger.error("write zk weight error!!", e);
                        }
                    }

                }
            });

            return Result.createSuccessResult("start job...");

        } else {

            return Result.createErrorResult("failed to validate...");

        }

    }

    @RequestMapping(value = "/syncnode2dbold", method = {RequestMethod.POST})
    @ResponseBody
    public Result syncService2ServiceNode(@RequestParam(value="validate") final String validate) {

        if(IPUtils.getFirstNoLoopbackIP4Address().equalsIgnoreCase(validate)) {

            threadPoolTaskExecutor.execute(new Runnable() {
                @Override
                public void run() {
                    try {
                        checkAndSyncServiceDB.loadFromDb();
                    } catch (DbException e1) {
                        logger.warn("load from db failed!try again!",e1);
                        try {
                            checkAndSyncServiceDB.loadFromDb();
                        } catch (DbException e2) {
                            logger.error("load from db failed!!",e2);
                        }
                    }

                    serviceGroupDbIndex = CheckAndSyncServiceDB.getServiceGroupDbIndex();

                    for (ServiceWithGroup serviceWithGroup : serviceGroupDbIndex.keySet()) {
                        final Service serviceDb = serviceGroupDbIndex.get(serviceWithGroup);
                        String hosts = serviceDb.getHosts();
                        if(StringUtils.isNotBlank(hosts) && StringUtils.isBlank(serviceDb.getGroup())) { // 默认泳道有机器

                            Set<String> hostSet = new HashSet<String>();
                            hostSet.addAll(Arrays.asList(hosts.split(",")));
                            boolean needUpdate = false;

                            for(String host : hosts.split(",")) {
                                if(!host.startsWith("192.168") && !host.startsWith("10.66")) {
                                    hostSet.remove(host);
                                    needUpdate = true;
                                }
                            }

                            // 更新数据库和zk
                            if(needUpdate) {
                                String newHostList = StringUtils.join(hostSet, ",");
                                String service_zk = Utils.escapeServiceName(serviceWithGroup.getService());
                                String serviceHostAddress = "/DP/SERVER/" + service_zk;

                                try {
                                    client.set(serviceHostAddress, newHostList);
                                } catch (Exception e) {
                                    logger.error("write zk error! return!", e);
                                    return;
                                }

                                //update database
                                serviceDb.setHosts(newHostList);
                                serviceService.updateById(serviceDb);

                                logger.warn("update: " + serviceWithGroup + " with: " + newHostList);
                            }
                        }
                    }

                }
            });

            return Result.createSuccessResult("start job...");

        } else {

            return Result.createErrorResult("failed to validate...");

        }

    }

    @RequestMapping(value = "/betaonly/dellocalip", method = {RequestMethod.POST})
    @ResponseBody
    public Result dellocalip(@RequestParam(value="validate") final String validate) {

        if(IPUtils.getFirstNoLoopbackIP4Address().equalsIgnoreCase(validate)) {

            threadPoolTaskExecutor.execute(new Runnable() {
                @Override
                public void run() {
                    try {
                        checkAndSyncServiceDB.loadFromDb();
                    } catch (DbException e1) {
                        logger.warn("load from db failed!try again!",e1);
                        try {
                            checkAndSyncServiceDB.loadFromDb();
                        } catch (DbException e2) {
                            logger.error("load from db failed!!",e2);
                        }
                    }

                    serviceGroupDbIndex = CheckAndSyncServiceDB.getServiceGroupDbIndex();

                    for (ServiceWithGroup serviceWithGroup : serviceGroupDbIndex.keySet()) {
                        final Service serviceDb = serviceGroupDbIndex.get(serviceWithGroup);
                        String hosts = serviceDb.getHosts();
                        if(StringUtils.isNotBlank(hosts) && StringUtils.isBlank(serviceDb.getGroup())) { // 默认泳道有机器

                            Set<String> hostSet = new HashSet<String>();
                            hostSet.addAll(Arrays.asList(hosts.split(",")));
                            boolean needUpdate = false;

                            for(String host : hosts.split(",")) {
                                if(!host.startsWith("192.168") && !host.startsWith("10.66")) {
                                    hostSet.remove(host);
                                    needUpdate = true;
                                }
                            }

                            // 更新数据库和zk
                            if(needUpdate) {
                                String newHostList = StringUtils.join(hostSet, ",");
                                String service_zk = Utils.escapeServiceName(serviceWithGroup.getService());
                                String serviceHostAddress = "/DP/SERVER/" + service_zk;

                                try {
                                    client.set(serviceHostAddress, newHostList);
                                } catch (Exception e) {
                                    logger.error("write zk error! return!", e);
                                    return;
                                }

                                //update database
                                serviceDb.setHosts(newHostList);
                                serviceService.updateById(serviceDb);

                                logger.warn("update: " + serviceWithGroup + " with: " + newHostList);
                            }
                        }
                    }

                }
            });

            return Result.createSuccessResult("start job...");

        } else {

            return Result.createErrorResult("failed to validate...");

        }

    }

    @RequestMapping(value = {"/syncnode2db"}, method = {RequestMethod.POST})
    @ResponseBody
    public Result syncnode2db(@RequestParam(value="validate") final String validate,
                         HttpServletRequest request, HttpServletResponse response) {
        if(IPUtils.getFirstNoLoopbackIP4Address().equalsIgnoreCase(validate)) {
            threadPoolTaskExecutor.execute(new Runnable() {
                @Override
                public void run() {
                    checkAndSyncServiceNodeDB.checkAndSyncDB();
                }
            });
            return Result.createSuccessResult("start sync db...");
        } else {
            return Result.createErrorResult("failed to validate...");
        }

    }

    @RequestMapping(value = {"/syncdb"}, method = {RequestMethod.POST})
    @ResponseBody
    public Result syncdb(@RequestParam(value="validate") final String validate,
                              HttpServletRequest request, HttpServletResponse response) {
        if(IPUtils.getFirstNoLoopbackIP4Address().equalsIgnoreCase(validate)) {
            threadPoolTaskExecutor.execute(new Runnable() {
                @Override
                public void run() {
                    checkAndSyncServiceDB.checkAndSyncDB();
                }
            });
            return Result.createSuccessResult("start sync db...");
        } else {
            return Result.createErrorResult("failed to validate...");
        }

    }

    @RequestMapping(value = {"/loglevel"}, method = {RequestMethod.GET})
    @ResponseBody
    public Result loglevel(HttpServletRequest request, HttpServletResponse response) {
        logger.trace("trace");
        logger.debug("debug");
        logger.info("info");
        logger.warn("warn");
        logger.error("error");
        logger.fatal("fatal");

        return Result.createSuccessResult("success!");
    }


    @RequestMapping(value = {"/shs/test1"},method = {RequestMethod.GET})
    public String testVelocity(HttpServletRequest request, HttpServletResponse response){
        return "/config/GroupConfig";
    }

}
