/**
 * Dianping.com Inc.
 * Copyright (c) 2003-2014 All Rights Reserved.
 */
package com.dianping.pigeon.remoting.invoker.process;

import java.util.ArrayList;
import java.util.List;

public class InvokerProcessInterceptorFactory {

	private static List<InvokerProcessInterceptor> interceptors = new ArrayList<InvokerProcessInterceptor>();

	public static boolean registerInterceptor(InvokerProcessInterceptor providerProcessInterceptor) {
		if (!interceptors.contains(providerProcessInterceptor)) {
			return interceptors.add(providerProcessInterceptor);
		}
		return false;
	}

	public static boolean unregisterInterceptor(InvokerProcessInterceptor providerProcessInterceptor) {
		return interceptors.remove(providerProcessInterceptor);
	}

	public static List<InvokerProcessInterceptor> getInterceptors() {
		return interceptors;
	}
}
