/**
 * Dianping.com Inc.
 * Copyright (c) 2003-2014 All Rights Reserved.
 */
package com.dianping.pigeon.remoting.provider.process;

import java.util.List;

import com.dianping.pigeon.extension.ExtensionLoader;

public class ProviderInterceptorFactory {

	private static List<ProviderInterceptor> interceptors = ExtensionLoader
			.getExtensionList(ProviderInterceptor.class);

	public static boolean registerInterceptor(ProviderInterceptor providerContextInterceptor) {
		if (!interceptors.contains(providerContextInterceptor)) {
			return interceptors.add(providerContextInterceptor);
		}
		return false;
	}

	public static boolean unregisterInterceptor(ProviderInterceptor providerContextInterceptor) {
		return interceptors.remove(providerContextInterceptor);
	}

	public static List<ProviderInterceptor> getInterceptors() {
		return interceptors;
	}
}
