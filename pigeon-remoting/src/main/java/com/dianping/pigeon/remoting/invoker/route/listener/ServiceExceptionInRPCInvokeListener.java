/**
 * Dianping.com Inc.
 * Copyright (c) 2003-2013 All Rights Reserved.
 */
package com.dianping.pigeon.remoting.invoker.route.listener;

import org.apache.log4j.Logger;

import com.dianping.pigeon.event.RuntimeServiceEvent;
import com.dianping.pigeon.monitor.Log4jLoader;
import com.dianping.pigeon.remoting.invoker.component.RpcInvokeInfo;
import com.dianping.pigeon.remoting.invoker.route.stat.DpsfAddressStatPool;
import com.dianping.pigeon.remoting.invoker.route.stat.barrel.ServerStatBarrelsHolder;

/**
 * 统计异常情况下的IP使用情况。后续需要增加到appname的维度。
 * 
 * @author jianhuihuang
 * 
 */
public class ServiceExceptionInRPCInvokeListener extends AbstractRPCInvokeListener {

	private static final Logger logger = Log4jLoader.getLogger(ServiceExceptionInRPCInvokeListener.class.getName());

	public void handleEvent(RuntimeServiceEvent event) {

		RpcInvokeInfo rpcInvokeInfo = (RpcInvokeInfo) event.eventObj;
		String ip = rpcInvokeInfo.getAddressIp();
		String serviceName = rpcInvokeInfo.getServiceName();
		if (logger.isDebugEnabled()) {
			logger.debug("rpc ip" + ip);
			logger.debug("servicename is" + serviceName);
		}

		if (ip != null) {
			DpsfAddressStatPool addressStatPool = dpsfAddressStatPoolService.getAddressStatPool(serviceName);
			addressStatPool.error(ip);
		}
		ServerStatBarrelsHolder.flowIn(rpcInvokeInfo.getRequest(), ip);
	}

	public boolean support(RuntimeServiceEvent event) {
		if (event.getEventType() == RuntimeServiceEvent.Type.RUNTIME_RPC_INVOKE_EXCEPTION) {
			return true;
		}
		return false;
	}
}
