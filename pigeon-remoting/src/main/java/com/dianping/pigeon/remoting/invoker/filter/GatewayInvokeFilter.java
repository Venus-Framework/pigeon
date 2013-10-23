/**
 * Dianping.com Inc.
 * Copyright (c) 2003-2013 All Rights Reserved.
 */
package com.dianping.pigeon.remoting.invoker.filter;

import org.apache.log4j.Logger;

import com.dianping.pigeon.component.invocation.InvocationResponse;
import com.dianping.pigeon.remoting.common.filter.ServiceInvocationHandler;
import com.dianping.pigeon.remoting.common.util.Constants;
import com.dianping.pigeon.remoting.invoker.component.InvokerMetaData;
import com.dianping.pigeon.remoting.invoker.component.async.ServiceFutureFactory;
import com.dianping.pigeon.remoting.invoker.component.context.InvokerContext;

/**
 * 
 * 
 * @author jianhuihuang
 * @version $Id: GatewayInvokeFilter.java, v 0.1 2013-6-20 下午9:50:16
 *          jianhuihuang Exp $
 */
public class GatewayInvokeFilter extends InvocationInvokeFilter {

	private static final Logger logger = Logger.getLogger(GatewayInvokeFilter.class);

	@Override
	public InvocationResponse invoke(ServiceInvocationHandler handler, InvokerContext invocationContext)
			throws Throwable {

		InvokerMetaData metaData = invocationContext.getMetaData();
		if (logger.isDebugEnabled()) {
			logger.debug("GatewayInvokeFilter invoke");
			logger.debug("metaData" + metaData);
		}
		try {
			return handler.handle(invocationContext);
		} catch (Throwable e) {
			if (Constants.CALL_FUTURE.equalsIgnoreCase(metaData.getCallMethod())) {
				ServiceFutureFactory.remove();
			}
			throw e;
		}
	}

}
