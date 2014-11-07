/**
 * Dianping.com Inc.
 * Copyright (c) 2003-2013 All Rights Reserved.
 */
package com.dianping.pigeon.demo.http;

import java.io.ByteArrayOutputStream;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpConnectionManager;
import org.apache.commons.httpclient.MultiThreadedHttpConnectionManager;
import org.apache.commons.httpclient.methods.ByteArrayRequestEntity;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.params.HttpConnectionManagerParams;

public class WechatTest {

	static String postUrl = "https://api.weixin.qq.com/sns/authorize/profiles?access_token=ACCESS_TOKEN&sns_ticket=SNS_TICKET";

	public static void main(String[] args) throws Exception {
		String request1 = "{\"openidlist\":[{\"openid\":\"OPENID1\"},{\"openid\":\"OPENID2\"},{\"openid\":\"OPENID3\"}]}";
		System.out.println(invoker(request1));
	}

	private static HttpClient getHttpClient() {
		HttpConnectionManager connectionManager = new MultiThreadedHttpConnectionManager();
		HttpConnectionManagerParams params = new HttpConnectionManagerParams();
		params.setMaxTotalConnections(300);
		params.setDefaultMaxConnectionsPerHost(50);
		params.setConnectionTimeout(3000);
		params.setTcpNoDelay(true);
		params.setSoTimeout(3000);
		params.setStaleCheckingEnabled(true);
		connectionManager.setParams(params);
		HttpClient httpClient = new HttpClient();
		httpClient.setHttpConnectionManager(connectionManager);

		return httpClient;
	}

	public static String invoker(String request) throws Exception {
		HttpClient httpClient = getHttpClient();
		PostMethod method = null;
		String response = null;
		try {
			method = new PostMethod(postUrl);
			// method.addRequestHeader("serialize", "7");
			ByteArrayOutputStream baos = new ByteArrayOutputStream(1024);
			try {
				baos.write(request.getBytes());
				method.setRequestEntity(new ByteArrayRequestEntity(baos.toByteArray(),
						"application/json; charset=utf-8"));
			} finally {
				baos.close();
			}
			httpClient.executeMethod(method);
			if (method.getStatusCode() >= 300) {
				throw new IllegalStateException("" + method.getStatusCode());
			}
			response = method.getResponseBodyAsString();
			return response;
		} finally {
			if (method != null) {
				method.releaseConnection();
			}
		}
	}
}
