/**
 * Dianping.com Inc.
 * Copyright (c) 2003-2013 All Rights Reserved.
 */
package com.dianping.pigeon.remoting.invoker.route.listener;

import com.dianping.pigeon.event.RuntimeServiceListener;
import com.dianping.pigeon.remoting.invoker.route.stat.AddressStatPoolServiceImpl;
import com.dianping.pigeon.remoting.invoker.route.stat.DpsfAddressStatPoolService;

/**
 * @author jianhuihuang
 * 
 */
public abstract class AbstractRPCInvokeListener implements RuntimeServiceListener {

	protected DpsfAddressStatPoolService dpsfAddressStatPoolService = AddressStatPoolServiceImpl.getInstance();

}
