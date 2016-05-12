/**
 * Dianping.com Inc.
 * Copyright (c) 2003-2013 All Rights Reserved.
 */
package com.dianping.pigeon.remoting.invoker.domain;

import com.dianping.pigeon.remoting.common.domain.InvocationContext;
import com.dianping.pigeon.remoting.invoker.Client;
import com.dianping.pigeon.remoting.invoker.config.InvokerConfig;

public interface InvokerContext extends InvocationContext {

	InvokerConfig<?> getInvokerConfig();

	String getMethodName();
	
	Class<?>[] getParameterTypes();

	Object[] getArguments();

	Client getClient();

	void setClient(Client client);

	void setDegraded();
	
	boolean isDegraded();
}
