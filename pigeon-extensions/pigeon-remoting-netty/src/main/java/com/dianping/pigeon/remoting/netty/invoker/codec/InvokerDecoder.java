/**
 * Dianping.com Inc.
 * Copyright (c) 2003-${year} All Rights Reserved.
 */
package com.dianping.pigeon.remoting.netty.invoker.codec;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.Channels;

import com.dianping.pigeon.remoting.common.codec.SerializerFactory;
import com.dianping.pigeon.remoting.common.domain.InvocationResponse;
import com.dianping.pigeon.remoting.netty.codec.AbstractDecoder;

public class InvokerDecoder extends AbstractDecoder {

	@Override
	public Object doInitMsg(Object message, Channel channel, long receiveTime) {
		return message;
	}

	@Override
	public void doFailResponse(Channel channel, InvocationResponse response) {
		List<InvocationResponse> respList = new ArrayList<InvocationResponse>();
		respList.add(response);
		Channels.fireMessageReceived(channel, respList);
	}

	@Override
	public Object deserialize(byte serializerType, InputStream is) {
		Object decoded = SerializerFactory.getSerializer(serializerType).deserializeResponse(is);
		return decoded;
	}

}
