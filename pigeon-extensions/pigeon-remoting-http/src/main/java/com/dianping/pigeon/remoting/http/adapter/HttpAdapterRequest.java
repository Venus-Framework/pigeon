package com.dianping.pigeon.remoting.http.adapter;

import com.dianping.pigeon.remoting.common.util.Constants;

/**
 * Created by chenchongze on 16/1/8.
 */
public class HttpAdapterRequest {

    private String url;

    private String method;

    private Object[] parameters;

    private int timeout = 0;

    private byte serialize;

    private long seq;

    private int callType = Constants.CALLTYPE_REPLY;

    private int messageType = Constants.MESSAGE_TYPE_SERVICE;

    public HttpAdapterRequest(String url, String method, Object[] parameters, int timeout, byte serialize, long seq) {
        this.url = url;
        this.method = method;
        this.parameters = parameters;
        this.timeout = timeout;
        this.serialize = serialize;
        this.seq = seq;
    }

    public HttpAdapterRequest(String url, String method, Object[] parameters, int timeout, byte serialize, long seq, int callType, int messageType) {
        this.url = url;
        this.method = method;
        this.parameters = parameters;
        this.timeout = timeout;
        this.serialize = serialize;
        this.seq = seq;
        this.callType = callType;
        this.messageType = messageType;
    }

    public HttpAdapterRequest() {
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getMethod() {
        return method;
    }

    public void setMethod(String method) {
        this.method = method;
    }

    public Object[] getParameters() {
        return parameters;
    }

    public void setParameters(Object[] parameters) {
        this.parameters = parameters;
    }

    public int getTimeout() {
        return timeout;
    }

    public void setTimeout(int timeout) {
        this.timeout = timeout;
    }

    public byte getSerialize() {
        return serialize;
    }

    public void setSerialize(byte serialize) {
        this.serialize = serialize;
    }

    public long getSeq() {
        return seq;
    }

    public void setSeq(long seq) {
        this.seq = seq;
    }

    public int getCallType() {
        return callType;
    }

    public void setCallType(int callType) {
        this.callType = callType;
    }

    public int getMessageType() {
        return messageType;
    }

    public void setMessageType(int messageType) {
        this.messageType = messageType;
    }
}
