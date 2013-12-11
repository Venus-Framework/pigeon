/**
 * Dianping.com Inc.
 * Copyright (c) 2003-2013 All Rights Reserved.
 */
package com.dianping.pigeon.remoting.netty.provider;

import java.net.InetSocketAddress;

import org.jboss.netty.channel.Channel;

import com.dianping.pigeon.remoting.common.component.invocation.InvocationResponse;
import com.dianping.pigeon.remoting.provider.component.ProviderChannel;

/**
 * 
 * 
 * @author jianhuihuang
 * @version $Id: NettyChannel.java, v 0.1 2013-6-20 下午5:47:22 jianhuihuang Exp $
 */
public class NettyChannel implements ProviderChannel {

	private Channel channel = null;

	public NettyChannel(Channel channel) {
		this.channel = channel;
	}

	/**
	 * @return the channel
	 */
	@SuppressWarnings("unchecked")
	public <C> C getChannel(C c) {
		return (C) channel;
	}

	@Override
	public void write(InvocationResponse response) {
		this.channel.write(response);
	}

	@Override
	public InetSocketAddress getRemoteAddress() {
		return (InetSocketAddress) this.channel.getRemoteAddress();
	}

}
