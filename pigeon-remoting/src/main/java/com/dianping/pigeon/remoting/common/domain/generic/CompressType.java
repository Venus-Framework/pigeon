package com.dianping.pigeon.remoting.common.domain.generic;

/**
 * @author qi.yin
 *         2016/06/03  下午2:11.
 */
public enum CompressType {

    None((byte) 0),       // 不压缩
    Snappy((byte) 1),     // Snappy
    Gzip((byte) 2);        // Gzip

    private byte code;

    private CompressType(byte code) {
        this.code = code;
    }

    public byte getCode() {
        return this.code;
    }

    public static CompressType getCompressType(byte code) {
        switch (code) {
            case 0:
                return None;
            case 1:
                return Snappy;
            case 2:
                return Gzip;
            default:
                throw new IllegalArgumentException("invalid CompressType code: " + code);
        }
    }
}
