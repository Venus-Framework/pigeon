/**
 * Dianping.com Inc.
 * Copyright (c) 2003-2013 All Rights Reserved.
 */
package com.dianping.pigeon.remoting.invoker.component.context;

import com.dianping.pigeon.remoting.common.component.invocation.InvocationContext;
import com.dianping.pigeon.remoting.invoker.Client;
import com.dianping.pigeon.remoting.invoker.config.InvokerConfig;

public interface InvokerContext extends InvocationContext {

	InvokerConfig<?> getInvokerConfig();

	String getMethodName();
	
	Class<?>[] getParameterTypes();

	Object[] getArguments();

	Client getClient();

	void setClient(Client client);

}
