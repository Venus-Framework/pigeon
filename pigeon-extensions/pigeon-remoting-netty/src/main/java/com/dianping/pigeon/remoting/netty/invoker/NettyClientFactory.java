/**
 * Dianping.com Inc.
 * Copyright (c) 2003-2013 All Rights Reserved.
 */
/**
 * 
 */
package com.dianping.pigeon.remoting.netty.invoker;

import java.util.Map;

import com.dianping.pigeon.remoting.invoker.Client;
import com.dianping.pigeon.remoting.invoker.ClientFactory;
import com.dianping.pigeon.remoting.invoker.domain.ConnectInfo;
import com.dianping.pigeon.util.CollectionUtils;

/**
 *
 */
public class NettyClientFactory implements ClientFactory {

	@Override
	public boolean support(ConnectInfo connectInfo) {
		Map<String, Integer> serviceNames = connectInfo.getServiceNames();
		if (!CollectionUtils.isEmpty(serviceNames)) {
			String name = serviceNames.keySet().iterator().next();
			if (name.startsWith("@")) {
				return false;
			}
		}
		return true;
	}

	@Override
	public Client createClient(ConnectInfo connectInfo) {
		return new NettyClient(connectInfo);
	}

}
