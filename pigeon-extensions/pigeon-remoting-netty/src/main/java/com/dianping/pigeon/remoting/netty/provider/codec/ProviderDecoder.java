/**
 * Dianping.com Inc.
 * Copyright (c) 2003-2013 All Rights Reserved.
 */
package com.dianping.pigeon.remoting.netty.provider.codec;

import java.io.InputStream;

import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.Channels;

import com.dianping.pigeon.remoting.common.codec.SerializerFactory;
import com.dianping.pigeon.remoting.common.domain.InvocationRequest;
import com.dianping.pigeon.remoting.common.domain.InvocationResponse;
import com.dianping.pigeon.remoting.netty.codec.AbstractDecoder;

public class ProviderDecoder extends AbstractDecoder {

	@Override
	public Object doInitMsg(Object message) {
		if (message == null) {
			return null;
		}
		InvocationRequest request = (InvocationRequest) message;
		request.setPequestTime(System.currentTimeMillis());
		return request;
	}

	@Override
	public void doFailResponse(Channel channel, InvocationResponse response) {
		Channels.write(channel, response);
	}

	@Override
	public Object deserialize(byte serializerType, InputStream is) {
		return SerializerFactory.getSerializer(serializerType).deserializeRequest(is);
	}

}
