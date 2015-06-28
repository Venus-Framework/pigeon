/**
 * Dianping.com Inc.
 * Copyright (c) 2003-2013 All Rights Reserved.
 */
package com.dianping.pigeon.remoting.common.monitor;


public final class MonitorHelper {

	private static ThreadLocal<Integer> tlSize = new ThreadLocal<Integer>();

	public static void setSize(Integer size) {
		tlSize.set(size);
	}

	public static Integer getSize() {
		Integer size = tlSize.get();
		tlSize.remove();
		return size;
	}

}
