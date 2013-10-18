/**
 * Dianping.com Inc.
 * Copyright (c) 2003-2013 All Rights Reserved.
 */
package com.dianping.pigeon.remoting.common.filter;

import com.dianping.dpsf.component.DPSFResponse;
import com.dianping.pigeon.remoting.common.component.context.InvocationContext;

public interface ServiceInvocationHandler {

	/**
	 * 
	 * 
	 * @param invocationContext
	 * @return
	 * @throws Throwable
	 */
	DPSFResponse handle(InvocationContext invocationContext) throws Throwable;

}
