/**
 * Dianping.com Inc.
 * Copyright (c) 2003-2013 All Rights Reserved.
 */
package com.dianping.pigeon.remoting.netty.invoker;

import static org.jboss.netty.channel.Channels.pipeline;

import org.jboss.netty.channel.ChannelHandler;
import org.jboss.netty.channel.ChannelPipeline;
import org.jboss.netty.channel.ChannelPipelineFactory;
import org.jboss.netty.util.HashedWheelTimer;
import org.jboss.netty.util.Timer;

import com.dianping.pigeon.remoting.invoker.Client;
import com.dianping.pigeon.remoting.netty.invoker.codec.InvokerDecoder;
import com.dianping.pigeon.remoting.netty.invoker.codec.InvokerEncoder;
import com.dianping.pigeon.remoting.netty.invoker.process.ClientChannelHandler;

/**
 * 
 * 
 * @author jianhuihuang
 * @version $Id: DPClientChannelPipelineFactory.java, v 0.1 2013-7-19
 *          下午3:13:16 jianhuihuang Exp $
 */
public class ClientChannelPipelineFactory implements ChannelPipelineFactory {

	private Client client;
	private ChannelHandler decoder;
	private ChannelHandler encoder;
	private ChannelHandler handler;
	Timer timer = new HashedWheelTimer();

	public ClientChannelPipelineFactory(Client client) {
		this.client = client;
		this.decoder = new InvokerDecoder();
		this.encoder = new InvokerEncoder();
		this.handler = new ClientChannelHandler(this.client);
	}

	public ChannelPipeline getPipeline() throws Exception {
		ChannelPipeline pipeline = pipeline();
		pipeline.addLast("decoder", decoder);
		pipeline.addLast("encoder", encoder);
		pipeline.addLast("handler", handler);
		//pipeline.addLast("timeout", new ReadTimeoutHandler(timer, 3));  
		//pipeline.addLast("timeout", new IdleStateHandler(timer, 10, 10, 0));
		return pipeline;
	}

}
