/**
 * Dianping.com Inc.
 * Copyright (c) 2003-2013 All Rights Reserved.
 */
package com.dianping.pigeon.remoting.provider.util;

import java.util.List;

import com.dianping.pigeon.monitor.Monitor;
import com.dianping.pigeon.monitor.MonitorLoader;
import com.dianping.pigeon.monitor.MonitorTransaction;
import com.dianping.pigeon.remoting.common.domain.InvocationRequest;
import com.dianping.pigeon.remoting.common.domain.InvocationResponse;
import com.dianping.pigeon.remoting.common.monitor.SizeMonitor;
import com.dianping.pigeon.remoting.common.util.Constants;
import com.dianping.pigeon.remoting.provider.domain.ProviderChannel;
import com.dianping.pigeon.remoting.provider.domain.ProviderContext;
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
		if (context != null) {
			context.setMonitorTransaction(monitor.getCurrentServiceTransaction());
		}
		return context;
	}

	public static void writeSuccessResponse(ProviderContext context, Object returnObj) {
		if (Constants.REPLY_MANUAL) {
			InvocationRequest request = context.getRequest();
			InvocationResponse response = ProviderUtils.createSuccessResponse(request, returnObj);
			ProviderChannel channel = context.getChannel();
			try {
				channel.write(response);
			} finally {
				MonitorTransaction transaction = context.getMonitorTransaction();
				if (transaction != null) {
					MonitorTransaction newTransaction = monitor.copyTransaction("PigeonServiceCallback",
							transaction.getUri(), context, transaction);
					if (newTransaction != null) {
						if (response != null) {
							newTransaction.logEvent("PigeonService.responseSize",
									SizeMonitor.getInstance().getLogSize(response.getSize()), "" + response.getSize());
						}
						newTransaction.setStatusOk();
						newTransaction.complete();
					}
					transaction.complete();
				}
				ProviderStatisticsHolder.flowOut(request);
			}
			List<ProviderProcessInterceptor> interceptors = ProviderProcessInterceptorFactory.getInterceptors();
			for (ProviderProcessInterceptor interceptor : interceptors) {
				interceptor.postInvoke(request, response);
			}
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
		}
	}
}
