/**
 * Dianping.com Inc.
 * Copyright (c) 2003-2013 All Rights Reserved.
 */
package com.dianping.pigeon.remoting.http.invoker;

import java.net.ConnectException;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpConnectionManager;
import org.apache.commons.httpclient.MultiThreadedHttpConnectionManager;
import org.apache.commons.httpclient.params.HttpConnectionManagerParams;
import com.dianping.pigeon.log.LoggerLoader;
import org.apache.logging.log4j.Logger;

import com.dianping.dpsf.protocol.DefaultRequest;
import com.dianping.pigeon.remoting.common.codec.SerializerFactory;
import com.dianping.pigeon.remoting.common.domain.InvocationRequest;
import com.dianping.pigeon.remoting.common.domain.InvocationResponse;
import com.dianping.pigeon.remoting.common.exception.NetworkException;
import com.dianping.pigeon.remoting.common.util.Constants;
import com.dianping.pigeon.remoting.invoker.AbstractClient;
import com.dianping.pigeon.remoting.invoker.domain.Callback;
import com.dianping.pigeon.remoting.invoker.domain.ConnectInfo;
import com.dianping.pigeon.remoting.invoker.listener.HeartBeatListener;

public class HttpInvokerClient extends AbstractClient {

	private static final Logger logger = LoggerLoader.getLogger(HttpInvokerClient.class);
	private ConnectInfo connectInfo;
	private HttpInvokerExecutor httpInvokerExecutor;
	private String serviceUrlPrefix = null;
	private String defaultServiceUrl = null;
	private boolean isActive = true;
	private boolean isConnected = false;
	public static final String CONTENT_TYPE_SERIALIZED_OBJECT = "application/x-java-serialized-object";

	public HttpInvokerClient(ConnectInfo connectInfo) {
		this.connectInfo = connectInfo;
		if (logger.isInfoEnabled()) {
			logger.info("http client:" + connectInfo);
		}
		serviceUrlPrefix = "http://" + connectInfo.getHost() + ":" + connectInfo.getPort() + "/";
		defaultServiceUrl = serviceUrlPrefix + "service";
		httpInvokerExecutor = new HttpInvokerExecutor();
		HttpConnectionManager connectionManager = new MultiThreadedHttpConnectionManager();
		HttpConnectionManagerParams params = new HttpConnectionManagerParams();
		params.setMaxTotalConnections(300);
		params.setDefaultMaxConnectionsPerHost(50);
		params.setConnectionTimeout(1000);
		params.setTcpNoDelay(true);
		params.setSoTimeout(3000);
		params.setStaleCheckingEnabled(true);
		connectionManager.setParams(params);
		HttpClient httpClient = new HttpClient();
		httpClient.setHttpConnectionManager(connectionManager);
		httpInvokerExecutor.setHttpClient(httpClient);
	}

	@Override
	public ConnectInfo getConnectInfo() {
		return connectInfo;
	}

	@Override
	public void connect() {
		InvocationRequest request = new DefaultRequest(HeartBeatListener.HEART_TASK_SERVICE,
				HeartBeatListener.HEART_TASK_METHOD, null, SerializerFactory.SERIALIZE_HESSIAN,
				Constants.MESSAGE_TYPE_HEART, 5000, null);
		request.setSequence(0);
		request.setCreateMillisTime(System.currentTimeMillis());
		request.setCallType(Constants.CALLTYPE_REPLY);
		InvocationResponse response = null;
		try {
			response = this.write(request);
			if (response != null && response.getSequence() == 0) {
				isConnected = true;
			}
		} catch (Throwable e) {
			isConnected = false;
		}
	}

	@Override
	public InvocationResponse doWrite(InvocationRequest invocationRequest, Callback callback) throws NetworkException {
		return write(defaultServiceUrl, invocationRequest, callback);
	}

	public InvocationResponse write(String url, InvocationRequest invocationRequest, Callback callback)
			throws NetworkException {
		final int timeout = invocationRequest.getTimeout();
		httpInvokerExecutor.setReadTimeout(timeout);
		try {
			InvocationResponse invocationResponse = httpInvokerExecutor.executeRequest(url, invocationRequest);
			this.isConnected = true;
			return invocationResponse;
		} catch (ConnectException e) {
			this.isConnected = false;
			throw new NetworkException(e);
		} catch (Throwable e) {
			throw new NetworkException(e);
		}
	}

	@Override
	public boolean isConnected() {
		return isConnected;
	}

	@Override
	public boolean isActive() {
		return isActive && HeartBeatListener.isActiveAddress(getAddress());
	}

	@Override
	public void setActive(boolean isActive) {
		this.isActive = isActive;
	}

	@Override
	public boolean isWritable() {
		return true;
	}

	@Override
	public String getHost() {
		return connectInfo.getHost();
	}

	@Override
	public String getAddress() {
		return connectInfo.getHost() + ":" + connectInfo.getPort();
	}

	@Override
	public int getPort() {
		return connectInfo.getPort();
	}

	@Override
	public void close() {

	}

	@Override
	public String toString() {
		return this.getAddress();
	}

	@Override
	public boolean isDisposable() {
		return false;
	}

	@Override
	public void dispose() {

	}

	public boolean equals(Object obj) {
		if (obj instanceof HttpInvokerClient) {
			HttpInvokerClient nc = (HttpInvokerClient) obj;
			return this.getAddress().equals(nc.getAddress());
		} else {
			return super.equals(obj);
		}
	}

	@Override
	public int hashCode() {
		return getAddress().hashCode();
	}

	@Override
	public String getProtocol() {
		return Constants.PROTOCOL_HTTP;
	}
}
