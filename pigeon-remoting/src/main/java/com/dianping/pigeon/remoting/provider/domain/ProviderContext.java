/**
 * Dianping.com Inc.
 * Copyright (c) 2003-2013 All Rights Reserved.
 */
package com.dianping.pigeon.remoting.provider.domain;

import java.util.concurrent.Future;

import com.dianping.pigeon.remoting.common.domain.InvocationContext;

/**
 * 
 * 
 * @author jianhuihuang
 * @version $Id: InvocationProcessContext.java, v 0.1 2013-6-30 下午7:59:15
 *          jianhuihuang Exp $
 */
public interface ProviderContext extends InvocationContext {

	Throwable getServiceError();

	void setServiceError(Throwable serviceError);

	ProviderChannel getChannel();
	
	Future<?> getFuture();
	
	void setFuture(Future<?> future);
}
