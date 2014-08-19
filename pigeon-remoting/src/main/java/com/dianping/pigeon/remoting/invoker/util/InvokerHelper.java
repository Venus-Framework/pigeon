/**
 * Dianping.com Inc.
 * Copyright (c) 2003-2013 All Rights Reserved.
 */
package com.dianping.pigeon.remoting.invoker.util;

public final class InvokerHelper {

	private static ThreadLocal<String> tlAddress = new ThreadLocal<String>();
	private static ThreadLocal<Integer> tlTimeout = new ThreadLocal<Integer>();

	public static void setAddress(String address) {
		tlAddress.set(address);
	}

	public static String getAddress() {
		String address = tlAddress.get();
		tlAddress.remove();
		return address;
	}

	public static void setTimeout(Integer timeout) {
		tlTimeout.set(timeout);
	}

	public static Integer getTimeout() {
		Integer timeout = tlTimeout.get();
		tlTimeout.remove();
		return timeout;
	}
}
