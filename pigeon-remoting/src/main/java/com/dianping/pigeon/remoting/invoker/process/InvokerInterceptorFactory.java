/**
 * Dianping.com Inc.
 * Copyright (c) 2003-2014 All Rights Reserved.
 */
package com.dianping.pigeon.remoting.invoker.process;

import java.util.List;

import com.dianping.pigeon.extension.ExtensionLoader;

public class InvokerInterceptorFactory {

	private static List<InvokerInterceptor> interceptors = ExtensionLoader.getExtensionList(InvokerInterceptor.class);

	public static boolean registerInterceptor(InvokerInterceptor invokerContextInterceptor) {
		if (!interceptors.contains(invokerContextInterceptor)) {
			return interceptors.add(invokerContextInterceptor);
		}
		return false;
	}

	public static boolean unregisterInterceptor(InvokerInterceptor invokerContextInterceptor) {
		return interceptors.remove(invokerContextInterceptor);
	}

	public static List<InvokerInterceptor> getInterceptors() {
		return interceptors;
	}
}
