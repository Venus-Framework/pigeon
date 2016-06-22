package com.dianping.pigeon.remoting.netty.codec;

import org.jboss.netty.buffer.ChannelBuffer;

/**
 * @author qi.yin
 *         2016/06/16  下午1:37.
 */
public class CodecEvent {

    private ChannelBuffer buffer;

    private boolean isUnified;

    private boolean isCompress;

    private boolean isChecksum;

    public CodecEvent() {

    }

    public CodecEvent(ChannelBuffer buffer, boolean isUnified) {
        this.buffer = buffer;
        this.isUnified = isUnified;
    }

    public ChannelBuffer getBuffer() {
        return buffer;
    }

    public void setBuffer(ChannelBuffer buffer) {
        this.buffer = buffer;
    }

    public boolean isUnified() {
        return isUnified;
    }

    public void setIsUnified(boolean isUnified) {
        this.isUnified = isUnified;
    }

    public boolean isCompress() {
        return isCompress;
    }

    public void setIsCompress(boolean isCompress) {
        this.isCompress = isCompress;
    }

    public boolean isChecksum() {
        return isChecksum;
    }

    public void setIsChecksum(boolean isChecksum) {
        this.isChecksum = isChecksum;
    }
}
