/**
 * Dianping.com Inc.
 * Copyright (c) 2003-2013 All Rights Reserved.
 */
package com.dianping.pigeon.remoting.common.domain;

import java.io.Serializable;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;

import com.dianping.pigeon.config.ConfigManagerLoader;
import com.dianping.pigeon.remoting.common.util.Constants;
import com.dianping.pigeon.remoting.common.util.InvocationUtils;
import com.dianping.pigeon.remoting.invoker.config.InvokerConfig;
import com.dianping.pigeon.remoting.invoker.domain.InvokerContext;
import com.dianping.pigeon.util.LangUtils;

public class CompactRequest implements InvocationRequest {

	private static final long serialVersionUID = 0;

	private byte serialize;

	private long seq;

	private int callType = Constants.CALLTYPE_REPLY;

	private int timeout = 0;

	private transient long createMillisTime;

	private int id;

	private transient String serviceName;

	private transient String methodName;

	private Object[] parameters;

	private int messageType = Constants.MESSAGE_TYPE_SERVICE;

	private Object context;

	private String app = ConfigManagerLoader.getConfigManager().getAppName();

	private transient int size;

	private Map<String, Serializable> globalValues = null;

	private Map<String, Serializable> requestValues = null;

	public static transient ConcurrentHashMap<Integer, ServiceId> PROVIDER_ID_MAP = new ConcurrentHashMap<Integer, ServiceId>();

	public static transient ConcurrentHashMap<String, Integer> METHOD_ID_MAP = new ConcurrentHashMap<String, Integer>();

	public CompactRequest() {
	}

	public CompactRequest(InvokerContext invokerContext) {
		if (invokerContext != null) {
			InvokerConfig<?> invokerConfig = invokerContext.getInvokerConfig();
			if (invokerConfig != null) {
				this.serialize = invokerConfig.getSerialize();
				this.timeout = invokerConfig.getTimeout();
				// this.setVersion(invokerConfig.getVersion());
				if (Constants.CALL_ONEWAY.equalsIgnoreCase(invokerConfig.getCallType())) {
					this.setCallType(Constants.CALLTYPE_NOREPLY);
				} else {
					this.setCallType(Constants.CALLTYPE_REPLY);
				}
				this.serviceName = invokerConfig.getUrl();
				this.methodName = invokerContext.getMethodName();
				this.id = LangUtils.hash(serviceName + "#" + methodName, 0, Integer.MAX_VALUE);
			}
			this.parameters = invokerContext.getArguments();
			this.messageType = Constants.MESSAGE_TYPE_SERVICE;
		}
	}

	public int getId() {
		return id;
	}

	public String getVersion() {
		return null;
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

	public Object getObject() {
		return this;
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
		if (serviceName != null) {
			return serviceName;
		}
		ServiceId serviceId = PROVIDER_ID_MAP.get(this.id);
		if (serviceId != null) {
			serviceName = serviceId.getUrl();
		}
		return serviceName;
	}

	public String getMethodName() {
		if (methodName != null) {
			return methodName;
		}
		ServiceId serviceId = PROVIDER_ID_MAP.get(this.id);
		if (serviceId != null) {
			methodName = serviceId.getMethod();
		}
		return methodName;
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

	public int getMessageType() {
		return this.messageType;
	}

	@Override
	public Object getContext() {
		return this.context;
	}

	@Override
	public void setContext(Object context) {
		this.context = context;
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
				.append("callType", callType).append("timeout", timeout).append("url", this.getServiceName())
				.append("method", this.getMethodName()).append("created", createMillisTime);
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
		return globalValues;
	}

	public void setGlobalValues(Map<String, Serializable> globalValues) {
		this.globalValues = globalValues;
	}

	public Map<String, Serializable> getRequestValues() {
		return requestValues;
	}

	public void setRequestValues(Map<String, Serializable> requestValues) {
		this.requestValues = requestValues;
	}

}
