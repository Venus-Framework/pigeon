/**
 * Dianping.com Inc.
 * Copyright (c) 2003-2014 All Rights Reserved.
 */
package com.dianping.pigeon.remoting.invoker.process;

import com.dianping.pigeon.remoting.invoker.domain.InvokerContext;

public interface InvokerInterceptor {

	public void preInvoke(InvokerContext invokerContext);

	public void postInvoke(InvokerContext invokerContext);

	public void afterThrowing(InvokerContext invokerContext, Throwable throwable);
}
