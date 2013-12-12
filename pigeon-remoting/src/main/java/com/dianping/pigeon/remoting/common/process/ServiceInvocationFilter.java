/**
 * Dianping.com Inc.
 * Copyright (c) 2003-2013 All Rights Reserved.
 */
package com.dianping.pigeon.remoting.common.process;

import com.dianping.pigeon.remoting.common.domain.InvocationContext;
import com.dianping.pigeon.remoting.common.domain.InvocationResponse;

public interface ServiceInvocationFilter<I extends InvocationContext> {

	/**
	 * 
	 * 
	 * @param handler
	 * @param invocationContext
	 * @return
	 * @throws Throwable
	 */
	InvocationResponse invoke(ServiceInvocationHandler handler, I invocationContext) throws Throwable;

}
