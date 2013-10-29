/**
 * Dianping.com Inc.
 * Copyright (c) 2003-2013 All Rights Reserved.
 */
package com.dianping.pigeon.monitor;

import com.dianping.pigeon.component.invocation.InvocationContext;


/**
 * @author xiangwu
 * @Oct 8, 2013
 * 
 */
public interface MonitorLogger {

	void logError(Throwable t);

	void logEvent(String name, String source, String event);

	/**
	 * write monitor's own error to local log files or console
	 * 
	 * @param t
	 */
	void logMonitorError(Throwable t);

	MonitorTransaction createTransaction(String name, String uri, InvocationContext invocationContext);
}
