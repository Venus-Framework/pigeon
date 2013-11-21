/**
 * Dianping.com Inc.
 * Copyright (c) 2003-${year} All Rights Reserved.
 */
package com.dianping.pigeon.remoting.netty.provider;

import static org.jboss.netty.channel.Channels.pipeline;

import org.jboss.netty.channel.ChannelPipeline;
import org.jboss.netty.channel.ChannelPipelineFactory;

import com.dianping.pigeon.remoting.netty.provider.codec.ProviderDecoder;
import com.dianping.pigeon.remoting.netty.provider.codec.ProviderEncoder;

public class NettyServerPipelineFactory implements ChannelPipelineFactory {

	private NettyServer server;

	public NettyServerPipelineFactory(NettyServer server) {
		this.server = server;
	}

	public ChannelPipeline getPipeline() {
		ChannelPipeline pipeline = pipeline();
		pipeline.addLast("decoder", new ProviderDecoder());
		pipeline.addLast("encoder", new ProviderEncoder());
		pipeline.addLast("handler", new NettyServerHandler(server));
		return pipeline;
	}

}
