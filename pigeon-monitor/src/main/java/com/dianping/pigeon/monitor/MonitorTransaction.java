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
public interface MonitorTransaction {

	public void setStatusError(Throwable t);

	public void complete();
	
	public void complete(long startTime);

	public void setStatusOk();

	public void addData(String name, Object data);

	public void readMonitorContext();

	public void writeMonitorContext();

	public void logEvent(String name, String event, String desc);

}
