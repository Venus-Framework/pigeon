/**
 * Dianping.com Inc.
 * Copyright (c) 2003-2013 All Rights Reserved.
 */
/**
 * 
 */
package com.dianping.pigeon.remoting.provider.listener;

import java.util.Map;
import java.util.concurrent.Future;

import org.apache.log4j.Logger;

import com.dianping.dpsf.exception.NetTimeoutException;
import com.dianping.pigeon.extension.ExtensionLoader;
import com.dianping.pigeon.log.LoggerLoader;
import com.dianping.pigeon.monitor.MonitorLogger;
import com.dianping.pigeon.remoting.common.component.invocation.InvocationRequest;
import com.dianping.pigeon.remoting.common.util.Constants;
import com.dianping.pigeon.remoting.provider.component.context.ProviderContext;
import com.dianping.pigeon.util.ContextUtils;

public class TimeoutListener implements Runnable {

	private static final Logger logger = LoggerLoader.getLogger(TimeoutListener.class);
	private static final MonitorLogger monitorLogger = ExtensionLoader.getExtension(MonitorLogger.class);
	private Map<InvocationRequest, ProviderContext> context;

	public TimeoutListener(Map<InvocationRequest, ProviderContext> context) {
		this.context = context;
	}

	public void run() {
//		while (true) {
//			try {
//				long currentTime = System.currentTimeMillis();
//				for (InvocationRequest request : context.keySet()) {
//					if (request.getCreateMillisTime() + request.getTimeout() < currentTime) {
//						try {
//							ProviderContext rc = context.get(request);
//							if (request.getMessageType() == Constants.MESSAGE_TYPE_HEART) {
//								@SuppressWarnings("rawtypes")
//								Future future = rc.ge.getFuture();
//								if (future != null) {
//									future.cancel(false);
//								}
//							} else {
//								// 记录超时堆栈
//								// TODO, 需要加强日志，把参数也打印出来？如果是一些敏感信息，是否涉及安全？
//								// 打印一个cat的messageid？
//								NetTimeoutException te;
//								StringBuffer msg = new StringBuffer();
//								msg.append("DPSF RequestExecutor timeout seq:").append(request.getSequence());
//								msg.append("  ip:").append(rc.getHost()).append("  timeout:" + request.getTimeout())
//										.append("  createTime:").append(request.getCreateMillisTime()).append("\r\n")
//										.append("  serviceName:").append(request.getServiceName()).append("\r\n");
//								Object[] params = request.getParameters();
//								if (params != null && params.length > 0) {
//									for (Object param : params) {
//										msg.append("<><>").append(String.valueOf(param));
//									}
//									msg.append("\r\n");
//								}
//								Thread t = rc.getThread();
//								if (t == null) {
//									msg.append(" and task has been not executed by threadPool");
//									te = new NetTimeoutException(msg.toString());
//								} else {
//									te = new NetTimeoutException(msg.toString());
//									te.setStackTrace(t.getStackTrace());
//								}
//								ContextUtils.setContext(request.getContext());
//
//								logger.error(te.getMessage(), te);
//								if (monitorLogger != null) {
//									monitorLogger.logError(te);
//								}
//
//								@SuppressWarnings("rawtypes")
//								Future fu = rc.getFuture();
//								if (fu != null) {
//									fu.cancel(false);
//								} else {
//									logger.error("<<<<<< No Future for Request  \r\n" + msg.toString());
//								}
//							}
//						} finally {
//							context.remove(request);
//						}
//
//					}
//				}
//				Thread.sleep(1000);
//			} catch (Exception e) {
//				logger.error(e.getMessage(), e);
//			}
//		}
	}
}
