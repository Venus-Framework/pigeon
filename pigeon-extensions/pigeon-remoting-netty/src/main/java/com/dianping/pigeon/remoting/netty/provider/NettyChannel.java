/**
 * Dianping.com Inc.
 * Copyright (c) 2003-2013 All Rights Reserved.
 */
package com.dianping.pigeon.remoting.netty.provider;

import java.net.InetSocketAddress;

import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelFuture;
import org.jboss.netty.channel.ChannelFutureListener;

import com.dianping.pigeon.remoting.common.domain.InvocationResponse;
import com.dianping.pigeon.remoting.common.util.TimelineManager;
import com.dianping.pigeon.remoting.common.util.TimelineManager.Phase;
import com.dianping.pigeon.remoting.provider.domain.ProviderChannel;

public class NettyChannel implements ProviderChannel {

	private Channel channel = null;

	private static final String protocol = "default";
	
	public NettyChannel(Channel channel) {
		this.channel = channel;
	}

	@Override
	public void write(final InvocationResponse response) {
		ChannelFuture future = this.channel.write(response);
		future.addListener(new ChannelFutureListener() {

			@Override
			public void operationComplete(ChannelFuture future)
					throws Exception {
				String ip = ((InetSocketAddress)channel.getRemoteAddress()).getAddress().getHostAddress();
				// TIMELINE_server_sent
				TimelineManager.time(response, ip, Phase.ServerSent);
			}
			
		});
	}

	@Override
	public String getRemoteAddress() {
		InetSocketAddress address = (InetSocketAddress) this.channel.getRemoteAddress();
		return address.getAddress().getHostAddress();
	}

	@Override
	public String getProtocol() {
		return protocol;
	}
}
