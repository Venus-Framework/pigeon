/**
 * Dianping.com Inc.
 * Copyright (c) 2003-2013 All Rights Reserved.
 */
package com.dianping.pigeon.remoting.provider.domain;

import java.util.concurrent.Future;

import com.dianping.pigeon.remoting.common.domain.InvocationContext;
import com.dianping.pigeon.remoting.provider.service.method.ServiceMethod;

public interface ProviderContext extends InvocationContext {

	Throwable getServiceError();

	void setServiceError(Throwable serviceError);

	ProviderChannel getChannel();

	Future<?> getFuture();

	void setFuture(Future<?> future);

	Thread getThread();
	
	void setThread(Thread thread);
	
	void setServiceMethod(ServiceMethod serviceMethod);
	
	ServiceMethod getServiceMethod();
}
