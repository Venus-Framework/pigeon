/**
 * Dianping.com Inc.
 * Copyright (c) 2003-2013 All Rights Reserved.
 */
package com.dianping.pigeon.remoting.http;

import com.dianping.pigeon.remoting.common.domain.InvocationRequest;
import com.dianping.pigeon.remoting.common.util.InvocationUtils;
import com.dianping.pigeon.remoting.http.adapter.HttpAdapterRequest;

public class HttpUtils {

	public static final String URL_PREFIX = "@HTTP@";

	public static String getDefaultServiceUrl(String serviceUrl) {
		if (serviceUrl == null) {
			return serviceUrl;
		}
		int idx = serviceUrl.indexOf(URL_PREFIX);
		if (idx != -1) {
			return serviceUrl.substring(idx + URL_PREFIX.length());
		}
		return serviceUrl;
	}

	public static String getHttpServiceUrl(String serviceUrl) {
		if (serviceUrl == null) {
			return serviceUrl;
		}
		int idx = serviceUrl.indexOf(URL_PREFIX);
		if (idx == -1) {
			return URL_PREFIX + serviceUrl;
		}
		return serviceUrl;
	}

	public static InvocationRequest createDefaultRequest(HttpAdapterRequest httpAdapterRequest) {
		return InvocationUtils.newRequest(httpAdapterRequest.getUrl(), httpAdapterRequest.getMethod(),
				httpAdapterRequest.getParameters(), httpAdapterRequest.getSerialize(),
				httpAdapterRequest.getMessageType(), httpAdapterRequest.getTimeout(), httpAdapterRequest.getCallType(),
				httpAdapterRequest.getSeq());
	}
}
