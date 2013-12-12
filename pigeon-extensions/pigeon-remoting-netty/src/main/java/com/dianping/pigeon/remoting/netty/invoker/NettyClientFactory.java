/**
 * Dianping.com Inc.
 * Copyright (c) 2003-2013 All Rights Reserved.
 */
package com.dianping.pigeon.remoting.netty.invoker;

import com.dianping.pigeon.remoting.invoker.Client;
import com.dianping.pigeon.remoting.invoker.ClientFactory;
import com.dianping.pigeon.remoting.invoker.domain.ConnectInfo;

/**
 * 
 * @author xiangwu
 * @Sep 11, 2013
 * 
 */
public class NettyClientFactory implements ClientFactory {

	@Override
	public Client createClient(ConnectInfo cmd) {
		return new NettyClient(cmd);
	}

}
