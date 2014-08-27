/**
 * Dianping.com Inc.
 * Copyright (c) 2003-${year} All Rights Reserved.
 */
package com.dianping.pigeon.remoting.netty.provider.codec;

import java.io.OutputStream;
import java.net.InetSocketAddress;

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
import com.dianping.pigeon.remoting.common.util.TimelineManager;
import com.dianping.pigeon.remoting.common.util.TimelineManager.Phase;
import com.dianping.pigeon.remoting.netty.codec.AbstractEncoder;

public class ProviderEncoder extends AbstractEncoder {

	private static MonitorLogger monitor = ExtensionLoader.getExtension(Monitor.class).getLogger();
	private static ConfigManager configManager = ExtensionLoader.getExtension(ConfigManager.class);
	private static final int responseSizeThreshold = configManager
			.getIntValue("pigeon.responsesize.threshold", 2 << 17);

	public ProviderEncoder() {
		super();
	}

	public Object encode(ChannelHandlerContext ctx, Channel channel, Object msg) throws Exception {
		Object encoded = super.encode(ctx, channel, msg);
		// TIMELINE_server_encoded
		String ip = ((InetSocketAddress) channel.getRemoteAddress()).getAddress().getHostAddress();
		TimelineManager.time((InvocationSerializable) msg, ip, Phase.ServerEncoded);
		int size = ((ChannelBuffer) encoded).readableBytes();
		if (size > responseSizeThreshold) {
			MonitorTransaction transaction = monitor.getCurrentTransaction();
			if (transaction != null) {
				transaction.addData("ResponseSize", size);
			}
		}
		return encoded;
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
