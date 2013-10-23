/**
 * Dianping.com Inc.
 * Copyright (c) 2003-${year} All Rights Reserved.
 */
package com.dianping.pigeon.monitor;

import com.dianping.pigeon.component.invocation.InvocationContext;

/**
 * @author xiangwu
 * @Sep 25, 2013
 * 
 */
public interface MonitorTransaction {

	public void setStatusError(Throwable t);

	public void complete();

	public void setStatusOk();

	public void addData(String name, Object data);

	public InvocationContext getInvocationContext();

	public MonitorLogger getLogger();

	public void readMonitorContext();

	public void writeMonitorContext();

}
