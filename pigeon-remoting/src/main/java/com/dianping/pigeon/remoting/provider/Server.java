/**
 * Dianping.com Inc.
 * Copyright (c) 2003-2013 All Rights Reserved.
 */
package com.dianping.pigeon.remoting.provider;

import java.util.List;
import java.util.concurrent.Future;

import com.dianping.pigeon.remoting.common.domain.InvocationRequest;
import com.dianping.pigeon.remoting.common.domain.InvocationResponse;
import com.dianping.pigeon.remoting.provider.config.ProviderConfig;
import com.dianping.pigeon.remoting.provider.config.ServerConfig;
import com.dianping.pigeon.remoting.provider.domain.ProviderContext;
import com.dianping.pigeon.remoting.provider.process.RequestProcessor;

public interface Server {

	public boolean isStarted();
	
	public boolean support(ServerConfig serverConfig);

	public void start(ServerConfig serverConfig);

	public void stop();

	public ServerConfig getServerConfig();

	public int getPort();

	public String getRegistryUrl(String url);

	public Future<InvocationResponse> processRequest(final InvocationRequest request,
			final ProviderContext providerContext);

	public <T> void addService(ProviderConfig<T> providerConfig);
	
	public <T> void removeService(ProviderConfig<T> providerConfig);

	public List<String> getInvokerMetaInfo();
	
	public String getProtocol();
	
	public RequestProcessor getRequestProcessor();
}
