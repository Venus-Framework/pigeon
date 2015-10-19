package com.dianping.pigeon.governor.lion.registry;

import org.apache.curator.framework.CuratorFramework;

public interface ZkListenerContext {

	public CuratorFramework getCuratorClient();
	
	public String getListenerPath();
}
