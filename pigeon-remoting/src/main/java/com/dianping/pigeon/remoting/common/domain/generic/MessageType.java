package com.dianping.pigeon.remoting.common.domain.generic;

/**
 * @author qi.yin
 *         2016/06/03  下午2:06.
 */
public enum MessageType {

    Normal((byte) 0),    // 正常消息
    Heartbeat((byte) 1), // 心跳消息
    ScannerHeartbeat((byte) 2); // scanner 心跳消息

    private byte code;

    private MessageType(byte code) {
        this.code = code;
    }

    public byte getCode() {
        return code;
    }

    public static MessageType getMessageType(byte code) {
        switch (code) {
            case 0:
                return Normal;
            case 1:
                return Heartbeat;
            default:
                throw new IllegalArgumentException("invalid MessageType code: " + code);
        }
    }

}
