/**
 * 
 */
package com.dianping.pigeon.remoting.common.codec.protobuf;

import java.io.Serializable;
import java.util.Map;

import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;

import com.dianping.pigeon.remoting.common.codec.SerializerFactory;
import com.dianping.pigeon.remoting.common.domain.InvocationResponse;
import com.dianping.pigeon.remoting.common.util.Constants;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.protobuf.MessageLite;

/**
 * <p>
 * Title: PBResponse.java
 * </p>
 * <p>
 * Description: 描述
 * </p>
 * 
 * @author saber miao
 * @version 1.0
 * @created 2010-8-19 下午02:07:43
 */
public class ProtobufResponse implements InvocationResponse {

	/**
	 * 
	 */
	private static final long serialVersionUID = -1444633411274329165L;

	private ProtobufRpcProtos.Response response;

	private long seq;

	private MessageLite returnRes;

	private int messageType;

	@JsonProperty("exception")
	private String cause;

	@JsonIgnore
	private transient int size;

	@JsonIgnore
	private transient long createMillisTime;

	private Map<String, Serializable> responseValues = null;

	public ProtobufResponse(MessageLite response) {
		if (response instanceof ProtobufRpcProtos.Response) {
			this.response = (ProtobufRpcProtos.Response) response;
		} else {
			this.returnRes = response;
		}
	}

	public ProtobufResponse(String cause) {
		this.cause = cause;
	}

	public ProtobufResponse() {
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.dianping.dpsf.net.component.DPSFSerializable#getObject()
	 */
	public Object getObject() {
		if (this.response == null) {
			this.response = createResponse();
		}
		return this.response;
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
		if (this.seq > 0) {
			return this.seq;
		} else {
			return this.response.getSeq();
		}
	}

	/**
	 * @param messageType
	 *            the messageType to set
	 */
	public void setMessageType(int messageType) {
		this.messageType = messageType;
	}

	private ProtobufRpcProtos.Response createResponse() {
		ProtobufRpcProtos.Response res = null;
		ProtobufRpcProtos.Response.Builder builder = ProtobufRpcProtos.Response.newBuilder()
				.setMessageType(this.messageType).setSeq(this.seq);
		if (this.returnRes != null) {
			builder.setResponseMessage(this.returnRes.toByteString());
		} else if (this.cause != null) {
			builder.setErrorMessage(this.cause);
		}
		res = builder.build();

		return res;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.dianping.dpsf.net.component.DPSFSerializable#getSerializ()
	 */
	public byte getSerialize() {
		return SerializerFactory.SERIALIZE_PROTOBUF;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.dianping.dpsf.net.component.DPSFResponse#getMessageType()
	 */
	public int getMessageType() {
		if (this.messageType > 0) {
			return this.messageType;
		}
		if (this.response == null) {
			throw new IllegalArgumentException("response is null");
		}
		return this.response.getMessageType();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.dianping.dpsf.component.DPSFResponse#getCause()
	 */
	public String getCause() {
		if (this.response != null) {
			return this.response.getErrorMessage();
		}
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.dianping.dpsf.component.DPSFResponse#getReturn()
	 */
	public Object getReturn() {
		if (this.response != null) {
			if (this.response.getMessageType() == Constants.MESSAGE_TYPE_EXCEPTION) {
				return new com.google.protobuf.ServiceException(this.response.getErrorMessage());
			} else {
				return this.response.getResponseMessage();
			}
		}
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.dianping.dpsf.component.DPSFSerializable#getContext()
	 */
	@Override
	public Object getContext() {
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.dianping.dpsf.component.DPSFSerializable#setContext(java.lang.Object)
	 */
	@Override
	public void setContext(Object context) {

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.dianping.dpsf.component.DPSFResponse#setReturn(java.lang.Object)
	 */
	@Override
	public void setReturn(Object obj) {
		if (obj instanceof ProtobufRpcProtos.Response) {
			this.response = (ProtobufRpcProtos.Response) obj;
		} else if (obj instanceof MessageLite) {
			this.returnRes = (MessageLite) obj;
		}
		this.response = createResponse();
	}

	@Override
	public void setSerialize(byte serialize) {
	}

	@Override
	public String toString() {
		return new ToStringBuilder(this, ToStringStyle.SHORT_PREFIX_STYLE).append("seq", seq)
				.append("messageType", messageType).append("cause", cause).toString();
	}

	@Override
	public void setSize(int size) {
		this.size = size;
	}

	@Override
	public int getSize() {
		return size;
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
