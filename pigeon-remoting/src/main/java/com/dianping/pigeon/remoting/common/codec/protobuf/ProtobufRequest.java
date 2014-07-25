/**
 * 
 */
package com.dianping.pigeon.remoting.common.codec.protobuf;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.commons.lang.ClassUtils;
import org.apache.commons.lang.SerializationException;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;

import com.dianping.pigeon.remoting.common.codec.SerializerFactory;
import com.dianping.pigeon.remoting.common.domain.InvocationRequest;
import com.dianping.pigeon.remoting.common.util.Constants;
import com.dianping.pigeon.remoting.invoker.config.InvokerConfig;
import com.dianping.pigeon.remoting.invoker.domain.InvokerContext;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.google.protobuf.Descriptors.MethodDescriptor;
import com.google.protobuf.GeneratedMessage;
import com.google.protobuf.InvalidProtocolBufferException;
import com.google.protobuf.Message;
import com.google.protobuf.MessageLite;
import com.google.protobuf.RpcCallback;
import com.google.protobuf.RpcController;

/**
 * <p>
 * Title: PBRequest.java
 * </p>
 * <p>
 * Description: 描述
 * </p>
 * 
 * @author saber miao
 * @version 1.0
 * @created 2010-8-19 下午01:58:08
 */
public class ProtobufRequest implements InvocationRequest {

	private static final long serialVersionUID = 8840220134866666929L;

	private ProtobufRpcProtos.Request request;

	private Message parameterReq;

	private String methodName;

	private String serviceName;

	private int callType = Constants.CALLTYPE_REPLY;

	private int messageType = Constants.MESSAGE_TYPE_SERVICE;

	private int timeout = 0;

	private long sequence;

	private transient long createMillisTime = 0;

	private String parameterClass;

	private String version;

	private transient String loadbalance;

	public String getLoadbalance() {
		return loadbalance;
	}

	public void setLoadbalance(String loadbalance) {
		this.loadbalance = loadbalance;
	}

	public void setMethodName(String methodName) {
		this.methodName = methodName;
	}

	public void setServiceName(String serviceName) {
		this.serviceName = serviceName;
	}

	public void setVersion(String version) {
		this.version = version;
	}

	private transient Map<String, Object> attachments = new HashMap<String, Object>();

	@JsonTypeInfo(use = JsonTypeInfo.Id.CLASS, include = JsonTypeInfo.As.PROPERTY, property = "@class")
	private Object context;

	private final static RpcController controller = new RpcController() {

		public void reset() {
			throw new UnsupportedOperationException();
		}

		public boolean failed() {
			throw new UnsupportedOperationException();
		}

		public String errorText() {
			throw new UnsupportedOperationException();
		}

		public void startCancel() {
			throw new UnsupportedOperationException();
		}

		public void setFailed(String reason) {
			throw new UnsupportedOperationException();
		}

		public boolean isCanceled() {
			throw new UnsupportedOperationException();
		}

		public void notifyOnCancel(RpcCallback<Object> callback) {
			throw new UnsupportedOperationException();
		}

	};

	private static Map<String, GeneratedMessage> initParameters = new ConcurrentHashMap<String, GeneratedMessage>();

	public ProtobufRequest() {

	}

	public ProtobufRequest(InvokerContext invokerContext) {
		if (invokerContext != null) {
			InvokerConfig<?> invokerConfig = invokerContext.getInvokerConfig();
			if (invokerConfig != null) {
				setInvokerConfig(invokerConfig);
			}
			this.methodName = invokerContext.getMethodName();
			// this.parameterClasses = invokerContext.getParameterTypes();
			// this.parameters = invokerContext.getArguments();
			this.parameterReq = ProtobufHelper.toMessage(invokerConfig.getServiceInterface().getName(),
					invokerContext.getMethodName(), invokerContext.getParameterTypes(), invokerContext.getArguments());
		}
	}

	private void setInvokerConfig(InvokerConfig<?> invokerConfig) {
		this.serviceName = invokerConfig.getUrl();
		this.timeout = invokerConfig.getTimeout();
		this.messageType = Constants.MESSAGE_TYPE_SERVICE;
		this.setVersion(invokerConfig.getVersion());
		this.setAttachment(Constants.REQ_ATTACH_WRITE_BUFF_LIMIT, invokerConfig.isWriteBufferLimit());
		if (Constants.CALL_ONEWAY.equalsIgnoreCase(invokerConfig.getCallType())) {
			this.setCallType(Constants.CALLTYPE_NOREPLY);
		} else {
			this.setCallType(Constants.CALLTYPE_REPLY);
		}
		this.setLoadbalance(invokerConfig.getLoadbalance());
	}

	// Server
	public ProtobufRequest(MessageLite request) {
		this.request = (ProtobufRpcProtos.Request) request;
		this.callType = this.request.getCallType();
		this.messageType = this.request.getMessageType();
		this.sequence = this.request.getSeq();
		this.serviceName = this.request.getUrl();
		this.timeout = this.request.getTimeout();
		this.version = this.request.getVersion();
		this.methodName = this.request.getMethodName();
	}

	// Client
	public ProtobufRequest(MethodDescriptor method, Message parameterReq, InvokerConfig<?> invokerConfig) {
		setInvokerConfig(invokerConfig);
		this.methodName = method.getName();
		this.parameterReq = parameterReq;
		this.parameterClass = parameterReq.getClass().getName();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.dianping.dpsf.net.component.DPSFRequest#getSerializ()
	 */
	public byte getSerialize() {
		return SerializerFactory.SERIALIZE_PROTOBUF;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.dianping.dpsf.net.component.DPSFSerializable#getObject()
	 */
	public Object getObject() {
		if (this.request == null) {
			this.request = createRequest();
		}
		return this.request;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.dianping.dpsf.net.component.DPSFRequest#setSequence(long)
	 */
	public void setSequence(long sequence) {
		this.sequence = sequence;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.dianping.dpsf.net.component.DPSFSerializable#getSequence()
	 */
	public long getSequence() {
		return sequence;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.dianping.dpsf.net.component.DPSFRequest#setCallType(int)
	 */
	public void setCallType(int callType) {
		this.callType = callType;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.dianping.dpsf.net.component.DPSFRequest#getCallType()
	 */
	public int getCallType() {
		return this.callType;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.dianping.dpsf.net.component.DPSFRequest#getTimeout()
	 */
	public int getTimeout() {
		return this.timeout;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.dianping.dpsf.net.component.DPSFRequest#getCreateMillisTime()
	 */
	public long getCreateMillisTime() {
		return createMillisTime;
	}

	/**
	 * @param messageType
	 *            the messageType to set
	 */
	public void setMessageType(int messageType) {
		this.messageType = messageType;
	}

	private ProtobufRpcProtos.Request createRequest() {
		ProtobufRpcProtos.Request request = ProtobufRpcProtos.Request.newBuilder().setMessageType(this.messageType)
				.setCallType(this.callType).setSeq(this.sequence).setUrl(this.serviceName)
				.setTimeout(this.getTimeout())
				// .setVersion(this.version)
				.setParameterClass(this.parameterClass).setMethodName(this.getMethodName())
				.setParameters(parameterReq.toByteString()).build();

		return request;
	}

	public int hashCode() {
		return new Long(this.sequence).hashCode();
	}

	public String getServiceName() {
		return this.serviceName;
	}

	public String getMethodName() {
		return this.methodName;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.dianping.dpsf.net.component.DPSFRequest#getParameterType()
	 */
	public String[] getParamClassName() {
		if (this.parameterClass != null) {
			return new String[] { RpcController.class.getName(), this.parameterClass };
		}
		if (this.request == null) {
			throw new IllegalArgumentException("request is null");
		}
		return new String[] { RpcController.class.getName(), this.request.getParameterClass() };
	}

	private GeneratedMessage getParamMessage() {
		if (this.parameterClass == null) {
			if (this.request == null) {
				throw new IllegalArgumentException("request is null");
			}
			this.parameterClass = this.request.getParameterClass();
		}
		GeneratedMessage gml = ProtobufRequest.initParameters.get(this.parameterClass);
		if (gml == null) {
			Class<?> paramClass = null;
			try {
				paramClass = ClassUtils.getClass(this.parameterClass);
			} catch (ClassNotFoundException e) {
				throw new SerializationException(e);
			}
			Field f = null;
			try {
				f = paramClass.getDeclaredField("defaultInstance");
			} catch (SecurityException e) {
				throw new SerializationException(e);
			} catch (NoSuchFieldException e) {
				throw new SerializationException(e);
			}
			f.setAccessible(true);
			try {
				gml = (GeneratedMessage) f.get(null);
			} catch (IllegalArgumentException e) {
				throw new SerializationException(e);
			} catch (IllegalAccessException e) {
				throw new SerializationException(e);
			}
			ProtobufRequest.initParameters.put(this.parameterClass, gml);
		}
		return gml;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.dianping.dpsf.net.component.DPSFRequest#getParameters()
	 */
	public Object[] getParameters() {
		if (this.parameterReq != null) {
			return new Object[] { this.parameterReq };
		}
		if (this.request == null) {
			throw new IllegalArgumentException("request is null");
		}
		GeneratedMessage gml = getParamMessage();
		MessageLite ml = null;
		try {
			ml = gml.newBuilderForType().mergeFrom(this.request.getParameters()).build();
		} catch (InvalidProtocolBufferException e) {
			throw new SerializationException(e);
		}
		return new Object[] { controller, ml };
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.dianping.dpsf.net.component.DPSFRequest#getMessageType()
	 */
	public int getMessageType() {
		return this.messageType;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.dianping.dpsf.component.DPSFSerializable#getContext()
	 */
	@Override
	public Object getContext() {
		return context;
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

	@Override
	public void setAttachment(String name, Object attachment) {
		this.attachments.put(name, attachment);
	}

	@Override
	public Object getAttachment(String name) {
		return this.attachments.get(name);
	}

	@Override
	public void setTimeout(int timeout) {
		this.timeout = timeout;
	}

	@Override
	public String getVersion() {
		return version;
	}

	@Override
	public void setCreateMillisTime(long createMillisTime) {
		this.createMillisTime = createMillisTime;
	}

	@Override
	public void setSerialize(byte serialize) {
	}

	@Override
	public String toString() {
		return new ToStringBuilder(this, ToStringStyle.SHORT_PREFIX_STYLE).append("seq", sequence)
				.append("messageType", messageType).append("callType", callType).append("timeout", timeout)
				.append("url", serviceName).append("methodName", methodName).append("parameterClass", parameterClass)
				.append("version", version).toString();
	}

}
