package com.dianping.pigeon.remoting.common.codec.thrift;

import com.dianping.pigeon.util.ByteUtils;

import java.io.IOException;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;

/**
 * @author qi.yin
 *         2016/05/23  上午12:03.
 */
public class DynamicByteArrayOutputStream extends OutputStream {

    protected byte buffer[];

    protected int count;

    public DynamicByteArrayOutputStream() {

        this(218);
    }

    public DynamicByteArrayOutputStream(int size) {

        if (size < 0)
            throw new IllegalArgumentException("Negative initial size: " + size);
        buffer = new byte[size];
    }

    public void write(int b) {

        int newcount = count + 1;
        if (newcount > buffer.length)
            buffer = ByteUtils.copyOf(buffer, Math.max(buffer.length << 1, newcount));
        buffer[count] = (byte) b;
        count = newcount;
    }

    public void write(byte b[], int off, int len) {

        if ((off < 0) || (off > b.length) || (len < 0) || ((off + len) > b.length) || ((off + len) < 0))
            throw new IndexOutOfBoundsException();
        if (len == 0)
            return;
        int newcount = count + len;
        if (newcount > buffer.length)
            buffer = ByteUtils.copyOf(buffer, Math.max(buffer.length << 1, newcount));
        System.arraycopy(b, off, buffer, count, len);
        count = newcount;
    }

    public int size() {

        return count;
    }

    public void setWriteIndex(int index) {
        count = index;
    }

    public void reset() {

        count = 0;
    }

    public byte[] toByteArray() {

        return ByteUtils.copyOf(buffer, count);
    }

    public ByteBuffer toByteBuffer() {

        return ByteBuffer.wrap(buffer, 0, count);
    }

    public void writeTo(OutputStream out) throws IOException {

        out.write(buffer, 0, count);
    }

    public String toString() {

        return new String(buffer, 0, count);
    }

    public String toString(String charset) throws UnsupportedEncodingException {

        return new String(buffer, 0, count, charset);
    }

    public void close() throws IOException {
    }
}
