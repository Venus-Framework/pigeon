/**
 * Dianping.com Inc.
 * Copyright (c) 2003-${year} All Rights Reserved.
 */
package com.dianping.pigeon.remoting.netty.provider.codec;

import java.io.OutputStream;

import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.Channels;

import com.dianping.pigeon.remoting.common.codec.SerializerFactory;
import com.dianping.pigeon.remoting.common.domain.InvocationResponse;
import com.dianping.pigeon.remoting.netty.codec.AbstractEncoder;

public class ProviderEncoder extends AbstractEncoder {

	public ProviderEncoder() {
		super();
	}

	public Object encode(ChannelHandlerContext ctx, Channel channel, Object msg) throws Exception {
		return super.encode(ctx, channel, msg);
	}

	@Override
	public void doFailResponse(Channel channel, InvocationResponse response) {
		Channels.write(channel, response);
	}

	@Override
	public void serialize(byte serializerType, OutputStream os, Object obj) {
		SerializerFactory.getSerializer(serializerType).serializeResponse(os, obj);
	}

}
