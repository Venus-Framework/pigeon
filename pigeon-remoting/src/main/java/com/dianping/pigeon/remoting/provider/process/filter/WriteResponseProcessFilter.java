/**
 * Dianping.com Inc.
 * Copyright (c) 2003-${year} All Rights Reserved.
 */
package com.dianping.pigeon.remoting.provider.process.filter;

import org.apache.log4j.Logger;

import com.dianping.pigeon.extension.ExtensionLoader;
import com.dianping.pigeon.log.LoggerLoader;
import com.dianping.pigeon.monitor.Monitor;
import com.dianping.pigeon.monitor.MonitorLogger;
import com.dianping.pigeon.remoting.common.domain.InvocationRequest;
import com.dianping.pigeon.remoting.common.domain.InvocationResponse;
import com.dianping.pigeon.remoting.common.process.ServiceInvocationFilter;
import com.dianping.pigeon.remoting.common.process.ServiceInvocationHandler;
import com.dianping.pigeon.remoting.common.util.Constants;
import com.dianping.pigeon.remoting.provider.domain.ProviderChannel;
import com.dianping.pigeon.remoting.provider.domain.ProviderContext;
import com.dianping.pigeon.remoting.provider.exception.ProcessTimeoutException;
import com.dianping.pigeon.util.ContextUtils;

/**
 * 
 * 
 * @author jianhuihuang
 * @version $Id: WriteResponseProcessFilter.java, v 0.1 2013-6-20 下午5:46:19
 *          jianhuihuang Exp $
 */
public class WriteResponseProcessFilter implements ServiceInvocationFilter<ProviderContext> {

	private static final Logger logger = LoggerLoader.getLogger(WriteResponseProcessFilter.class);
	private static final MonitorLogger monitorLogger = ExtensionLoader.getExtension(Monitor.class).getLogger();

	@Override
	public InvocationResponse invoke(ServiceInvocationHandler handler, ProviderContext invocationContext)
			throws Throwable {
		if (logger.isDebugEnabled()) {
			logger.debug("invoke the WriteResponseProcessFilter, invocationContext:" + invocationContext);
		}
		try {
			ProviderChannel channel = invocationContext.getChannel();
			InvocationRequest request = invocationContext.getRequest();
			InvocationResponse response = handler.handle(invocationContext);
			if (request.getCallType() == Constants.CALLTYPE_REPLY) {
				long currentTime = System.currentTimeMillis();
				channel.write(response);
				if (request.getTimeout() > 0 && request.getCreateMillisTime() > 0 && 
						request.getCreateMillisTime() + request.getTimeout() < currentTime) {
					StringBuffer msg = new StringBuffer();
					msg.append("request timeout,\r\nrequest:").append(request).append("\r\nresponse:").append(response);
					ProcessTimeoutException te = new ProcessTimeoutException(msg.toString());
					logger.error(te.getMessage(), te);
					if (monitorLogger != null) {
						monitorLogger.logError(te);
					}
				}
			}
			return response;
		} finally {
			ContextUtils.clearContext();
			ContextUtils.clearLocalContext();
		}
	}

}
