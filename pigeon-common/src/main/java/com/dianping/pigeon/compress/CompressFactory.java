package com.dianping.pigeon.compress;

/**
 * @author qi.yin
 *         2016/09/30  上午9:39.
 */
public class CompressFactory {

    private static Compress gzipCompress = new GZipCompress();

    private static Compress snappyCompress = new SnappyCompress();

    private CompressFactory() {

    }

    public static Compress getGZipCompress() {
        return gzipCompress;
    }

    public static Compress getSnappyCompress() {
        return snappyCompress;
    }
}
