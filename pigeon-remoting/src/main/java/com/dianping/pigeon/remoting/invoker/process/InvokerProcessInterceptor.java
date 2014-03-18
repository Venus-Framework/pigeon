/**
 * Dianping.com Inc.
 * Copyright (c) 2003-2014 All Rights Reserved.
 */
package com.dianping.pigeon.remoting.invoker.process;

import com.dianping.pigeon.remoting.common.domain.InvocationRequest;
import com.dianping.pigeon.remoting.common.domain.InvocationResponse;

public interface InvokerProcessInterceptor {

	public void preInvoke(InvocationRequest invocationRequest);

	public void postInvoke(InvocationRequest invocationRequest, InvocationResponse invocationResponse);
}
