/**
 * Dianping.com Inc.
 * Copyright (c) 2003-2013 All Rights Reserved.
 */
package com.dianping.pigeon.remoting.provider;

import java.util.concurrent.Future;

import com.dianping.pigeon.remoting.common.domain.InvocationRequest;
import com.dianping.pigeon.remoting.common.domain.InvocationResponse;
import com.dianping.pigeon.remoting.common.exception.RpcException;
import com.dianping.pigeon.remoting.provider.config.ProviderConfig;
import com.dianping.pigeon.remoting.provider.config.ServerConfig;
import com.dianping.pigeon.remoting.provider.domain.ProviderContext;

/**
 * 
 * 
 * @author jianhuihuang
 * @version $Id: Server.java, v 0.1 2013-6-17 下午6:38:37 jianhuihuang Exp $
 */
public interface Server {

	boolean support(ServerConfig serverConfig);
	
	void start(ServerConfig serverConfig);

	void stop();

	ServerConfig getServerConfig();
	
	int getPort();
	
	String getRegistryUrl(String url);

	public Future<InvocationResponse> processRequest(final InvocationRequest request, final ProviderContext providerContext);

	public <T> void addService(ProviderConfig<T> providerConfig) throws RpcException;
	
}
