/**
 * Dianping.com Inc.
 * Copyright (c) 2003-${year} All Rights Reserved.
 */
package com.dianping.pigeon.monitor;

import java.util.Map;

/**
 * @author xiangwu
 * @Sep 25, 2013
 * 
 */
public interface MonitorTransaction {

	public void setStatusError(Throwable t);

	public void complete();

	public void setStatusOk();

	public void setDuration(long duration);

	public void addData(String name, Object data);

	public void readMonitorContext();

	public void writeMonitorContext();

	public String getName();
	
	public String getUri();
	
	public Map<String, Object> getDataMap();
	
	public long getDuration();
	
}
