package com.dianping.pigeon.registry.util;

/**
 * Created by chenchongze on 16/8/16.
 */
public enum HeartBeatSupport {

    UNSUPPORT((short) 0),
    CLIENTTOSERVER((short) 1),
    SCANNER((short) 2),
    BOTH((short) 3);

    private final short value;

    private HeartBeatSupport(short value) {
        this.value = value;
    }

    public short getValue() {
        return value;
    }
}
