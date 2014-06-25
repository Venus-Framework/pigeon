/**
 * Dianping.com Inc.
 * Copyright (c) 2003-2013 All Rights Reserved.
 */
package com.dianping.pigeon.remoting.invoker.route.support;

public final class RouterHelper {

	private static ThreadLocal<String> tlAddress = new ThreadLocal<String>();

	public static void setAddress(String address) {
		tlAddress.set(address);
	}

	public static String getAddress() {
		String address = tlAddress.get();
		tlAddress.remove();
		return address;
	}

}
