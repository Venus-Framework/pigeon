/**
 * Dianping.com Inc.
 * Copyright (c) 2003-2013 All Rights Reserved.
 */
package com.dianping.pigeon.remoting.provider.listener;

import java.util.Map;
import java.util.concurrent.Future;

import org.apache.log4j.Logger;

import com.dianping.dpsf.exception.NetTimeoutException;
import com.dianping.pigeon.component.invocation.InvocationRequest;
import com.dianping.pigeon.extension.ExtensionLoader;
import com.dianping.pigeon.monitor.MonitorLogger;
import com.dianping.pigeon.remoting.common.util.Constants;
import com.dianping.pigeon.remoting.provider.component.context.RequestContext;
import com.dianping.pigeon.util.ContextUtils;

/**
 * 服务器端的超时检查
 * 
 * @author jianhuihuang
 * @version $Id: TimeoutCheck.java, v 0.1 2013-7-19 上午11:18:08 jianhuihuang Exp
 *          $
 */
public class TimeoutListener implements Runnable {

	private static final Logger logger = Logger.getLogger(TimeoutListener.class);
	private static final MonitorLogger monitorLogger = ExtensionLoader.getExtension(MonitorLogger.class);
	private Map<InvocationRequest, RequestContext> contexts;

	public TimeoutListener(Map<InvocationRequest, RequestContext> contexts) {

		this.contexts = contexts;
	}

	public void run() {
		while (true) {
			try {
				long currentTime = System.currentTimeMillis();
				for (InvocationRequest request : contexts.keySet()) {
					if (request.getCreateMillisTime() + request.getTimeout() < currentTime) {
						try {
							RequestContext rc = contexts.get(request);
							if (request.getMessageType() == Constants.MESSAGE_TYPE_HEART) {
								@SuppressWarnings("rawtypes")
								Future future = rc.getFuture();
								if (future != null) {
									future.cancel(false);
								}
							} else {
								// 记录超时堆栈
								// TODO, 需要加强日志，把参数也打印出来？如果是一些敏感信息，是否涉及安全？
								// 打印一个cat的messageid？
								NetTimeoutException te;
								StringBuilder msg = new StringBuilder();
								msg.append("DPSF RequestExecutor timeout seq:").append(request.getSequence());
								msg.append("  ip:").append(rc.getHost()).append("  timeout:" + request.getTimeout())
										.append("  createTime:").append(request.getCreateMillisTime()).append("\r\n")
										.append("  serviceName:").append(request.getServiceName()).append("\r\n");
								Object[] params = request.getParameters();
								if (params != null && params.length > 0) {
									for (Object param : params) {
										msg.append("<><>").append(String.valueOf(param));
									}
									msg.append("\r\n");
								}
								Thread t = rc.getThread();
								if (t == null) {
									msg.append(" and task has been not executed by threadPool");

									te = new NetTimeoutException(msg.toString());
								} else {
									te = new NetTimeoutException(msg.toString());
									te.setStackTrace(t.getStackTrace());
								}
								ContextUtils.setContext(request.getContext());

								logger.error(te.getMessage(), te);
								if (monitorLogger != null) {
									monitorLogger.logError(te);
								}

								@SuppressWarnings("rawtypes")
								Future fu = rc.getFuture();
								if (fu != null) {
									fu.cancel(false);
								} else {
									logger.error("<<<<<< No Future for Request  \r\n" + msg.toString());
								}
							}
						} finally {
							contexts.remove(request);
						}

					}
				}
				Thread.sleep(1000);
			} catch (Exception e) {
				logger.error(e.getMessage(), e);
			}
		}
	}
}
