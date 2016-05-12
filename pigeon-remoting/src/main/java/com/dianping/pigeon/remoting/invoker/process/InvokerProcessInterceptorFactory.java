/**
 * Dianping.com Inc.
 * Copyright (c) 2003-2014 All Rights Reserved.
 */
package com.dianping.pigeon.remoting.invoker.process;

import java.util.ArrayList;
import java.util.List;

/**
 * @deprecated
 * @see InvokerInterceptorFactory
 */
public class InvokerProcessInterceptorFactory {

	private static List<InvokerProcessInterceptor> interceptors = new ArrayList<InvokerProcessInterceptor>();

	public static boolean registerInterceptor(InvokerProcessInterceptor invokerProcessInterceptor) {
		if (!interceptors.contains(invokerProcessInterceptor)) {
			return interceptors.add(invokerProcessInterceptor);
		}
		return false;
	}

	public static boolean unregisterInterceptor(InvokerProcessInterceptor invokerProcessInterceptor) {
		return interceptors.remove(invokerProcessInterceptor);
	}

	public static List<InvokerProcessInterceptor> getInterceptors() {
		return interceptors;
	}
}
