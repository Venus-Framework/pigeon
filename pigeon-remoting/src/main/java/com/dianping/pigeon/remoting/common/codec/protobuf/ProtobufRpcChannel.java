/**
 * 
 */
package com.dianping.pigeon.remoting.common.codec.protobuf;

import com.dianping.pigeon.remoting.invoker.config.InvokerConfig;
import com.google.protobuf.Descriptors.MethodDescriptor;
import com.google.protobuf.Message;
import com.google.protobuf.RpcCallback;
import com.google.protobuf.RpcChannel;
import com.google.protobuf.RpcController;

/**
 * <p>
 * Title: DPSFRpcChannel.java
 * </p>
 * <p>
 * Description: 描述
 * </p>
 * 
 * @author saber miao
 * @version 1.0
 * @created 2010-8-17 上午12:26:22
 */
public class ProtobufRpcChannel implements RpcChannel {

	private InvokerConfig invokerConfig;

	public ProtobufRpcChannel(InvokerConfig invokerConfig) {
		this.invokerConfig = invokerConfig;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.google.protobuf.RpcChannel#callMethod(com.google.protobuf.Descriptors
	 * .MethodDescriptor, com.google.protobuf.RpcController,
	 * com.google.protobuf.Message, com.google.protobuf.Message,
	 * com.google.protobuf.RpcCallback)
	 */
	public void callMethod(MethodDescriptor method, RpcController controller, Message request,
			Message responsePrototype, RpcCallback<Message> done) {

	}

}
