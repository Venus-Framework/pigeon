/**
 * Dianping.com Inc.
 * Copyright (c) 2003-2013 All Rights Reserved.
 */
package com.dianping.pigeon.monitor.cat;

import com.dianping.cat.Cat;
import com.dianping.cat.message.MessageProducer;
import com.dianping.cat.message.Transaction;
import com.dianping.pigeon.monitor.Monitor;
import com.dianping.pigeon.monitor.MonitorLogger;

/**
 * @author xiangwu
 * @Sep 25, 2013
 * 
 */
public class CatMonitor implements Monitor {

	CatLogger innerLogger = new CatLogger(null);
	MonitorLogger logger = null;
	volatile boolean isInitialized = false;

	public MonitorLogger createLogger() {
		MessageProducer cat = null;
		try {
			cat = Cat.getProducer();
			return new CatLogger(cat);
		} catch (Throwable e2) {
			innerLogger.logMonitorError(e2);
		}
		return null;
	}

	@Override
	public void init() {
		if (!isInitialized) {
			logger = createLogger();
			Transaction t = Cat.newTransaction("System", "PigeonClientStart");
			t.setStatus("0");
			t.complete();
			isInitialized = true;
		}
	}

	public MonitorLogger getLogger() {
		if (logger == null) {
			logger = createLogger();
		}
		return logger;
	}
}
