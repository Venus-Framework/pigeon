/**
 * Dianping.com Inc.
 * Copyright (c) 2003-2013 All Rights Reserved.
 */
package com.dianping.pigeon.remoting.netty.provider;

import com.dianping.pigeon.remoting.provider.Server;
import com.dianping.pigeon.remoting.provider.ServerFactory;

/**
 * 
 * @author xiangwu
 * @Sep 11, 2013
 * 
 */
public class NettyServerFactory implements ServerFactory {

	@Override
	public Server createServer(int port) {
		return new NettyServer(port);
	}

}
