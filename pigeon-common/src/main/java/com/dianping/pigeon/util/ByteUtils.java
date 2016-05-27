package com.dianping.pigeon.util;

/**
 * @author qi.yin
 *         2016/05/23  上午12:01.
 */
public class ByteUtils {

    public static byte[] copyOf(byte[] src, int length) {
        byte[] dest = new byte[length];
        System.arraycopy(src, 0, dest, 0, Math.min(src.length, length));
        return dest;
    }
}
