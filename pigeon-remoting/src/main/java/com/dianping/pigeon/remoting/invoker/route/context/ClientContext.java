/**
 * Dianping.com Inc.
 * Copyright (c) 2003-2013 All Rights Reserved.
 */
package com.dianping.pigeon.remoting.invoker.route.context;

/**
 * 
 * 
 * @author jianhuihuang
 * @version $Id: ClientContext.java, v 0.1 2013-6-29 下午7:25:39 jianhuihuang Exp
 *          $
 */
public final class ClientContext {

	private static ThreadLocal<String> used_tl = new ThreadLocal<String>();

	private static ThreadLocal<String> use_tl = new ThreadLocal<String>();

	public static String getUsedClientAddress() {
		String address = used_tl.get();
		used_tl.remove();
		return address;
	}

	public static void setUsedClientAddress(String address) {
		used_tl.set(address);
	}

	public static void setUseClientAddress(String address) {
		use_tl.set(address);
	}

	public static String getUseClientAddress() {
		String address = use_tl.get();
		use_tl.remove();
		return address;
	}

}
