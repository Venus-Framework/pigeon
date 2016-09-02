/**
 * Dianping.com Inc.
 * Copyright (c) 2003-2013 All Rights Reserved.
 */
package com.dianping.pigeon.remoting.invoker.util;

import java.util.concurrent.Future;

import com.dianping.pigeon.remoting.invoker.concurrent.FutureFactory;
import com.dianping.pigeon.remoting.invoker.concurrent.InvocationCallback;

public final class InvokerHelper {

	private static ThreadLocal<String> tlAddress = new ThreadLocal<String>();
	private static ThreadLocal<Integer> tlTimeout = new ThreadLocal<Integer>();
	private static ThreadLocal<InvocationCallback> tlCallback = new ThreadLocal<InvocationCallback>();
	private static ThreadLocal<Boolean> tlCancel = new ThreadLocal<Boolean>() {
		protected Boolean initialValue() {
			return false;
		}
	};
	private static ThreadLocal<Object> tlDefaultResult = new ThreadLocal<Object>();
	private static ThreadLocal<Boolean> tlLogCallException = new ThreadLocal<Boolean>() {
		protected Boolean initialValue() {
			return true;
		}
	};

	public static void setDefaultResult(Object defaultResult) {
		tlDefaultResult.set(defaultResult);
	}

	public static Object getDefaultResult() {
		Object result = tlDefaultResult.get();
		tlDefaultResult.remove();
		return result;
	}

	public static void setLogCallException(boolean logCallException) {
		tlLogCallException.set(logCallException);
	}

	public static boolean getLogCallException() {
		boolean logCallException = tlLogCallException.get();
		tlLogCallException.remove();
		return logCallException;
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

	public static void setCallback(InvocationCallback callback) {
		tlCallback.set(callback);
	}

	public static InvocationCallback getCallback() {
		InvocationCallback callback = tlCallback.get();
		return callback;
	}

	public static void clearCallback() {
		tlCallback.remove();
	}

	public static Future<?> getFuture() {
		return FutureFactory.getFuture();
	}

	public static <T> Future<T> getFuture(Class<T> type) {
		return FutureFactory.getFuture(type);
	}

}
