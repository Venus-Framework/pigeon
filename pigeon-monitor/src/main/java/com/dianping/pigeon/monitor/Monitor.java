/**
 * Dianping.com Inc.
 * Copyright (c) 2003-${year} All Rights Reserved.
 */
package com.dianping.pigeon.monitor;

/**
 * @author xiangwu
 * @Sep 25, 2013
 * 
 */
public interface Monitor {

	void init();

	void logError(String msg, Throwable t);

	void logError(Throwable t);

	void logEvent(String name, String event, String desc);

	/**
	 * write monitor's own error to local log files or console
	 * 
	 * @param t
	 */
	void logMonitorError(Throwable t);

	MonitorTransaction createTransaction(String name, String uri, Object invocationContext, boolean autoCommit);

	MonitorTransaction getCurrentServiceTransaction();

	MonitorTransaction getCurrentCallTransaction();
	
	void setCurrentServiceTransaction(MonitorTransaction transaction);

	void setCurrentCallTransaction(MonitorTransaction transaction);

	void clearServiceTransaction();

	void clearCallTransaction();

	MonitorTransaction copyTransaction(String name, String uri, Object invocationContext, MonitorTransaction transaction);
}
