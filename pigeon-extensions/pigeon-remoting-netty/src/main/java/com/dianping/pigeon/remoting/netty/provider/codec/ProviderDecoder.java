/**
 * Dianping.com Inc.
 * Copyright (c) 2003-2013 All Rights Reserved.
 */
package com.dianping.pigeon.remoting.netty.provider.codec;

import java.io.InputStream;

import org.jboss.netty.channel.Channel;

import com.dianping.pigeon.remoting.common.codec.SerializerFactory;
import com.dianping.pigeon.remoting.common.domain.InvocationRequest;
import com.dianping.pigeon.remoting.common.domain.InvocationResponse;
import com.dianping.pigeon.remoting.netty.codec.AbstractDecoder;
import com.dianping.pigeon.remoting.netty.provider.NettyChannel;

public class ProviderDecoder extends AbstractDecoder {

	@Override
	public Object doInitMsg(Object message, Channel channel, long receiveTime) {
		if (message == null) {
			return null;
		}
		InvocationRequest request = (InvocationRequest) message;
		request.setCreateMillisTime(receiveTime);
		return request;
	}

	@Override
	public void doFailResponse(Channel channel, InvocationResponse response) {
		NettyChannel nettyChannel = new NettyChannel(channel);
		nettyChannel.write(response);
	}

	@Override
	public Object deserialize(byte serializerType, InputStream is) {
		Object decoded = SerializerFactory.getSerializer(serializerType).deserializeRequest(is);
		return decoded;
	}

}
