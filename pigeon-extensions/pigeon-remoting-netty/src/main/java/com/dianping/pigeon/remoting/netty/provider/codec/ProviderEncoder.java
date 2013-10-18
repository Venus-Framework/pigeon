/**
 * Dianping.com Inc.
 * Copyright (c) 2003-${year} All Rights Reserved.
 */
package com.dianping.pigeon.remoting.netty.provider.codec;

import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.Channels;

import com.dianping.dpsf.component.DPSFResponse;
import com.dianping.pigeon.remoting.netty.codec.AbstractEncoder;

public class ProviderEncoder extends AbstractEncoder {

	public ProviderEncoder() {
		super();
	}

	public Object encode(ChannelHandlerContext ctx, Channel channel, Object msg) throws Exception {
		return super.encode(ctx, channel, msg);
	}

	@Override
	public void doFailResponse(Channel channel, DPSFResponse response) {
		Channels.write(channel, response);
	}

}
