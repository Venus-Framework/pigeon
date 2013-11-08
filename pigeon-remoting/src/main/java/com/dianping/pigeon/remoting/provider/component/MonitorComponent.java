/**
 * Dianping.com Inc.
 * Copyright (c) 2003-2013 All Rights Reserved.
 */
package com.dianping.pigeon.remoting.provider.component;

import org.apache.log4j.Logger;

import com.dianping.pigeon.event.EventManager;
import com.dianping.pigeon.extension.plugin.Component;
import com.dianping.pigeon.monitor.Log4jLoader;

public class MonitorComponent implements Component {

	private static final Logger logger = Log4jLoader.getLogger(MonitorComponent.class);

	private static EventManager eventManager = EventManager.getInstance();

	@Override
	public void init() {
//		if (logger.isInfoEnabled()) {
//			logger.info("初始化，开始注册事件监听器");
//		}
		//eventManager.addServiceListener(new SystemMonitorListener());
		// RequestProcessHandlerFactory.registerBizProcessFilter(ProcessPhase.Before_Write,
		// new MonitorProcessFilter());

		// RequestProcessHandlerFactory.registerHeartBeatProcessFilter(ProcessPhase.Before_Write,
		// new MonitorProcessFilter());
	}

}
