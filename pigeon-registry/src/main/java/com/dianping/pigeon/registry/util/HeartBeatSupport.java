package com.dianping.pigeon.registry.util;

/**
 * Created by chenchongze on 16/8/16.
 */
public enum HeartBeatSupport {

    UNSUPPORT((byte)0),
    CLIENTTOSERVER((byte)1),
    SCANNER((byte)2),
    BOTH((byte)3);

    private final byte value;

    private HeartBeatSupport(byte value) {
        this.value = value;
    }

    public byte getValue() {
        return value;
    }

    public static HeartBeatSupport findByValue(byte value) {
        switch(value) {
            case 0:
                return UNSUPPORT;
            case 1:
                return CLIENTTOSERVER;
            case 2:
                return SCANNER;
            case 3:
                return BOTH;
            default:
                return null;
        }
    }
}
