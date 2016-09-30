package com.dianping.pigeon.compress;

import org.xerial.snappy.Snappy;

import java.io.IOException;

/**
 * @author qi.yin
 *         2016/06/05  下午6:02.
 */
public class SnappyCompress implements Compress {

    @Override
    public byte[] compress(byte[] buf) throws IOException {
        if (buf == null) {
            return null;
        }
        return Snappy.compress(buf);
    }

    @Override
    public byte[] unCompress(byte[] buf) throws IOException {
        if (buf == null) {
            return null;
        }
        return Snappy.uncompress(buf);
    }
}
