/**
 * Dianping.com Inc.
 * Copyright (c) 00-0 All Rights Reserved.
 */
package com.dianping.pigeon.remoting.provider.process;

import java.util.concurrent.Future;

import com.dianping.pigeon.remoting.common.domain.InvocationRequest;
import com.dianping.pigeon.remoting.common.domain.InvocationResponse;
import com.dianping.pigeon.remoting.provider.domain.ProviderContext;

public interface RequestProcessor {

	public void stop();

	public Future<InvocationResponse> processRequest(final InvocationRequest request, final ProviderContext providerContext);

}
