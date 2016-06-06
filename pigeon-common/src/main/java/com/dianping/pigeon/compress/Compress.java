package com.dianping.pigeon.compress;

import java.io.IOException;

/**
 * @author qi.yin
 *         2016/06/05  下午5:56.
 */
public interface Compress {
    /**
     * 压缩
     *
     * @param buf
     * @return
     */
    byte[] compress(byte[] buf) throws IOException;

    /**
     * 解压缩
     *
     * @param buf
     * @return
     */
    byte[] unCompress(byte[] buf) throws IOException;
}
