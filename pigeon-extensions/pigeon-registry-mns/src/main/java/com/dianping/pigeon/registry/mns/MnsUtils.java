package com.dianping.pigeon.registry.mns;

/**
 * Created by chenchongze on 16/6/13.
 */
public class MnsUtils {

    public static int getMtthriftStatus(int pigeon_weight) {
        int status = 0;

        if (pigeon_weight < 0) {
            status = 4;
        } else if (pigeon_weight == 0) {
            status = 0;
        } else if (pigeon_weight > 0) {
            status = 2;
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
