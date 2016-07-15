package com.dianping.pigeon.registry.mns;

/**
 * Created by chenchongze on 16/6/13.
 */
public class MnsUtils {


    /**
     * 注册服务, uptCmd:
     * 0,重置(代表后面的serviceName list就是该应用支持的全量接口)，
     * 1，增加(代表后面的serviceName list是该应用新增的接口)，
     * 2，减少(代表后面的serviceName list是该应用删除的接口)。
     */
    public final static int UPT_CMD_RESET = 0;
    public final static int UPT_CMD_ADD = 1;
    public final static int UPT_CMD_DEL = 2;

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
        int weight = -1;

        if (mtthrift_status == 4) {
            weight = -1;
        } else if (mtthrift_status == 0) {
            weight = 0;
        } else if (mtthrift_status == 2 && mtthrift_weight > 0) {
            weight = 1;
        }

        return weight;
    }
}
