/**
 * Dianping.com Inc.
 * Copyright (c) 00-0 All Rights Reserved.
 */
package com.dianping.pigeon.remoting.provider.process;

import com.dianping.pigeon.component.invocation.InvocationRequest;
import com.dianping.pigeon.remoting.provider.component.context.ProviderContext;

public interface RequestProcessor {

	public void stop();

	public void processRequest(final InvocationRequest request, final ProviderContext providerContext);

}
