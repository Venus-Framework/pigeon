package com.dianping.pigeon.remoting.common.codec.thrift;


import java.io.ByteArrayOutputStream;

/**
 * @author qi.yin
 *         2016/05/23  上午12:03.
 */
public class DynamicByteArrayOutputStream extends ByteArrayOutputStream {

    public DynamicByteArrayOutputStream() {
        super(218);
    }

    public DynamicByteArrayOutputStream(int size) {
        super(size);
    }

    public void setWriteIndex(int index) {
        count = index;
    }

}
