package com.dianping.pigeon.registry.mns.mock;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import org.apache.commons.lang.StringUtils;
import org.apache.thrift.TException;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Created by chenchongze on 16/6/3.
 */
public class MnsInvoker {

    private static Set<SGService> sgServices = Sets.newConcurrentHashSet();

    // serviceId --> sgService
    private static Map<String, List<SGService>> sgServiceMapByServiceId = Maps.newConcurrentMap();

    // hostId --> sgService
    private static Map<String, List<SGService>> sgServiceMapByHostId = Maps.newConcurrentMap();



    public static void registerService(SGService sgService) throws TException {
        sgServices.add(sgService);

        if (sgServiceMapByServiceId.containsKey(sgService.getServiceId())) {
            List<SGService> services = sgServiceMapByServiceId.get(sgService.getServiceId());
            services.add(sgService);
        } else {
            List<SGService> services = Lists.newArrayList();
            services.add(sgService);
            sgServiceMapByServiceId.put(sgService.getServiceId(), services);
        }

        if (sgServiceMapByHostId.containsKey(sgService.getHostId())) {
            List<SGService> services = sgServiceMapByHostId.get(sgService.getHostId());
            services.add(sgService);
        } else {
            List<SGService> services = Lists.newArrayList();
            services.add(sgService);
            sgServiceMapByHostId.put(sgService.getHostId(), services);
        }
    }

    public static void unRegisterService(SGService sgService) throws TException {
        sgServices.remove(sgService);

        if (sgServiceMapByServiceId.containsKey(sgService.getServiceId())) {
            List<SGService> services = sgServiceMapByServiceId.get(sgService.getServiceId());
            services.remove(sgService);
        }

        if (sgServiceMapByHostId.containsKey(sgService.getHostId())) {
            List<SGService> services = sgServiceMapByHostId.get(sgService.getHostId());
            services.remove(sgService);
        }
    }

    public static List<SGService> getServiceList(ServiceListRequest req) {

        if (StringUtils.isNotBlank(req.getAppkey()) && StringUtils.isNotBlank(req.getServiceName())) {
            if (sgServiceMapByServiceId.containsKey(req.getServiceId())) {
                return sgServiceMapByServiceId.get(req.getServiceId());
            }
        } else if (StringUtils.isNotBlank(req.getIp()) && req.getPort() != 0) {
            if (sgServiceMapByHostId.containsKey(req.getHostId())) {
                return sgServiceMapByHostId.get(req.getHostId());
            }
        }

        return new ArrayList<SGService>();
    }

    public static void getStatistics() {
        for (String serviceId : sgServiceMapByServiceId.keySet()) {
            System.out.println(serviceId);
            List<SGService> services = sgServiceMapByServiceId.get(serviceId);
            for (SGService service : services) {
                System.out.println("    " + service.getHostId());
            }
        }
    }
}
