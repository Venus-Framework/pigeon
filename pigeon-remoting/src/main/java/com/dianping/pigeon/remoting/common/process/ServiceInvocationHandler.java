/**
 * Dianping.com Inc.
 * Copyright (c) 2003-2013 All Rights Reserved.
 */
package com.dianping.pigeon.remoting.common.process;

import com.dianping.pigeon.component.invocation.InvocationContext;
import com.dianping.pigeon.component.invocation.InvocationResponse;

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
