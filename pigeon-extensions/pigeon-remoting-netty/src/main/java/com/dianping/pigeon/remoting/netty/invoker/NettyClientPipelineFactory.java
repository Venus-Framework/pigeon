/**
 * Dianping.com Inc.
 * Copyright (c) 2003-2013 All Rights Reserved.
 */
package com.dianping.pigeon.remoting.netty.invoker;

import static org.jboss.netty.channel.Channels.pipeline;

import org.jboss.netty.channel.ChannelHandler;
import org.jboss.netty.channel.ChannelPipeline;
import org.jboss.netty.channel.ChannelPipelineFactory;

import com.dianping.pigeon.remoting.netty.invoker.codec.InvokerDecoder;
import com.dianping.pigeon.remoting.netty.invoker.codec.InvokerEncoder;

public class NettyClientPipelineFactory implements ChannelPipelineFactory {

	private NettyClient client;
	private ChannelHandler decoder;
	private ChannelHandler encoder;
	private ChannelHandler handler;

	public NettyClientPipelineFactory(NettyClient client) {
		this.client = client;
		this.decoder = new InvokerDecoder();
		this.encoder = new InvokerEncoder();
		this.handler = new NettyClientHandler(this.client);
	}

	public ChannelPipeline getPipeline() throws Exception {
		ChannelPipeline pipeline = pipeline();
		pipeline.addLast("decoder", decoder);
		pipeline.addLast("encoder", encoder);
		pipeline.addLast("handler", handler);
		return pipeline;
	}

}
