package com.dianping.pigeon.governor.lion.registry.filter;

import com.dianping.pigeon.governor.lion.registry.ZkListenerChain;
import com.dianping.pigeon.governor.lion.registry.ZkListenerContext;

public interface ZkListenerFilter {

	public void invoke(ZkListenerContext context, ZkListenerChain chain) throws Throwable;
	
}
