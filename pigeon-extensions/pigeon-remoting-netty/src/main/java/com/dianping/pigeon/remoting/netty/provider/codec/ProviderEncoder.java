/**
 * Dianping.com Inc.
 * Copyright (c) 2003-${year} All Rights Reserved.
 */
package com.dianping.pigeon.remoting.netty.provider.codec;

import java.io.IOException;
import java.net.InetSocketAddress;

import org.jboss.netty.buffer.ChannelBufferOutputStream;
import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.Channels;

import com.dianping.pigeon.remoting.common.codec.SerializerFactory;
import com.dianping.pigeon.remoting.common.domain.InvocationResponse;
import com.dianping.pigeon.remoting.common.domain.InvocationSerializable;
import com.dianping.pigeon.remoting.common.util.TimelineManager;
import com.dianping.pigeon.remoting.common.util.TimelineManager.Phase;
import com.dianping.pigeon.remoting.netty.codec.AbstractEncoder;

public class ProviderEncoder extends AbstractEncoder {

	private static final String eventName = "PigeonService.responseSize";

	public ProviderEncoder() {
		super();
	}

	public Object encode(ChannelHandlerContext ctx, Channel channel, Object msg) throws Exception {
		Object encoded = super.encode(ctx, channel, msg);
		// TIMELINE_server_encoded
		String ip = ((InetSocketAddress) channel.getRemoteAddress()).getAddress().getHostAddress();
		TimelineManager.time((InvocationSerializable) msg, ip, Phase.ServerEncoded);
		return encoded;
	}

	@Override
	public void doFailResponse(Channel channel, InvocationResponse response) {
		Channels.write(channel, response);
	}

	@Override
	public void serialize(byte serializerType, ChannelBufferOutputStream os, Object obj, Channel channel)
			throws IOException {
		SerializerFactory.getSerializer(serializerType).serializeResponse(os, obj);
	}

	@Override
	public String getEventName() {
		return eventName;
	}

}
