/**
 * 
 */
package com.dianping.pigeon.remoting.common.codec.protobuf;

import org.apache.log4j.Logger;

import com.dianping.pigeon.log.LoggerLoader;
import com.dianping.pigeon.remoting.common.component.invocation.InvocationContext;
import com.dianping.pigeon.remoting.common.component.invocation.InvocationResponse;
import com.dianping.pigeon.remoting.common.process.ServiceInvocationHandler;
import com.dianping.pigeon.remoting.common.util.Constants;
import com.dianping.pigeon.remoting.invoker.component.context.DefaultInvokerContext;
import com.dianping.pigeon.remoting.invoker.config.InvokerConfig;
import com.dianping.pigeon.remoting.invoker.process.InvocationHandlerFactory;
import com.google.protobuf.BlockingRpcChannel;
import com.google.protobuf.Descriptors.MethodDescriptor;
import com.google.protobuf.InvalidProtocolBufferException;
import com.google.protobuf.Message;
import com.google.protobuf.RpcController;
import com.google.protobuf.ServiceException;

/**
 * <p>
 * Title: DPSFBlockingRpcChannel.java
 * </p>
 * <p>
 * Description: 描述
 * </p>
 * 
 * @author saber miao
 * @version 1.0
 * @created 2010-8-17 上午12:26:54
 */
public class ProtobufBlockingRpcChannel implements BlockingRpcChannel {

	private static Logger logger = LoggerLoader.getLogger(ProtobufBlockingRpcChannel.class);

	private InvokerConfig invokerConfig;

	public ProtobufBlockingRpcChannel(InvokerConfig invokerConfig) {
		this.invokerConfig = invokerConfig;
	}

	@Override
	public Message callBlockingMethod(MethodDescriptor method, RpcController controller, Message request,
			Message responsePrototype) throws ServiceException {
		ServiceInvocationHandler handler = InvocationHandlerFactory.createInvokeHandler(invokerConfig);
		InvocationContext invocationContext = new DefaultInvokerContext(invokerConfig, method.getName(), null, null);
		ProtobufRequest protobufRequest = new ProtobufRequest(method, request, invokerConfig);
		invocationContext.setRequest(protobufRequest);
		InvocationResponse response;
		try {
			response = handler.handle(invocationContext);
		} catch (Throwable e) {
			throw new ServiceException("", e);
		}
		if (response != null) {
			ProtobufRpcProtos.Response resp = (ProtobufRpcProtos.Response) response.getObject();
			int messageType = resp.getMessageType();
			if (messageType == Constants.MESSAGE_TYPE_SERVICE) {
				Message msg;
				try {
					msg = responsePrototype.newBuilderForType().mergeFrom(resp.getResponseMessage()).build();
				} catch (InvalidProtocolBufferException e) {
					throw new ServiceException("", e);
				}
				return msg;
			}
			if (messageType == Constants.MESSAGE_TYPE_EXCEPTION
					|| messageType == Constants.MESSAGE_TYPE_SERVICE_EXCEPTION) {
				logger.error(resp.getErrorMessage());
				throw new ServiceException(resp.getErrorMessage());
			}
			throw new RuntimeException("unsupported response with type[" + messageType + "].");
		}
		return null;
	}

}
