package com.dianping.pigeon.remoting.common.domain.generic;

/**
 * @author qi.yin
 *         2016/06/03  下午2:13.
 */
public enum CallType {

    Reply((byte) 0),    // 需要响应
    NoReply((byte) 1);  // 不需要响应

    private byte code;

    private CallType(byte code) {
        this.code = code;
    }

    public byte getCode() {
        return this.code;
    }

    public static CallType getCallType(byte code) {
        switch (code) {
            case 0:
                return Reply;
            case 1:
                return NoReply;
            default:
                throw new IllegalArgumentException("invalid CallType code: " + code);
        }
    }
}
