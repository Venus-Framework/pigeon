/**
 * Dianping.com Inc.
 * Copyright (c) 2003-${year} All Rights Reserved.
 */
package com.dianping.pigeon.remoting.netty.provider;

import java.util.List;

import org.apache.log4j.Logger;
import org.jboss.netty.channel.ChannelEvent;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.ChannelState;
import org.jboss.netty.channel.ChannelStateEvent;
import org.jboss.netty.channel.ExceptionEvent;
import org.jboss.netty.channel.MessageEvent;
import org.jboss.netty.channel.SimpleChannelUpstreamHandler;

import com.dianping.pigeon.extension.ExtensionLoader;
import com.dianping.pigeon.log.LoggerLoader;
import com.dianping.pigeon.monitor.MonitorLogger;
import com.dianping.pigeon.remoting.common.domain.InvocationRequest;
import com.dianping.pigeon.remoting.common.util.Constants;
import com.dianping.pigeon.remoting.common.util.ResponseUtils;
import com.dianping.pigeon.remoting.provider.domain.DefaultProviderContext;
import com.dianping.pigeon.remoting.provider.domain.ProviderContext;

public class NettyServerHandler extends SimpleChannelUpstreamHandler {

	private static final Logger log = LoggerLoader.getLogger(NettyServerHandler.class);
	private static final MonitorLogger monitorLogger = ExtensionLoader.getExtension(MonitorLogger.class);

	private NettyServer server;

	public NettyServerHandler(NettyServer server) {
		this.server = server;
	}

	@Override
	public void handleUpstream(ChannelHandlerContext ctx, ChannelEvent e) throws Exception {
		if (e instanceof ChannelStateEvent && ((ChannelStateEvent) e).getState() != ChannelState.INTEREST_OPS) {
			if (log.isInfoEnabled()) {
				log.info(e.toString());
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
		for (InvocationRequest request : messages) {
			ProviderContext invocationContext = new DefaultProviderContext(request, new NettyChannel(ctx.getChannel()));
			try {
				this.server.processRequest(request, invocationContext);
			} catch (Exception e) {
				String msg = "process request failed, seq--" + request.getSequence() + "\r\n";
				// 心跳消息只返回正常的, 异常不返回
				if (request.getCallType() == Constants.CALLTYPE_REPLY
						&& request.getMessageType() != Constants.MESSAGE_TYPE_HEART) {
					ctx.getChannel().write(ResponseUtils.createFailResponse(request, e));
				}
				log.error(msg + "****SEQ:" + request.getSequence() + "****callType:" + request.getCallType(), e);
				monitorLogger.logError(e);
			}
		}
	}

	@Override
	public void channelConnected(ChannelHandlerContext ctx, ChannelStateEvent e) {
		if (log.isInfoEnabled()) {
			log.info("pigeon server channel connected....");
		}
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
