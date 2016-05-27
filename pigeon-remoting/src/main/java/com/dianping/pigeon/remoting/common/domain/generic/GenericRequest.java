package com.dianping.pigeon.remoting.common.domain.generic;


import com.dianping.pigeon.config.ConfigManagerLoader;
import com.dianping.pigeon.remoting.common.util.Constants;
import com.dianping.pigeon.remoting.common.util.InvocationUtils;
import com.dianping.pigeon.remoting.invoker.config.InvokerConfig;
import com.dianping.pigeon.remoting.invoker.domain.InvokerContext;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;

import java.io.Serializable;
import java.util.Map;

/**
 * @author qi.yin
 *         2016/05/12  下午4:28.
 */
public class GenericRequest implements UnifiedRequest {

    private static final long serialVersionUID = -1L;

    private transient byte serialize;

    private byte protocalVersion;

    private long seq;

    private int callType;

    private int messageType;

    private int compressType;

    private int timeout;

    private transient long createMillisTime;

    private String serviceName;

    private String methodName;

    private Object[] parameters;

    private Class<?>[] parameterTypes;

    private String app = ConfigManagerLoader.getConfigManager().getAppName();

    private transient int size;

    private Map<String, String> globalContext = null;

    private Map<String, String> localContext = null;

    private String version;

    public GenericRequest(String serviceName, String methodName, Object[] parameters, byte serialize, int messageType,
                          int timeout, int callType, long seq) {
        this.serviceName = serviceName;
        this.methodName = methodName;
        this.parameters = parameters;
        this.serialize = serialize;
        this.messageType = messageType;
        this.timeout = timeout;
        this.callType = callType;
        this.seq = seq;
    }


    public GenericRequest() {
    }

    public GenericRequest(InvokerContext invokerContext) {
        if (invokerContext != null) {
            InvokerConfig<?> invokerConfig = invokerContext.getInvokerConfig();
            if (invokerConfig != null) {
                this.serviceName = invokerConfig.getUrl();
                this.serialize = invokerConfig.getSerialize();
                this.timeout = invokerConfig.getTimeout();
                this.setVersion(invokerConfig.getVersion());
                if (Constants.CALL_ONEWAY.equalsIgnoreCase(invokerConfig.getCallType())) {
                    this.setCallType(Constants.CALLTYPE_NOREPLY);
                } else {
                    this.setCallType(Constants.CALLTYPE_REPLY);
                }
            }
            this.methodName = invokerContext.getMethodName();
            this.parameters = invokerContext.getArguments();
            this.messageType = Constants.MESSAGE_TYPE_SERVICE;
        }
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
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

    @Override
    public Object getContext() {
        throw new UnsupportedOperationException("operation not supported.");
    }

    @Override
    public void setContext(Object context) {
        throw new UnsupportedOperationException("operation not supported.");
    }

    public void setCallType(int callType) {
        this.callType = callType;
    }

    public int getCallType() {
        return this.callType;
    }

    public int getTimeout() {
        return this.timeout;
    }

    public long getCreateMillisTime() {
        return this.createMillisTime;
    }

    public String getServiceName() {
        return this.serviceName;
    }

    public String getMethodName() {
        return this.methodName;
    }

    public String[] getParamClassName() {
        if (this.parameters == null) {
            return new String[0];
        }
        String[] paramClassNames = new String[this.parameters.length];

        int k = 0;
        for (Object parameter : this.parameters) {
            if (parameter == null) {
                paramClassNames[k] = "NULL";
            } else {
                paramClassNames[k] = this.parameters[k].getClass().getName();
            }
            k++;
        }
        return paramClassNames;
    }

    public Object[] getParameters() {
        return this.parameters;
    }

    public void setParameters(Object[] parameters) {
        this.parameters = parameters;
    }

    public Class<?>[] getParameterTypes() {
        return parameterTypes;
    }

    public void setParameterTypes(Class<?>[] parameterTypes) {
        this.parameterTypes = parameterTypes;
    }

    public int getMessageType() {
        return this.messageType;
    }

    public int getCompressType() {
        return compressType;
    }

    public void setCompressType(int compressType) {
        this.compressType = compressType;
    }

    @Override
    public void setCreateMillisTime(long createTime) {
        this.createMillisTime = createTime;
    }

    public void setTimeout(int timeout) {
        this.timeout = timeout;
    }

    @Override
    public String toString() {
        ToStringBuilder builder = new ToStringBuilder(this, ToStringStyle.SHORT_PREFIX_STYLE)
                .append("serialize", serialize).append("seq", seq).append("msgType", messageType)
                .append("callType", callType).append("timeout", timeout).append("url", serviceName)
                .append("method", methodName).append("created", createMillisTime);
        if (Constants.LOG_PARAMETERS) {
            builder.append("parameters", InvocationUtils.toJsonString(parameters));
        }

        return builder.toString();
    }

    @Override
    public void setSerialize(byte serialize) {
        this.serialize = serialize;
    }

    @Override
    public void setMessageType(int messageType) {
        this.messageType = messageType;
    }

    @Override
    public void setServiceName(String serviceName) {
        this.serviceName = serviceName;
    }

    public String getApp() {
        return app;
    }

    public void setApp(String app) {
        this.app = app;
    }

    public void setMethodName(String methodName) {
        this.methodName = methodName;
    }

    public int getSize() {
        return size;
    }

    public void setSize(int size) {
        this.size = size;
    }

    public Map<String, Serializable> getGlobalValues() {
        throw new UnsupportedOperationException("operation not supported.");
    }

    public void setGlobalValues(Map<String, Serializable> globalValues) {
        throw new UnsupportedOperationException("operation not supported.");
    }

    public Map<String, Serializable> getRequestValues() {
        throw new UnsupportedOperationException("operation not supported.");
    }

    public void setRequestValues(Map<String, Serializable> requestValues) {
        throw new UnsupportedOperationException("operation not supported.");
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
