package com.dianping.pigeon.remoting.netty.codec;

import com.dianping.pigeon.remoting.common.domain.InvocationSerializable;
import org.jboss.netty.buffer.ChannelBuffer;

/**
 * @author qi.yin
 *         2016/06/16  下午1:37.
 */
public class CodecEvent {

    private ChannelBuffer buffer;

    private InvocationSerializable invocation;

    private boolean isUnified;

    private boolean isCompress;

    private boolean isChecksum;

    private long receiveTime;

    private boolean isValid;

    public CodecEvent() {
        isValid = true;
    }

    public CodecEvent(ChannelBuffer buffer, boolean isUnified) {
        this();
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

    public long getReceiveTime() {
        return receiveTime;
    }

    public void setReceiveTime(long receiveTime) {
        this.receiveTime = receiveTime;
    }

    public boolean isValid() {
        return isValid;
    }

    public void setIsValid(boolean isValid) {
        this.isValid = isValid;
    }

    public InvocationSerializable getInvocation() {
        return invocation;
    }

    public void setInvocation(InvocationSerializable invocation) {
        this.invocation = invocation;
    }
}
