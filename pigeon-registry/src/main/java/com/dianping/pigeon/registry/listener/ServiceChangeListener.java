/**
 * Dianping.com Inc.
 * Copyright (c) 2003-2013 All Rights Reserved.
 */
package com.dianping.pigeon.registry.listener;

import java.util.List;

public interface ServiceChangeListener {

	// 当服务器权重变化
	void onHostWeightChange(String host, int weight);

	// 当服务新增机器、修改机器。 String[]默认1维数组，ip+port
	void onServiceHostChange(String serviceName, List<String[]> hostList);
	
}
