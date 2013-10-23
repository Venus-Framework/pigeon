/**
 * Dianping.com Inc.
 * Copyright (c) 2003-2013 All Rights Reserved.
 */
package com.dianping.pigeon.remoting.netty.invoker.process;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor.CallerRunsPolicy;

import org.apache.log4j.Logger;
import org.jboss.netty.channel.ChannelEvent;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.ChannelStateEvent;
import org.jboss.netty.channel.ExceptionEvent;
import org.jboss.netty.channel.MessageEvent;
import org.jboss.netty.channel.SimpleChannelUpstreamHandler;

import com.dianping.pigeon.component.invocation.InvocationRequest;
import com.dianping.pigeon.component.invocation.InvocationResponse;
import com.dianping.pigeon.remoting.common.util.Constants;
import com.dianping.pigeon.remoting.invoker.Client;
import com.dianping.pigeon.remoting.invoker.util.RpcEventUtils;
import com.dianping.pigeon.remoting.netty.codec.NettyCodecUtils;
import com.dianping.pigeon.threadpool.DefaultThreadPool;
import com.dianping.pigeon.threadpool.ThreadPool;

public class ClientChannelHandler extends SimpleChannelUpstreamHandler {

	private static final Logger log = Logger.getLogger(ClientChannelHandler.class);

	private Client client;

	private static ThreadPool clientProcessThreadPool = new DefaultThreadPool(
			Constants.THREADNAME_CLIENT_PRESPONSE_PROCESSOR, 20, 300, new LinkedBlockingQueue<Runnable>(50),
			new CallerRunsPolicy());

	public ClientChannelHandler(Client client) {
		this.client = client;

	}

	@Override
	public void handleUpstream(ChannelHandlerContext ctx, ChannelEvent e) throws Exception {
		super.handleUpstream(ctx, e);

	}

	@SuppressWarnings("unchecked")
	@Override
	public void messageReceived(ChannelHandlerContext ctx, MessageEvent e) {

		List<InvocationResponse> messages = (List<InvocationResponse>) e.getMessage();
		for (final InvocationResponse response : messages) {
			Runnable task = new Runnable() {
				public void run() {
					client.doResponse(response);
				}
			};
			try {
				// TODO [v1.7.0, danson.liu]对于callback调用, 防止callback阻塞response
				// handler thread pool线程池, 影响其他正常响应无法处理
				clientProcessThreadPool.execute(task);
			} catch (Exception ex) {
				String msg = "Response execute fail:seq--" + response.getSequence() + "\r\n";
				log.error(msg + ex.getMessage(), ex);
			}

		}

	}

	@Override
	public void channelConnected(ChannelHandlerContext ctx, ChannelStateEvent e) throws Exception {
	}

	@Override
	public void exceptionCaught(ChannelHandlerContext ctx, ExceptionEvent e) {
		final ExceptionEvent e_ = e;
		final Object attachment = NettyCodecUtils.getAttachment(ctx, Constants.ATTACHMENT_RETRY);
		flowOutexceptionCaughtRequest(attachment);
		if (e.getCause() instanceof IOException) {
			e.getChannel().close();
			Runnable task = new Runnable() {
				public void run() {
					client.connectionException(attachment, e_.getCause());
				}
			};
			clientProcessThreadPool.execute(task);
		}
	}

	private void flowOutexceptionCaughtRequest(final Object attachment) {
		InvocationRequest request = getRequest(attachment);
		if (request != null) {
			RpcEventUtils.channelExceptionCaughtEvent(request, client.getAddress());
		}
	}

	private InvocationRequest getRequest(Object attachment) {
		if (attachment instanceof Object[]) {
			Object[] msg = (Object[]) attachment;
			for (Object ele : msg) {
				if (ele instanceof InvocationRequest) {
					return (InvocationRequest) ele;
				}
			}
		} else if (attachment instanceof InvocationRequest) {
			return (InvocationRequest) attachment;
		}
		return null;
	}

}
