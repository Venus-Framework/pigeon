/**
 * Dianping.com Inc.
 * Copyright (c) 2003-2013 All Rights Reserved.
 */
package com.dianping.pigeon.remoting.http;

public class HttpUtils {

	public static final String URL_PREFIX = "@HTTP@";

	public static String getDefaultServiceUrl(String serviceUrl) {
		int idx = serviceUrl.indexOf(URL_PREFIX);
		if (idx != -1) {
			return serviceUrl.substring(idx + URL_PREFIX.length());
		}
		return serviceUrl;
	}

	public static String getHttpServiceUrl(String serviceUrl) {
		int idx = serviceUrl.indexOf(URL_PREFIX);
		if (idx == -1) {
			return URL_PREFIX + serviceUrl;
		}
		return serviceUrl;
	}
}
