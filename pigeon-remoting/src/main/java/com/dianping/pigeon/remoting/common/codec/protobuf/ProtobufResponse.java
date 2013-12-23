/**
 * 
 */
package com.dianping.pigeon.remoting.common.codec.protobuf;

import org.apache.commons.lang.builder.ReflectionToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;

import com.dianping.pigeon.remoting.common.codec.SerializerFactory;
import com.dianping.pigeon.remoting.common.domain.InvocationResponse;
import com.dianping.pigeon.remoting.common.util.Constants;
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

	private String cause;
	
	private long invokerRequestTime;

	private long invokerResponseTime;

	private long providerRequestTime;

	private long providerResponseTime;

	public long getInvokerRequestTime() {
		return invokerRequestTime;
	}

	public void setInvokerRequestTime(long invokerRequestTime) {
		this.invokerRequestTime = invokerRequestTime;
	}

	public long getInvokerResponseTime() {
		return invokerResponseTime;
	}

	public void setInvokerResponseTime(long invokerResponseTime) {
		this.invokerResponseTime = invokerResponseTime;
	}

	public long getProviderRequestTime() {
		return providerRequestTime;
	}

	public void setProviderRequestTime(long providerRequestTime) {
		this.providerRequestTime = providerRequestTime;
	}

	public long getProviderResponseTime() {
		return providerResponseTime;
	}

	public void setProviderResponseTime(long providerResponseTime) {
		this.providerResponseTime = providerResponseTime;
	}

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
		return ReflectionToStringBuilder.toString(this, ToStringStyle.SHORT_PREFIX_STYLE);
	}
}
