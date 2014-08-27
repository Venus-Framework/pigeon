/**
 * Dianping.com Inc.
 * Copyright (c) 2003-${year} All Rights Reserved.
 */
package com.dianping.pigeon.remoting.netty.invoker.codec;

import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.Channels;

import com.dianping.pigeon.config.ConfigManager;
import com.dianping.pigeon.extension.ExtensionLoader;
import com.dianping.pigeon.monitor.Monitor;
import com.dianping.pigeon.monitor.MonitorLogger;
import com.dianping.pigeon.monitor.MonitorTransaction;
import com.dianping.pigeon.remoting.common.codec.SerializerFactory;
import com.dianping.pigeon.remoting.common.domain.InvocationResponse;
import com.dianping.pigeon.remoting.common.domain.InvocationSerializable;
import com.dianping.pigeon.remoting.common.util.Constants;
import com.dianping.pigeon.remoting.common.util.TimelineManager;
import com.dianping.pigeon.remoting.common.util.TimelineManager.Phase;
import com.dianping.pigeon.remoting.netty.codec.AbstractEncoder;
import com.dianping.pigeon.remoting.netty.codec.NettyCodecUtils;

public class InvokerEncoder extends AbstractEncoder {

	private static MonitorLogger monitor = ExtensionLoader.getExtension(Monitor.class).getLogger();
	private static ConfigManager configManager = ExtensionLoader.getExtension(ConfigManager.class);
	private static final int requestSizeThreshold = configManager.getIntValue("pigeon.requestsize.threshold", 2 << 17);

	public InvokerEncoder() {
		super();
	}

	public Object encode(ChannelHandlerContext ctx, Channel channel, Object msg) throws Exception {
		NettyCodecUtils.setAttachment(ctx, Constants.ATTACHMENT_RETRY, msg);
		Object[] message = (Object[]) msg;
		Object encoded = super.encode(ctx, channel, message[0]);
		// TIMELINE_client_encoded
		TimelineManager.time((InvocationSerializable) message[0], TimelineManager.getLocalIp(), Phase.ClientEncoded);
		int size = ((ChannelBuffer) encoded).readableBytes();
		if (size > requestSizeThreshold) {
			MonitorTransaction transaction = monitor.getCurrentTransaction();
			if (transaction != null) {
				transaction.addData("RequestSize", size);
			}
		}
		return encoded;
	}

	@Override
	public void doFailResponse(Channel channel, InvocationResponse response) {
		List<InvocationResponse> respList = new ArrayList<InvocationResponse>();
		respList.add(response);
		Channels.fireMessageReceived(channel, respList);
	}

	@Override
	public void serialize(byte serializerType, OutputStream os, Object obj) {
		SerializerFactory.getSerializer(serializerType).serializeRequest(os, obj);
	}

}
