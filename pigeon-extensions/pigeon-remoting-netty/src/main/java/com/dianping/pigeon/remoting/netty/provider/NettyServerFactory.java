/**
 * Dianping.com Inc.
 * Copyright (c) 2003-2013 All Rights Reserved.
 */
package com.dianping.pigeon.remoting.netty.provider;

import com.dianping.pigeon.remoting.provider.Server;
import com.dianping.pigeon.remoting.provider.ServerFactory;
import com.dianping.pigeon.remoting.provider.component.ProviderConfig;

/**
 * 
 * @author xiangwu
 * @Sep 11, 2013
 * 
 */
public class NettyServerFactory implements ServerFactory {

	private int port;

	@Override
	public Server createServer(ProviderConfig providerConfig) {
		this.port = providerConfig.getPort();
		return new NettyServer(providerConfig.getPort());
	}

	@Override
	public int getPort() {
		return port;
	}

}
