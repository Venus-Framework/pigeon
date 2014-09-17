/**
 * Dianping.com Inc.
 * Copyright (c) 2003-2013 All Rights Reserved.
 */
package com.dianping.pigeon.remoting.common.monitor;

import com.dianping.pigeon.remoting.common.monitor.SizeMonitor.SizeMonitorInfo;

public final class MonitorHelper {

	private static ThreadLocal<SizeMonitorInfo> tlSize = new ThreadLocal<SizeMonitorInfo>();

	public static void setSize(SizeMonitorInfo size) {
		tlSize.set(size);
	}

	public static SizeMonitorInfo getSize() {
		SizeMonitorInfo size = tlSize.get();
		tlSize.remove();
		return size;
	}

}
