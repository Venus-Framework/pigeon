/**
 * Dianping.com Inc.
 * Copyright (c) 2003-2013 All Rights Reserved.
 */
package com.dianping.pigeon.remoting.invoker.component.context;

import java.lang.reflect.Method;

import com.dianping.pigeon.component.invocation.InvocationContext;
import com.dianping.pigeon.remoting.invoker.Client;
import com.dianping.pigeon.remoting.invoker.component.InvokerMetaData;

public interface InvokerContext extends InvocationContext {

	InvokerMetaData getMetaData();

	Method getMethod();

	Object[] getArguments();

	Client getClient();

	void setClient(Client client);

}
