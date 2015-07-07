/**
 * Dianping.com Inc.
 * Copyright (c) 2003-2013 All Rights Reserved.
 */
package com.dianping.pigeon.remoting.invoker.util;

import com.dianping.dpsf.async.ServiceCallback;

public final class InvokerHelper {

	private static ThreadLocal<String> tlAddress = new ThreadLocal<String>();
	private static ThreadLocal<Integer> tlTimeout = new ThreadLocal<Integer>();
	private static ThreadLocal<ServiceCallback> tlCallback = new ThreadLocal<ServiceCallback>();

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

	public static void setCallback(ServiceCallback callback) {
		tlCallback.set(callback);
	}

	public static ServiceCallback getCallback() {
		ServiceCallback callback = tlCallback.get();
		return callback;
	}

	public static void clearCallback() {
		tlCallback.remove();
	}
}
