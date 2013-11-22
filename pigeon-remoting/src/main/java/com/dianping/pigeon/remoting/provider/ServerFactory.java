/**
 * Dianping.com Inc.
 * Copyright (c) 2003-2013 All Rights Reserved.
 */
package com.dianping.pigeon.remoting.provider;

import com.dianping.pigeon.remoting.provider.component.ProviderConfig;

/**
 * 
 * @author xiangwu
 * @Sep 11, 2013
 * 
 */
public interface ServerFactory {

	final int DEFAULT_PORT = 4625;

	Server createServer(ProviderConfig providerConfig);
	
	int getPort();
}
