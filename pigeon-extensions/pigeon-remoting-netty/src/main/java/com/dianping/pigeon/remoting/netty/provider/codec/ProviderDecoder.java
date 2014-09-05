/**
 * Dianping.com Inc.
 * Copyright (c) 2003-2013 All Rights Reserved.
 */
package com.dianping.pigeon.remoting.netty.provider.codec;

import java.io.InputStream;
import java.net.InetSocketAddress;

import org.jboss.netty.channel.Channel;
import org.jboss.netty.util.DebugUtil;

import com.dianping.pigeon.remoting.common.codec.SerializerFactory;
import com.dianping.pigeon.remoting.common.domain.InvocationRequest;
import com.dianping.pigeon.remoting.common.domain.InvocationResponse;
import com.dianping.pigeon.remoting.common.util.TimelineManager;
import com.dianping.pigeon.remoting.common.util.TimelineManager.Phase;
import com.dianping.pigeon.remoting.netty.codec.AbstractDecoder;
import com.dianping.pigeon.remoting.netty.provider.NettyChannel;

public class ProviderDecoder extends AbstractDecoder {

	private static final String eventName = "PigeonService.requestSize";

	@Override
	public Object doInitMsg(Object message, Channel channel, long receiveTime) {
		if (message == null) {
			return null;
		}
		InvocationRequest request = (InvocationRequest) message;
		// TIMELINE_server_received: DebugUtil.getTimestamp()
		String remoteIp = ((InetSocketAddress) channel.getRemoteAddress()).getAddress().getHostAddress();
		if (isNettyTimelineEnabled) {
			TimelineManager.time(request, remoteIp, Phase.ServerReceived, DebugUtil.getTimestamp());
		}
		// TIMELINE_server_decoded
		TimelineManager.time(request, remoteIp, Phase.ServerDecoded);
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

	@Override
	public String getEventName() {
		return eventName;
	}

}
