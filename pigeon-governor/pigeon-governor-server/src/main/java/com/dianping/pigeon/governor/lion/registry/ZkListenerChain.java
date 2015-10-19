package com.dianping.pigeon.governor.lion.registry;

public interface ZkListenerChain {

	public void dofilter(ZkListenerContext context) throws Throwable;
}
