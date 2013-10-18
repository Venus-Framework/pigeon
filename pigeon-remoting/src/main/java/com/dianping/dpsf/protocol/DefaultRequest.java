/**
 * Dianping.com Inc.
 * Copyright (c) 2003-2013 All Rights Reserved.
 */
package com.dianping.dpsf.protocol;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang.builder.ReflectionToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;

import com.dianping.dpsf.component.DPSFRequest;

/**
 * 不能修改packagename，修改属性需要注意，确保和之前的dpsf兼容。
 * 
 * @author jianhuihuang
 * @version $Id: DefaultRequest.java, v 0.1 2013-7-5 上午8:25:42 jianhuihuang Exp
 *          $
 */
public class DefaultRequest implements DPSFRequest {

	/**
	 * 不能随意修改！
	 */
	private static final long serialVersionUID = 652592942114047764L;

	private byte serialize;

	private long seq;

	private int callType;

	private int timeout;

	private long createMillisTime;

	private String serviceName;

	private String methodName;

	private Object[] parameters;

	private int messageType;

	private Object context;

	private transient Class<?>[] parameterClasses;

	private transient Map<String, Object> attachments = new HashMap<String, Object>();

	public DefaultRequest(String serviceName, String methodName, Object[] parameters, byte serialize, int messageType,
			int timeout, Class<?>[] parameterClasses) {
		this.serviceName = serviceName;
		this.methodName = methodName;
		this.parameters = parameters;
		this.serialize = serialize;
		this.messageType = messageType;
		this.timeout = timeout;
		this.parameterClasses = parameterClasses;
	}

	/**
	 * @return the parameterClasses
	 */
	public Class<?>[] getParameterClasses() {
		return parameterClasses;
	}

	/**
	 * @param parameterClasses
	 *            the parameterClasses to set
	 */
	public void setParameterClasses(Class<?>[] parameterClasses) {
		this.parameterClasses = parameterClasses;
	}

	public byte getSerializ() {
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
	public void setAttachment(String name, Object attachment) {
		attachments.put(name, attachment);
	}

	@Override
	public Object getAttachment(String name) {
		return attachments.get(name);
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
		return ReflectionToStringBuilder.toString(this, ToStringStyle.SHORT_PREFIX_STYLE);
	}
}
