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
import org.jboss.netty.util.DebugUtil;

import com.dianping.pigeon.remoting.common.codec.SerializerFactory;
import com.dianping.pigeon.remoting.common.domain.InvocationResponse;
import com.dianping.pigeon.remoting.common.domain.InvocationSerializable;
import com.dianping.pigeon.remoting.common.util.TimelineManager;
import com.dianping.pigeon.remoting.common.util.TimelineManager.Phase;
import com.dianping.pigeon.remoting.netty.codec.AbstractDecoder;

public class InvokerDecoder extends AbstractDecoder {

	private static final String eventName = "PigeonCall.responseSize";

	@Override
	public Object doInitMsg(Object message, Channel channel, long receiveTime) {
		// TIMELINE_client_received: DebugUtil.getTimestamp()
		if (isNettyTimelineEnabled) {
			TimelineManager.time((InvocationSerializable) message, TimelineManager.getLocalIp(), Phase.ClientReceived,
					DebugUtil.getTimestamp());
		}
		// TIMELINE_client_decoded
		TimelineManager.time((InvocationSerializable) message, TimelineManager.getLocalIp(), Phase.ClientDecoded);
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

	@Override
	public String getEventName() {
		return eventName;
	}

}
