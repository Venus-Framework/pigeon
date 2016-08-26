package com.dianping.pigeon.registry.mns;

import com.dianping.pigeon.log.LoggerLoader;
import com.dianping.pigeon.util.VersionUtils;
import com.google.common.collect.Maps;
import com.sankuai.sgagent.thrift.model.fb_status;
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

    public final static int MNS_WEIGHT_ACTIVE_DEFAULT = 10;
    public final static int MNS_WEIGHT_INACTIVE_DEFAULT = 0;
    public final static double MNS_FWEIGHT_ACTIVE_DEFAULT = 10.d;
    public final static double MNS_FWEIGHT_INACTIVE_DEFAULT = 0.d;

    // 框架层权重
    public final static int WEIGHT_ACTIVE = 1;
    public final static int WEIGHT_INACTIVE = 0;


    private static final Map<String, String> hostRemoteAppkeyMapping = Maps.newConcurrentMap();

    public static Map<String, String> getHostRemoteAppkeyMapping() {
        return hostRemoteAppkeyMapping;
    }

    public static int getMtthriftWeight(int weight) {

        return MNS_WEIGHT_ACTIVE_DEFAULT;
    }

    public static double getMtthriftFWeight(int weight) {

        return MNS_FWEIGHT_ACTIVE_DEFAULT;
    }

    public static int getMtthriftStatus(int weight) {
        int status;

        if (weight == WEIGHT_INACTIVE) {
            status = fb_status.DEAD.getValue();//dead
        } else if (weight > WEIGHT_INACTIVE) {
            status = fb_status.ALIVE.getValue();//alive
        } else {
            status = fb_status.ALIVE.getValue();
        }

        return status;
    }

    public static int getWeight(int mtthriftStatus) {
        int weight;

        if (mtthriftStatus == fb_status.ALIVE.getValue()) {
            weight = WEIGHT_ACTIVE;
        } else {
            weight = WEIGHT_INACTIVE;
        }

        return weight;
    }

    public static int getWeight(int mtthriftStatus, int mtthriftWeight) {
        int weight;

        if (mtthriftStatus == fb_status.ALIVE.getValue()
                && mtthriftWeight > MNS_WEIGHT_INACTIVE_DEFAULT) {
            weight = WEIGHT_ACTIVE;
        } else {
            weight = WEIGHT_INACTIVE;
        }

        return weight;
    }

    public static boolean checkVersion(String version) {
        // support new mtthrift and new/old pigeon
        if (version.startsWith(VersionUtils.MT_THRIFT_VERSION_BASE)) {
            return VersionUtils.isThriftSupported(version);
        }

        return true;
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
}
