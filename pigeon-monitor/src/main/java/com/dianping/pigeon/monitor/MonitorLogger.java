/**
 * Dianping.com Inc.
 * Copyright (c) 2003-2013 All Rights Reserved.
 */
package com.dianping.pigeon.monitor;



/**
 * @author xiangwu
 * @Oct 8, 2013
 * 
 */
public interface MonitorLogger {

	void logError(String msg, Throwable t);
	
	void logError(Throwable t);

	void logEvent(String name, String event, String desc);

	/**
	 * write monitor's own error to local log files or console
	 * 
	 * @param t
	 */
	void logMonitorError(Throwable t);

	MonitorTransaction createTransaction(String name, String uri, Object invocationContext);
	
	MonitorTransaction getCurrentTransaction();
	
}
