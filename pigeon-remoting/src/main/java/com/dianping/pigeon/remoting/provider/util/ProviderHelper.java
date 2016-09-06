/**
 * Dianping.com Inc.
 * Copyright (c) 2003-2013 All Rights Reserved.
 */
package com.dianping.pigeon.remoting.provider.util;

import java.util.List;

import com.dianping.pigeon.monitor.Monitor;
import com.dianping.pigeon.monitor.MonitorLoader;
import com.dianping.pigeon.monitor.MonitorTransaction;
import com.dianping.pigeon.remoting.common.domain.InvocationContext.TimePhase;
import com.dianping.pigeon.remoting.common.domain.InvocationContext.TimePoint;
import com.dianping.pigeon.remoting.common.domain.InvocationRequest;
import com.dianping.pigeon.remoting.common.domain.InvocationResponse;
import com.dianping.pigeon.remoting.common.monitor.SizeMonitor;
import com.dianping.pigeon.remoting.common.util.Constants;
import com.dianping.pigeon.remoting.provider.domain.ProviderChannel;
import com.dianping.pigeon.remoting.provider.domain.ProviderContext;
import com.dianping.pigeon.remoting.provider.process.ProviderInterceptor;
import com.dianping.pigeon.remoting.provider.process.ProviderInterceptorFactory;
import com.dianping.pigeon.remoting.provider.process.ProviderProcessInterceptor;
import com.dianping.pigeon.remoting.provider.process.ProviderProcessInterceptorFactory;
import com.dianping.pigeon.remoting.provider.process.statistics.ProviderStatisticsHolder;

public final class ProviderHelper {

	private static final Monitor monitor = MonitorLoader.getMonitor();

	private static ThreadLocal<ProviderContext> tlContext = new ThreadLocal<ProviderContext>();

	public static void setContext(ProviderContext context) {
		tlContext.set(context);
	}

	public static ProviderContext getContext() {
		ProviderContext context = tlContext.get();
		tlContext.remove();
		return context;
	}

	public static void clearContext() {
		tlContext.remove();
	}

	public static void writeSuccessResponse(ProviderContext context, Object returnObj) {
		if (context == null) {
			return;
		}
		InvocationRequest request = context.getRequest();
		InvocationResponse response = null;
		if (Constants.REPLY_MANUAL && request.getCallType() != Constants.CALLTYPE_NOREPLY) {
			response = ProviderUtils.createSuccessResponse(request, returnObj);
			context.getTimeline().add(new TimePoint(TimePhase.B, System.currentTimeMillis()));
			ProviderChannel channel = context.getChannel();
			MonitorTransaction transaction = null;
			if (Constants.MONITOR_ENABLE) {
				MonitorTransaction currentTransaction = monitor.getCurrentServiceTransaction();
				try {
					if (currentTransaction == null) {
						transaction = monitor.createTransaction("PigeonServiceCallback", context.getMethodUri(),
								context);
						if (transaction != null) {
							transaction.setStatusOk();
							monitor.logEvent("PigeonService.app", request.getApp(), "");
							transaction.addData("CallType", request.getCallType());
							String reqSize = SizeMonitor.getInstance().getLogSize(request.getSize());
							if (reqSize != null) {
								monitor.logEvent("PigeonService.requestSize", reqSize, "" + request.getSize());
							}
						}
					}
				} catch (Throwable e) {
					monitor.logMonitorError(e);
				}
				try {
					channel.write(response);
				} finally {
					if (Constants.MONITOR_ENABLE) {
						if (response != null && response.getSize() > 0) {
							String respSize = SizeMonitor.getInstance().getLogSize(response.getSize());
							if (respSize != null) {
								monitor.logEvent("PigeonService.responseSize", respSize, "" + response.getSize());
							}
						}
						if (transaction != null) {
							context.getTimeline().add(new TimePoint(TimePhase.E, System.currentTimeMillis()));
							try {
								transaction.complete();
							} catch (Throwable e) {
								monitor.logMonitorError(e);
							}
						}
					}
				}
			}
		}
		ProviderStatisticsHolder.flowOut(request);
		List<ProviderProcessInterceptor> interceptors = ProviderProcessInterceptorFactory.getInterceptors();
		for (ProviderProcessInterceptor interceptor : interceptors) {
			interceptor.postInvoke(request, response);
		}
		List<ProviderInterceptor> contextInterceptors = ProviderInterceptorFactory.getInterceptors();
		for (ProviderInterceptor interceptor : contextInterceptors) {
			interceptor.postInvoke(context);
		}
	}

	public static void writeFailureResponse(ProviderContext context, Throwable exeption) {
		if (Constants.REPLY_MANUAL) {
			InvocationRequest request = context.getRequest();
			InvocationResponse response = ProviderUtils.createServiceExceptionResponse(request, exeption);
			ProviderChannel channel = context.getChannel();
			channel.write(response);
			ProviderStatisticsHolder.flowOut(request);
			List<ProviderProcessInterceptor> interceptors = ProviderProcessInterceptorFactory.getInterceptors();
			for (ProviderProcessInterceptor interceptor : interceptors) {
				interceptor.postInvoke(request, response);
			}
			List<ProviderInterceptor> contextInterceptors = ProviderInterceptorFactory.getInterceptors();
			for (ProviderInterceptor interceptor : contextInterceptors) {
				interceptor.postInvoke(context);
			}
		}
	}
}
