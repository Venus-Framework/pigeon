/**
 * Dianping.com Inc.
 * Copyright (c) 2003-2013 All Rights Reserved.
 */
package com.dianping.dpsf.protocol;

import java.io.Serializable;
import java.util.Map;

import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;

import com.dianping.pigeon.remoting.common.domain.InvocationResponse;
import com.dianping.pigeon.remoting.common.util.Constants;
import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;

@JsonIdentityInfo(generator = ObjectIdGenerators.PropertyGenerator.class, property = "seq", scope = DefaultResponse.class)
public class DefaultResponse implements InvocationResponse {

	/**
	 * 不能随意修改！
	 */
	private static final long serialVersionUID = 4200559704846455821L;

	private transient byte serialize;

	@JsonProperty("seq")
	private long seq;

	private int messageType;

	@JsonProperty("exception")
	private String cause;

	@JsonProperty("response")
	@JsonTypeInfo(use = JsonTypeInfo.Id.CLASS, include = JsonTypeInfo.As.PROPERTY, property = "@class")
	private Object returnVal;

	@JsonTypeInfo(use = JsonTypeInfo.Id.CLASS, include = JsonTypeInfo.As.PROPERTY, property = "@class")
	private Object context;

	@JsonIgnore
	private transient int size;

	@JsonIgnore
	private transient long createMillisTime;

	private Map<String, Serializable> responseValues = null;

	public DefaultResponse() {
	}

	public DefaultResponse(int messageType, byte serialize) {
		this.messageType = messageType;
		this.serialize = serialize;
	}

	public DefaultResponse(byte serialize, long seq, int messageType, Object returnVal) {
		this.serialize = serialize;
		this.seq = seq;
		this.messageType = messageType;
		this.returnVal = returnVal;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.dianping.dpsf.net.component.DPSFSerializable#getSerializ()
	 */
	public byte getSerialize() {
		return this.serialize;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.dianping.dpsf.net.component.DPSFSerializable#setSequence(long)
	 */
	public void setSequence(long seq) {
		this.seq = seq;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.dianping.dpsf.net.component.DPSFSerializable#getSequence()
	 */
	public long getSequence() {
		return this.seq;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.dianping.dpsf.net.component.DPSFSerializable#getObject()
	 */
	public Object getObject() {
		return this;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.dianping.dpsf.net.component.DPSFResponse#setMessageType(int)
	 */
	public void setMessageType(int messageType) {
		this.messageType = messageType;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.dianping.dpsf.net.component.DPSFResponse#getMessageType()
	 */
	public int getMessageType() {
		return this.messageType;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.dianping.dpsf.component.DPSFResponse#getCause()
	 */
	public String getCause() {
		return this.cause;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.dianping.dpsf.component.DPSFResponse#getReturn()
	 */
	public Object getReturn() {
		return this.returnVal;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.dianping.dpsf.component.DPSFSerializable#getContext()
	 */
	@Override
	public Object getContext() {
		return this.context;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.dianping.dpsf.component.DPSFSerializable#setContext(java.lang.Object)
	 */
	@Override
	public void setContext(Object context) {
		this.context = context;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.dianping.dpsf.component.DPSFResponse#setReturn(java.lang.Object)
	 */
	@Override
	public void setReturn(Object obj) {
		this.returnVal = obj;
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
		return responseValues;
	}

	public void setResponseValues(Map<String, Serializable> responseValues) {
		this.responseValues = responseValues;
	}

	public long getCreateMillisTime() {
		return createMillisTime;
	}

	public void setCreateMillisTime(long createMillisTime) {
		this.createMillisTime = createMillisTime;
	}

}
