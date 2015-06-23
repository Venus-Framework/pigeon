/**
 * Dianping.com Inc.
 * Copyright (c) 2003-${year} All Rights Reserved.
 */
package com.dianping.pigeon.remoting.netty.provider;

import java.util.List;

import com.dianping.pigeon.log.LoggerLoader;
import org.apache.logging.log4j.Logger;
import org.jboss.netty.channel.ChannelEvent;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.ChannelState;
import org.jboss.netty.channel.ChannelStateEvent;
import org.jboss.netty.channel.ExceptionEvent;
import org.jboss.netty.channel.MessageEvent;
import org.jboss.netty.channel.SimpleChannelUpstreamHandler;

import com.dianping.pigeon.remoting.common.domain.InvocationRequest;
import com.dianping.pigeon.remoting.common.util.Constants;
import com.dianping.pigeon.remoting.provider.domain.DefaultProviderContext;
import com.dianping.pigeon.remoting.provider.domain.ProviderContext;
import com.dianping.pigeon.remoting.provider.util.ProviderUtils;

public class NettyServerHandler extends SimpleChannelUpstreamHandler {

	private static final Logger log = LoggerLoader.getLogger(NettyServerHandler.class);

	private NettyServer server;

	public NettyServerHandler(NettyServer server) {
		this.server = server;
	}

	@Override
	public void handleUpstream(ChannelHandlerContext ctx, ChannelEvent e) throws Exception {
		if (log.isDebugEnabled()) {
			if (e instanceof ChannelStateEvent && ((ChannelStateEvent) e).getState() != ChannelState.INTEREST_OPS) {
				log.debug(e.toString());
			}
		}
		super.handleUpstream(ctx, e);
	}

	/**
	 * 服务器端接受到消息
	 * 
	 * @see org.jboss.netty.channel.SimpleChannelUpstreamHandler#messageReceived(org.jboss.netty.channel.ChannelHandlerContext,
	 *      org.jboss.netty.channel.MessageEvent)
	 */
	@SuppressWarnings("unchecked")
	@Override
	public void messageReceived(ChannelHandlerContext ctx, MessageEvent message) {
		List<InvocationRequest> messages = (List<InvocationRequest>) (message.getMessage());
		// System.out.println("messages:" + messages.size());
		// NioSocketChannelConfig config = ((NioSocketChannelConfig)
		// ctx.getChannel().getConfig());
		for (InvocationRequest request : messages) {
			ProviderContext invocationContext = new DefaultProviderContext(request, new NettyChannel(ctx.getChannel()));
			try {
				this.server.processRequest(request, invocationContext);
			} catch (Throwable e) {
				String msg = "process request failed:" + request;
				// 心跳消息只返回正常的, 异常不返回
				if (request.getCallType() == Constants.CALLTYPE_REPLY
						&& request.getMessageType() != Constants.MESSAGE_TYPE_HEART) {
					ctx.getChannel().write(ProviderUtils.createFailResponse(request, e));
				}
				log.error(msg, e);
			}
		}
	}

	@Override
	public void channelConnected(ChannelHandlerContext ctx, ChannelStateEvent e) {
	}

	@Override
	public void channelOpen(ChannelHandlerContext ctx, ChannelStateEvent e) {
		this.server.getChannelGroup().add(e.getChannel());
	}

	@Override
	public void exceptionCaught(ChannelHandlerContext ctx, ExceptionEvent e) {
		log.error(e.getCause().getMessage(), e.getCause());
	}

}
