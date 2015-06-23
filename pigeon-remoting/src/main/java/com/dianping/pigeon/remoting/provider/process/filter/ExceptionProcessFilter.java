/**
 * Dianping.com Inc.
 * Copyright (c) 2003-${year} All Rights Reserved.
 */
package com.dianping.pigeon.remoting.provider.process.filter;

import java.lang.reflect.InvocationTargetException;

import org.apache.logging.log4j.Logger;

import com.dianping.pigeon.config.ConfigChangeListener;
import com.dianping.pigeon.config.ConfigManagerLoader;
import com.dianping.pigeon.log.LoggerLoader;
import com.dianping.pigeon.remoting.common.domain.InvocationRequest;
import com.dianping.pigeon.remoting.common.domain.InvocationResponse;
import com.dianping.pigeon.remoting.common.process.ServiceInvocationFilter;
import com.dianping.pigeon.remoting.common.process.ServiceInvocationHandler;
import com.dianping.pigeon.remoting.common.util.Constants;
import com.dianping.pigeon.remoting.provider.domain.ProviderContext;
import com.dianping.pigeon.remoting.provider.util.ProviderUtils;

/**
 * 
 * 
 */
public class ExceptionProcessFilter implements ServiceInvocationFilter<ProviderContext> {

	private static final Logger logger = LoggerLoader.getLogger(ExceptionProcessFilter.class);
	private static boolean logServiceException = ConfigManagerLoader.getConfigManager().getBooleanValue(
			"pigeon.provider.logserviceexception", true);

	public ExceptionProcessFilter() {
		ConfigManagerLoader.getConfigManager().registerConfigChangeListener(new InnerConfigChangeListener());
	}

	private static class InnerConfigChangeListener implements ConfigChangeListener {

		@Override
		public void onKeyUpdated(String key, String value) {
			if (key.endsWith("pigeon.provider.logserviceexception")) {
				try {
					logServiceException = Boolean.valueOf(value);
				} catch (RuntimeException e) {
				}
			}
		}

		@Override
		public void onKeyAdded(String key, String value) {

		}

		@Override
		public void onKeyRemoved(String key) {

		}

	}

	@Override
	public InvocationResponse invoke(ServiceInvocationHandler handler, ProviderContext invocationContext)
			throws Throwable {
		if (logger.isDebugEnabled()) {
			logger.debug("invoke the ExceptionProcessFilter, invocationContext:" + invocationContext);
		}
		InvocationRequest request = invocationContext.getRequest();
		InvocationResponse response = null;
		try {
			response = handler.handle(invocationContext);
		} catch (InvocationTargetException e) {
			Throwable e2 = e.getTargetException();
			if (e2 != null) {
				boolean isLog = logServiceException;
				if (e2 instanceof Error) {
					isLog = true;
				}
				if (isLog) {
					logger.error(e2.getMessage(), e2);
					invocationContext.setServiceError(e2);
				}
			}
			if (request.getCallType() == Constants.CALLTYPE_REPLY) {
				response = ProviderUtils.createServiceExceptionResponse(request, e2);
			}
		} catch (Throwable e) {
			logger.error(e.getMessage(), e);
			invocationContext.setServiceError(e);
			if (request.getCallType() == Constants.CALLTYPE_REPLY
					&& request.getMessageType() != Constants.MESSAGE_TYPE_HEART) {
				response = ProviderUtils.createFailResponse(request, e);
			}
		}
		return response;
	}

}
