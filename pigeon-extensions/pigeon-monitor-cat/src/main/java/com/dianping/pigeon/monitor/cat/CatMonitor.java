/**
 * Dianping.com Inc.
 * Copyright (c) 2003-2013 All Rights Reserved.
 */
package com.dianping.pigeon.monitor.cat;

import com.dianping.cat.Cat;
import com.dianping.cat.message.MessageProducer;
import com.dianping.pigeon.monitor.Monitor;
import com.dianping.pigeon.monitor.MonitorLogger;

/**
 * @author xiangwu
 * @Sep 25, 2013
 * 
 */
public class CatMonitor implements Monitor {

	CatLogger logger = new CatLogger(null);

	@Override
	public MonitorLogger createLogger() {
		MessageProducer cat = null;
		try {
			cat = Cat.getProducer();
			return new CatLogger(cat);
		} catch (Exception e2) {
			logger.logMonitorError(e2);
		}
		return null;
	}

}
