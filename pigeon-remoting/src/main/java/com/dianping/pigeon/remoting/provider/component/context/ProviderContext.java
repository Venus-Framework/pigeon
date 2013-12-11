/**
 * Dianping.com Inc.
 * Copyright (c) 2003-2013 All Rights Reserved.
 */
package com.dianping.pigeon.remoting.provider.component.context;

import com.dianping.pigeon.remoting.common.component.invocation.InvocationContext;
import com.dianping.pigeon.remoting.provider.component.ProviderChannel;

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

}
