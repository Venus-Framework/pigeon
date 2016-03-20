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
	private static ThreadLocal<Boolean> tlCancel = new ThreadLocal<Boolean>() {
		protected Boolean initialValue() {
			return false;
		}
	};
	private static ThreadLocal<Object> tlDefaultResult = new ThreadLocal<Object>();

	public static void setDefaultResult(Object defaultResult) {
		tlDefaultResult.set(defaultResult);
	}

	public static Object getDefaultResult() {
		Object result = tlDefaultResult.get();
		tlDefaultResult.remove();
		return result;
	}

	public static void setCancel(boolean cancel) {
		tlCancel.set(cancel);
	}

	public static boolean getCancel() {
		boolean cancel = tlCancel.get();
		tlCancel.remove();
		return cancel;
	}

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
