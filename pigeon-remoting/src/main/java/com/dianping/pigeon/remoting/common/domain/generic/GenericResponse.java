package com.dianping.pigeon.remoting.common.domain.generic;

import com.dianping.pigeon.remoting.common.util.Constants;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;

import java.io.Serializable;
import java.util.Map;

/**
 * @author qi.yin
 *         2016/05/12  下午4:29.
 */
public class GenericResponse implements UnifiedResponse {

    private static final long serialVersionUID = -1L;

    private transient byte serialize;

    private byte protocalVersion;

    private long seq;

    private int messageType;

    private transient String serviceName;

    private transient String methodName;

    private transient Throwable exception;

    private Object returnVal;

    private int compressType;

    private transient int size;

    private transient long createMillisTime;

    private Map<String, String> globalContext = null;

    private Map<String, String> localContext = null;

    public GenericResponse() {

    }

    public GenericResponse(int messageType, byte serialize) {
        this.messageType = messageType;
        this.serialize = serialize;
    }

    public GenericResponse(byte serialize, long seq, int messageType, Object returnVal) {
        this.serialize = serialize;
        this.seq = seq;
        this.messageType = messageType;
        this.returnVal = returnVal;
    }

    public byte getSerialize() {
        return this.serialize;
    }

    public void setSequence(long seq) {
        this.seq = seq;
    }

    public long getSequence() {
        return this.seq;
    }

    public byte getProtocalVersion() {
        return protocalVersion;
    }

    public void setProtocalVersion(byte protocalVersion) {
        this.protocalVersion = protocalVersion;
    }

    public Object getObject() {
        return this;
    }

    public void setMessageType(int messageType) {
        this.messageType = messageType;
    }

    public int getMessageType() {
        return this.messageType;
    }

    public String getServiceName() {
        return serviceName;
    }

    public void setServiceName(String serviceName) {
        this.serviceName = serviceName;
    }

    public String getMethodName() {
        return methodName;
    }

    public void setMethodName(String methodName) {
        this.methodName = methodName;
    }

    public String getCause() {
        throw new UnsupportedOperationException("operation not supported.");
    }

    public Object getReturn() {
        return this.returnVal;
    }

    @Override
    public Object getContext() {
        throw new UnsupportedOperationException("operation not supported.");
    }

    @Override
    public void setContext(Object context) {
        throw new UnsupportedOperationException("operation not supported.");
    }

    public Throwable getException() {
        return exception;
    }

    public void setException(Throwable exception) {
        this.exception = exception;
    }

    @Override
    public void setReturn(Object obj) {
        this.returnVal = obj;
    }


    public int getCompressType() {
        return compressType;
    }

    public void setCompressType(int compressType) {
        this.compressType = compressType;
    }

    @Override
    public String toString() {
        if (this.messageType == Constants.MESSAGE_TYPE_SERVICE) {
            return new ToStringBuilder(this, ToStringStyle.SHORT_PREFIX_STYLE).append("serialize", serialize)
                    .append("seq", seq).append("messageType", messageType).toString();
        } else {
            return new ToStringBuilder(this, ToStringStyle.SHORT_PREFIX_STYLE).append("serialize", serialize)
                    .append("seq", seq).append("messageType", messageType).append("return", returnVal).toString();
        }
    }

    @Override
    public void setSerialize(byte serialize) {
        this.serialize = serialize;
    }

    public int getSize() {
        return size;
    }

    public void setSize(int size) {
        this.size = size;
    }

    public Map<String, Serializable> getResponseValues() {
        throw new UnsupportedOperationException("operation not supported.");
    }

    public void setResponseValues(Map<String, Serializable> responseValues) {
        throw new UnsupportedOperationException("operation not supported.");
    }

    public long getCreateMillisTime() {
        return createMillisTime;
    }

    public void setCreateMillisTime(long createMillisTime) {
        this.createMillisTime = createMillisTime;
    }

    public Map<String, String> getGlobalContext() {
        return globalContext;
    }

    public void setGlobalContext(Map<String, String> globalContext) {
        this.globalContext = globalContext;
    }

    public Map<String, String> getLocalContext() {
        return localContext;
    }

    public void setLocalContext(Map<String, String> localContext) {
        this.localContext = localContext;
    }
}
