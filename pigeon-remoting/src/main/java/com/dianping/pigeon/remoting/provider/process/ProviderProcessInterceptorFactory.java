/**
 * Dianping.com Inc.
 * Copyright (c) 2003-2014 All Rights Reserved.
 */
package com.dianping.pigeon.remoting.provider.process;

import java.util.ArrayList;
import java.util.List;

public class ProviderProcessInterceptorFactory {

	private static List<ProviderProcessInterceptor> interceptors = new ArrayList<ProviderProcessInterceptor>();

	public static boolean registerInterceptor(ProviderProcessInterceptor providerProcessInterceptor) {
		if (!interceptors.contains(providerProcessInterceptor)) {
			return interceptors.add(providerProcessInterceptor);
		}
		return false;
	}

	public static boolean unregisterInterceptor(ProviderProcessInterceptor providerProcessInterceptor) {
		return interceptors.remove(providerProcessInterceptor);
	}

	public static List<ProviderProcessInterceptor> getInterceptors() {
		return interceptors;
	}
}
