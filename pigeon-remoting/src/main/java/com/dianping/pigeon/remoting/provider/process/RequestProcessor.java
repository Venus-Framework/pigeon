/**
 * Dianping.com Inc.
 * Copyright (c) 00-0 All Rights Reserved.
 */
package com.dianping.pigeon.remoting.provider.process;

import java.util.concurrent.Future;

import com.dianping.pigeon.remoting.common.domain.InvocationRequest;
import com.dianping.pigeon.remoting.common.domain.InvocationResponse;
import com.dianping.pigeon.remoting.provider.config.ProviderConfig;
import com.dianping.pigeon.remoting.provider.domain.ProviderContext;

public interface RequestProcessor {

	public void start();
	
	public void stop();

	public Future<InvocationResponse> processRequest(final InvocationRequest request, final ProviderContext providerContext);

	public String getProcessorStatistics();
	
	public String getProcessorStatistics(final InvocationRequest request);
	
	public <T> void addService(ProviderConfig<T> providerConfig);
	
	public <T> void removeService(ProviderConfig<T> providerConfig);
	
	public boolean needCancelRequest(InvocationRequest request);
}
