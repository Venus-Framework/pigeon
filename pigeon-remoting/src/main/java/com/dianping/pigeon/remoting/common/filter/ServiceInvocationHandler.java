/**
 * Dianping.com Inc.
 * Copyright (c) 2003-2013 All Rights Reserved.
 */
package com.dianping.pigeon.remoting.common.filter;

import com.dianping.pigeon.component.invocation.InvocationResponse;
import com.dianping.pigeon.remoting.common.component.context.InvocationContext;

public interface ServiceInvocationHandler {

	/**
	 * 
	 * 
	 * @param invocationContext
	 * @return
	 * @throws Throwable
	 */
	InvocationResponse handle(InvocationContext invocationContext) throws Throwable;

}
