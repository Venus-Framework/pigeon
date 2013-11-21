/**
s * Dianping.com Inc.
 * Copyright (c) 2003-${year} All Rights Reserved.
 */
package com.dianping.pigeon.remoting.provider.process.filter;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import org.apache.log4j.Logger;

import com.dianping.pigeon.component.invocation.InvocationRequest;
import com.dianping.pigeon.component.invocation.InvocationResponse;
import com.dianping.pigeon.monitor.LoggerLoader;
import com.dianping.pigeon.remoting.common.filter.ServiceInvocationFilter;
import com.dianping.pigeon.remoting.common.filter.ServiceInvocationHandler;
import com.dianping.pigeon.remoting.common.util.Constants;
import com.dianping.pigeon.remoting.common.util.ResponseUtils;
import com.dianping.pigeon.remoting.provider.component.context.ProviderContext;
import com.dianping.pigeon.remoting.provider.service.method.ServiceMethod;
import com.dianping.pigeon.remoting.provider.service.method.ServiceMethodFactory;
import com.dianping.pigeon.util.ContextUtils;

/**
 * 
 * 
 * @author jianhuihuang
 * @version $Id: BusinessProcessFilter.java, v 0.1 2013-6-30 下午8:33:49
 *          jianhuihuang Exp $
 */
public class BusinessProcessFilter implements ServiceInvocationFilter<ProviderContext> {

	private static final Logger logger = LoggerLoader.getLogger(BusinessProcessFilter.class);

	@Override
	public InvocationResponse invoke(ServiceInvocationHandler handler, ProviderContext invocationContext)
			throws Throwable {
		InvocationRequest request = invocationContext.getRequest();
		if (request.getMessageType() == Constants.MESSAGE_TYPE_SERVICE) {
			//ContextUtils.putLocalContext(Constants.REQUEST_CREATE_TIME, System.currentTimeMillis());
			ContextUtils.putLocalContext(Constants.REQUEST_TIMEOUT, request.getTimeout());

			InvocationResponse response = null;
			ServiceMethod method = ServiceMethodFactory.getMethod(request);
			Method method_ = method.getMethod();
			try {
				// long currentTime = 0;
				// if (logger.isDebugEnabled()) {
				// currentTime = System.nanoTime();
				// }

				Object returnObj = method_.invoke(method.getService(), request.getParameters());

				// if (logger.isDebugEnabled()) {
				// logger.debug("service:" + request.getServiceName() + "_" +
				// request.getMethodName());
				// logger.debug("execute time:" + (System.nanoTime() -
				// currentTime) / 1000);
				// logger.debug("RequestId:" + request.getSequence());
				// }
				if (request.getCallType() == Constants.CALLTYPE_REPLY) {
					response = ResponseUtils.createSuccessResponse(request, returnObj);
				}
			} catch (InvocationTargetException e) {
				Throwable e2 = e.getTargetException();
				if (e2 != null) {
					logger.error(e2.getMessage(), e2);
				}
				if (request.getCallType() == Constants.CALLTYPE_REPLY) {
					response = ResponseUtils.createServiceExceptionResponse(request, e2);
				}
				invocationContext.setServiceError(e2);
			} catch (Exception e) {
				invocationContext.setServiceError(e);
				throw e;
			}

			return response;
		}
		throw new RuntimeException("Message type[" + request.getMessageType() + "] is not supported!");
	}

}
