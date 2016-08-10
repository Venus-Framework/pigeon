package com.dianping.pigeon.registry.mns;

import com.dianping.pigeon.log.LoggerLoader;
import com.dianping.pigeon.util.VersionUtils;
import com.google.common.collect.Maps;
import org.apache.commons.lang.StringUtils;
import org.apache.logging.log4j.Logger;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Created by chenchongze on 16/6/13.
 */
public class MnsUtils {

    private final static Logger logger = LoggerLoader.getLogger(MnsUtils.class);

    /**
     * 注册服务, uptCmd:
     * 0,重置(代表后面的serviceName list就是该应用支持的全量接口)，
     * 1，增加(代表后面的serviceName list是该应用新增的接口)，
     * 2，减少(代表后面的serviceName list是该应用删除的接口)。
     */
    public final static int UPT_CMD_RESET = 0;
    public final static int UPT_CMD_ADD = 1;
    public final static int UPT_CMD_DEL = 2;

    private static final Map<String, String> hostRemoteAppkeyMapping = Maps.newConcurrentMap();

    public static Map<String, String> getHostRemoteAppkeyMapping() {
        return hostRemoteAppkeyMapping;
    }

    public static int getMtthriftWeight(int pigeon_weight) {
        int mtThriftWeight = 0;

        if (pigeon_weight <= 0) {
            mtThriftWeight = 0;//stopped
        }  else if (pigeon_weight > 0) {
            mtThriftWeight = 10;//alive
        }

        return mtThriftWeight;
    }

    public static double getMtthriftFWeight(int pigeon_weight) {
        double mtThriftWeight = 0;

        if (pigeon_weight <= 0) {
            mtThriftWeight = 0.d;//stopped
        }  else if (pigeon_weight > 0) {
            mtThriftWeight = 10.d;//alive
        }

        return mtThriftWeight;
    }

    public static int getMtthriftStatus(int pigeon_weight) {
        int status = 0;

        if (pigeon_weight < 0) {
            status = 4;//stopped
        } else if (pigeon_weight == 0) {
            status = 0;//dead
        } else if (pigeon_weight > 0) {
            status = 2;//alive
        }

        return status;
    }

    public static int getPigeonWeight(int mtthrift_status, int mtthrift_weight) {
        int weight = 1;

        if (mtthrift_status == 0 || mtthrift_status == 4) {
            weight = 0;
        } else if (mtthrift_status == 2 && mtthrift_weight > 0) {
            weight = 1;
        }

        return weight;
    }

    public static List<String[]> getServiceIpPortList(String serviceAddress) {
        List<String[]> result = new ArrayList<String[]>();

        if (StringUtils.isNotBlank(serviceAddress)) {
            String[] hostArray = serviceAddress.split(",");

            for (String host : hostArray) {
                int idx = host.lastIndexOf(":");

                if (idx != -1) {
                    String ip = null;
                    int port = -1;

                    try {
                        ip = host.substring(0, idx);
                        port = Integer.parseInt(host.substring(idx + 1));
                    } catch (RuntimeException e) {
                        logger.warn("invalid host: " + host + ", ignored!");
                    }

                    if (ip != null && port > 0) {
                        result.add(new String[] { ip, port + "" });
                    }

                } else {
                    logger.warn("invalid host: " + host + ", ignored!");
                }
            }
        }

        return result;
    }

    public static boolean checkVersion(String version) {
        // support new mtthrift and new/old pigeon
        if (version.startsWith(VersionUtils.MT_THRIFT_VERSION_BASE)) {
            return VersionUtils.isThriftSupported(version);
        }

        return true;
    }
}
