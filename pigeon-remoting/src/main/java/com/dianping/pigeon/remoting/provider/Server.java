/**
 * Dianping.com Inc.
 * Copyright (c) 2003-2013 All Rights Reserved.
 */
package com.dianping.pigeon.remoting.provider;

import com.dianping.pigeon.component.invocation.InvocationRequest;
import com.dianping.pigeon.remoting.provider.component.context.ProviderContext;
import com.dianping.pigeon.remoting.provider.config.ServerConfig;

/**
 * 
 * 
 * @author jianhuihuang
 * @version $Id: Server.java, v 0.1 2013-6-17 下午6:38:37 jianhuihuang Exp $
 */
public interface Server {

	void start(ServerConfig serverConfig);

	void stop();

	ServerConfig getServerConfig();

	public void processRequest(final InvocationRequest request, final ProviderContext providerContext);

}
