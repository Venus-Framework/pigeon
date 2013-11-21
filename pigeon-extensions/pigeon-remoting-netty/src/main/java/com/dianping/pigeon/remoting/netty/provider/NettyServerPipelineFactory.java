/**
 * Dianping.com Inc.
 * Copyright (c) 2003-${year} All Rights Reserved.
 */
package com.dianping.pigeon.remoting.netty.provider;

import static org.jboss.netty.channel.Channels.pipeline;

import org.jboss.netty.channel.ChannelPipeline;
import org.jboss.netty.channel.ChannelPipelineFactory;
import org.jboss.netty.channel.group.ChannelGroup;

import com.dianping.pigeon.remoting.netty.provider.codec.ProviderDecoder;
import com.dianping.pigeon.remoting.netty.provider.codec.ProviderEncoder;
import com.dianping.pigeon.remoting.netty.provider.process.RequestProcessor;
import com.dianping.pigeon.remoting.netty.provider.process.ServerChannelHandler;

/**
 * 
 * 
 * @author jianhuihuang
 * @version $Id: DPServerChannelPipelineFactory.java, v 0.1 2013-6-20 下午5:47:03
 *          jianhuihuang Exp $
 */
public class ServerChannelPipelineFactory implements ChannelPipelineFactory {

	private RequestProcessor processor;
	private ChannelGroup channelGroup;

	public ServerChannelPipelineFactory(ChannelGroup channelGroup, RequestProcessor processor) {
		this.channelGroup = channelGroup;
		this.processor = processor;
	}

	public ChannelPipeline getPipeline() {

		ChannelPipeline pipeline = pipeline();
		pipeline.addLast("decoder", new ProviderDecoder());
		pipeline.addLast("encoder", new ProviderEncoder());
		pipeline.addLast("handler", new ServerChannelHandler(channelGroup, processor));
		return pipeline;
	}

}
