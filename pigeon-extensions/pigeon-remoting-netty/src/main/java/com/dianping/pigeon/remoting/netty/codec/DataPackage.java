package com.dianping.pigeon.remoting.netty.codec;

import org.jboss.netty.buffer.ChannelBuffer;

/**
 * @author qi.yin
 *         2016/06/16  下午1:37.
 */
public class DataPackage {

    private ChannelBuffer frameBuffer;

    private boolean isUnified;

    public DataPackage(ChannelBuffer frameBuffer,boolean isUnified) {
        this.frameBuffer = frameBuffer;
        this.isUnified = isUnified;
    }

    public ChannelBuffer getFrameBuffer() {
        return frameBuffer;
    }

    public void setFrameBuffer(ChannelBuffer frameBuffer) {
        this.frameBuffer = frameBuffer;
    }

    public boolean isUnified() {
        return isUnified;
    }

    public void setIsUnified(boolean isUnified) {
        this.isUnified = isUnified;
    }
}
