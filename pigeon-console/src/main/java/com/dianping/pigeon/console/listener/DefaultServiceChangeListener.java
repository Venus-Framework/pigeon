/**
 * Dianping.com Inc.
 * Copyright (c) 2003-2013 All Rights Reserved.
 */
package com.dianping.pigeon.console.listener;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpConnectionManager;
import org.apache.commons.httpclient.MultiThreadedHttpConnectionManager;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.params.HttpConnectionManagerParams;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import com.dianping.pigeon.config.ConfigManager;
import com.dianping.pigeon.console.exception.ServiceNotifyException;
import com.dianping.pigeon.extension.ExtensionLoader;
import com.dianping.pigeon.log.LoggerLoader;
import com.dianping.pigeon.monitor.Monitor;
import com.dianping.pigeon.monitor.MonitorLogger;
import com.dianping.pigeon.remoting.common.util.Constants;
import com.dianping.pigeon.remoting.provider.config.ProviderConfig;
import com.dianping.pigeon.remoting.provider.listener.ServiceChangeListener;
import com.dianping.pigeon.threadpool.DefaultThreadPool;
import com.dianping.pigeon.threadpool.ThreadPool;

public class DefaultServiceChangeListener implements ServiceChangeListener {

	private static final Logger logger = LoggerLoader.getLogger(DefaultServiceChangeListener.class);

	private static ConfigManager configManager = ExtensionLoader.getExtension(ConfigManager.class);

	private Map<String, NotifyEvent> failedNotifyEvents = new ConcurrentHashMap<String, NotifyEvent>();

	private static ThreadPool failureListenerThreadPool = new DefaultThreadPool("pigeon-notify-failure-listener");

	private static final MonitorLogger monitorLogger = ExtensionLoader.getExtension(Monitor.class).getLogger();

	public DefaultServiceChangeListener() {
		failureListenerThreadPool.execute(new NotifyFailureListener(this));
	}

	public Map<String, NotifyEvent> getFailedNotifyEvents() {
		return failedNotifyEvents;
	}

	private HttpClient getHttpClient() {
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

	@Override
	public synchronized void notifyServicePublished(ProviderConfig<?> providerConfig) {
		logger.info("start to notify service published:" + providerConfig);
		notifyServiceChange("publish", providerConfig);
		logger.info("succeed to notify service published:" + providerConfig);
	}

	public synchronized void notifyServiceChange(String action, ProviderConfig<?> providerConfig) {
		String managerAddress = configManager.getStringValue(Constants.KEY_MANAGER_ADDRESS,
				Constants.DEFAULT_MANAGER_ADDRESS);
		String env = providerConfig.getServerConfig().getEnv();
		if (StringUtils.isBlank(env)) {
			env = configManager.getEnv();
		}
		String ip = providerConfig.getServerConfig().getIp();
		if (StringUtils.isBlank(ip)) {
			ip = configManager.getLocalIp();
		}
		String group = providerConfig.getServerConfig().getGroup();
		if (StringUtils.isBlank(group)) {
			group = Constants.DEFAULT_GROUP;
		}
		StringBuilder url = new StringBuilder();
		url.append("http://").append(managerAddress).append("/service/").append(action);
		url.append("?env=").append(env).append("&id=3&updatezk=false&service=");
		url.append(providerConfig.getUrl());
		url.append("&group=").append(group);
		url.append("&ip=").append(ip);
		url.append("&port=").append(providerConfig.getServerConfig().getActualPort());
		if (StringUtils.isNotBlank(configManager.getAppName())) {
			url.append("&app=").append(configManager.getAppName());
		}

		failedNotifyEvents.remove(providerConfig.getUrl());
		boolean isSuccess = false;
		try {
			isSuccess = doNotify(url.toString());
		} catch (Throwable t) {
			logger.error("error while notifying service change to url:" + url, t);
		}
		if (!isSuccess) {
			NotifyEvent event = new NotifyEvent();
			event.setNotifyUrl(url.toString());
			failedNotifyEvents.put(providerConfig.getUrl(), event);
		}
	}

	synchronized boolean doNotify(String url) {
		HttpClient httpClient = getHttpClient();
		GetMethod getMethod = null;
		String response = null;
		Throwable notifyException = null;
		logger.info("service change notify url:" + url);
		try {
			getMethod = new GetMethod(url);
			httpClient.executeMethod(getMethod);
			if (getMethod.getStatusCode() >= 300) {
				throw new IllegalStateException("Did not receive successful HTTP response: status code = "
						+ getMethod.getStatusCode() + ", status message = [" + getMethod.getStatusText() + "]");
			}
			response = getMethod.getResponseBodyAsString();
		} catch (Throwable t) {
			logger.error("error while notifying service change to url:" + url, t);
			notifyException = t;
		} finally {
			if (getMethod != null) {
				getMethod.releaseConnection();
			}
		}
		boolean isSuccess = false;
		if (response != null && response.startsWith("0")) {
			isSuccess = true;
		}
		if (!isSuccess) {
			logger.error("error while notifying service change to url:" + url + ", response:" + response);
			monitorLogger.logError(new ServiceNotifyException("error while notifying service change to url:" + url
					+ ", response:" + response, notifyException));
		}
		return isSuccess;
	}

	@Override
	public synchronized void notifyServiceUnpublished(ProviderConfig<?> providerConfig) {
		logger.info("start to notify service unpublished:" + providerConfig);
		try {
			notifyServiceChange("unpublish", providerConfig);
			logger.info("succeed to notify service unpublished:" + providerConfig);
		} catch (Throwable t) {
			logger.warn(t.getMessage());
		}
	}

}
